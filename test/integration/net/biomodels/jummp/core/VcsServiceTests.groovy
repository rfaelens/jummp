package net.biomodels.jummp.core

import static org.junit.Assert.*
import org.junit.*
import net.biomodels.jummp.plugins.git.GitManagerFactory
import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.tmatesoft.svn.core.io.SVNRepositoryFactory
import org.springframework.context.ApplicationContextAware
import org.springframework.context.ApplicationContext
import org.springframework.security.access.AccessDeniedException
import net.biomodels.jummp.model.Model
import net.biomodels.jummp.model.Revision
import net.biomodels.jummp.plugins.security.User
import net.biomodels.jummp.core.vcs.VcsException
import net.biomodels.jummp.core.vcs.FileNotVersionedException
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.lib.Constants
import net.biomodels.jummp.model.ModelFormat
import org.springframework.mock.web.MockServletContext
import org.springframework.core.io.FileSystemResourceLoader
import java.util.List
import java.util.LinkedList

class VcsServiceTests extends JummpIntegrationTest implements ApplicationContextAware {
    /**
     * Dependency injection of the service we want to test
     */
    def vcsService
    /**
     * Dependency injection of ModelService for easier manipulation
     */
    def modelService
    /**
     * Dependency infection of application context
     */
    def appCtx
    /**
     * Dependency injection of grails Application
     */
    def grailsApplication

    void setApplicationContext(ApplicationContext applicationContext) {
        appCtx = applicationContext
    }

    @Before
    void setUp() {
        createUserAndRoles()
        vcsService.vcsManager = null
        appCtx.getBean("gitManagerFactory").servletContext = new MockServletContext("./target", new FileSystemResourceLoader())
        //appCtx.getBean("svnManagerFactory").servletContext = new MockServletContext("./target", new FileSystemResourceLoader())
    }

    @After
    void tearDown() {
        FileUtils.deleteDirectory(new File("target/vcs/git"))
        FileUtils.deleteDirectory(new File("target/vcs/resource"))
        FileUtils.deleteDirectory(new File("target/vcs/repository"))
        FileUtils.deleteDirectory(new File("target/vcs/exchange"))
        vcsService.vcsManager = null
        appCtx.getBean("gitManagerFactory").git = null
    }

    @Test
    void testGit() {
        // verifies that the service is valid, if git backend is configured correctly
        grailsApplication.config.jummp.vcs.pluginServiceName="gitManagerFactory"
        grailsApplication.config.jummp.plugins.git.enabled=true
        grailsApplication.config.jummp.vcs.workingDirectory="target/vcs/git"
        File gitDirectory = new File("target/vcs/git")
        FileRepositoryBuilder builder = new FileRepositoryBuilder()
        Repository repository = builder.setWorkTree(gitDirectory)
        .readEnvironment() // scan environment GIT_* variables
        .findGitDir() // scan up the file system tree
        .build()
        Git git = new Git(repository)
        git.init().setDirectory(gitDirectory).call()
        assertFalse(vcsService.isValid())
        vcsService.vcsManager = appCtx.getBean("gitManagerFactory").getInstance()
        assertTrue(vcsService.isValid())
    }

    @Ignore("Don't test SVN because it does not reflect the changes made to VcsManager")
    @Test
    void testSvn() {
        // verifies that the service is valid, if svn backend is configured correctly
        grailsApplication.config.jummp.vcs.pluginServiceName = "svnManagerFactory"
        grailsApplication.config.jummp.plugins.subversion.enabled = true
        grailsApplication.config.jummp.plugins.subversion.localRepository = "target/vcs/repository"
        SVNRepositoryFactory.createLocalRepository(new File("target/vcs/repository"), true, false)
        assertFalse(vcsService.isValid())
        vcsService.vcsManager = appCtx.getBean("svnManagerFactory").getInstance()
        assertTrue(vcsService.isValid())
    }

    @Test
    void testImport() {
        String modelIdentifier="target/vcs/git"
        assertFalse(vcsService.isValid())
        // first create a model
        Model model = new Model(name: "test", vcsIdentifier: modelIdentifier)
        Revision revision = new Revision(model: model, vcsId: "1", revisionNumber: 1, owner: User.findByUsername("testuser"), minorRevision: false, description:"", comment: "", uploadDate: new Date(), format: ModelFormat.findByIdentifier("UNKNOWN"))
        assertTrue(revision.validate())
        model.addToRevisions(revision)
        assertTrue(model.validate())
        model.save()
        // without authentication it should fail
        authenticateAnonymous()
        shouldFail(AccessDeniedException) {
            vcsService.importModel(model, null)
        }
        // as user we should get a VcsException as the service is not valid
        authenticateAsUser()
        shouldFail(VcsException) {
            vcsService.importModel(model, null)
        }
        // same of course for admin
        authenticateAsAdmin()
        shouldFail(VcsException) {
            vcsService.importModel(model, null)
        }
        // create a git repository
        List<File> imports=new LinkedList<File>();
        for (i in 0..9)
        {
            imports.add(new File("target/vcs/exchange/test${i}.xml"));
            FileUtils.touch(imports.get(i));
            imports.get(i).append("Test - ${i}\n");
        }
        // setup VCS
        File clone = new File(model.vcsIdentifier)
        clone.mkdirs()
        FileRepositoryBuilder builder = new FileRepositoryBuilder()
        Repository repository = builder.setWorkTree(clone)
        .readEnvironment() // scan environment GIT_* variables
        .findGitDir(clone) // scan up the file system tree
        .build() 
     
        GitManagerFactory gitService = new GitManagerFactory()
        gitService.grailsApplication = grailsApplication
        grailsApplication.config.jummp.plugins.git.enabled = true
        grailsApplication.config.jummp.vcs.workingDirectory = "target/vcs/git"
        grailsApplication.config.jummp.vcs.exchangeDirectory = "target/vcs/exchange"
        vcsService.vcsManager = gitService.getInstance()
        assertTrue(vcsService.isValid())
        // now as user we should be able to import
        authenticateAsTestUser()
        String rev= vcsService.importModel(model, imports)
        
        for (i in 0..9)
        {
            File gitFile = new File("target/vcs/git/test${i}.xml")
            List<String> lines = gitFile.readLines()
            assertEquals(1, lines.size())
            assertEquals("Test - ${i}".toString(), lines[0])
        }
        // ensure the revision and commit message is correct
        ObjectId commit = repository.resolve(Constants.HEAD)
        RevWalk revWalk = new RevWalk(repository)
        RevCommit revCommit = revWalk.parseCommit(commit)
        assertEquals(commit.getName(), rev)
        // we did not specify a commit message, so default should be used
        assertEquals("Import of ${model.name}".toString(), revCommit.getShortMessage())
        assertEquals("Import of ${model.name}".toString(), revCommit.getFullMessage())
        // importing again should fail
/*        shouldFail(VcsException) {
            vcsService.importFile(model, importFile)
        }*/
    }

    @Test
    void testUpdate() {
        assertFalse(vcsService.isValid())
        // first create a model
        String modelIdentifier="target/vcs/git"
        assertFalse(vcsService.isValid())
        // first create a model
        Model model = new Model(name: "test", vcsIdentifier: modelIdentifier)
        Revision revision = new Revision(model: model, vcsId: "1", revisionNumber: 1, owner: User.findByUsername("testuser"), minorRevision: false, description:"", comment: "", uploadDate: new Date(), format: ModelFormat.findByIdentifier("UNKNOWN"))
        assertTrue(revision.validate())
        model.addToRevisions(revision)
        assertTrue(model.validate())
        model.save()
        
        authenticateAnonymous()
        shouldFail(AccessDeniedException) {
            vcsService.updateModel(model, null, null)
        }
        // as user we should get an access denied exception
        authenticateAsTestUser()
        shouldFail(AccessDeniedException) {
            vcsService.updateModel(model, null, null)
        }
        // as admin we should get an VcsException as the vcsService is not valid
        authenticateAsAdmin()
        shouldFail(VcsException) {
            vcsService.updateModel(model, null, null)
        }
        // now also the user should get a VcsException
        modelService.grantWriteAccess(model, User.findByUsername("testuser"))
        authenticateAsTestUser()
        shouldFail(VcsException) {
            vcsService.updateModel(model, null, null)
        }
        
        List<File> imports=new LinkedList<File>();
        for (i in 0..9)
        {
            imports.add(new File("target/vcs/exchange/test${i}.xml"));
            FileUtils.touch(imports.get(i));
            imports.get(i).append("Test - ${i}\n");
        }
        // setup VCS
        File clone = new File(model.vcsIdentifier)
        clone.mkdirs()
        FileRepositoryBuilder builder = new FileRepositoryBuilder()
        Repository repository = builder.setWorkTree(clone)
        .readEnvironment() // scan environment GIT_* variables
        .findGitDir(clone) // scan up the file system tree
        .build()

        GitManagerFactory gitService = new GitManagerFactory()
        gitService.grailsApplication = grailsApplication
        grailsApplication.config.jummp.plugins.git.enabled = true
        grailsApplication.config.jummp.vcs.exchangeDirectory = "target/vcs/exchange"
        grailsApplication.config.jummp.vcs.workingDirectory = "target/vcs/git"
        vcsService.vcsManager = gitService.getInstance()
        assertTrue(vcsService.isValid())

       /* // file is not yet imported, so uploading should fail with a VcsException
        shouldFail(FileNotVersionedException) {
            vcsService.updateFile(model, importFile, null)
        }
        // import a file to the git repository, to make future updates possible
        gitService.getInstance().importFile(importFile, "test.xml")*/
        
        
        String rev = vcsService.updateModel(model, imports, null)

        for (i in 0..9)
        {
            File gitFile = new File("target/vcs/git/test${i}.xml")
            List<String> lines = gitFile.readLines()
            assertEquals(1, lines.size())
            assertEquals("Test - ${i}".toString(), lines[0])
        }

        // ensure the revision and commit message is correct
        ObjectId commit = repository.resolve(Constants.HEAD)
        RevWalk revWalk = new RevWalk(repository)
        RevCommit revCommit = revWalk.parseCommit(commit)
        assertEquals(commit.getName(), rev)
        // we passed null as commit message, so default should be used
        assertEquals("Update of $model.name".toString(), revCommit.getShortMessage())
        assertEquals("Update of $model.name".toString(), revCommit.getFullMessage())
        // try again with an empty commit message - should also be default message
        imports.each
        {
            it.append("Second Test\n")
        }
        rev = vcsService.updateModel(model, imports, null)
        
        for (i in 0..9)
        {
            File gitFile = new File("target/vcs/git/test${i}.xml")
            List<String> lines = gitFile.readLines()
            assertEquals(2, lines.size())
            assertEquals("Test - ${i}".toString(), lines[0])
            assertEquals("Second Test", lines[1])
        }

        
        repository = builder.setWorkTree(clone)
        .readEnvironment() // scan environment GIT_* variables
        .findGitDir(clone) // scan up the file system tree
        .build()
        commit = repository.resolve(Constants.HEAD)
        revWalk = new RevWalk(repository)
        revCommit = revWalk.parseCommit(commit)
        assertEquals(commit.getName(), rev)
        assertEquals("Update of $model.name".toString(), revCommit.getShortMessage())
        assertEquals("Update of $model.name".toString(), revCommit.getFullMessage())
        // try with a custom commit message
        
        imports.each
        {
            it.append("Third Test\n")
        }
        rev = vcsService.updateModel(model, imports, "Commit Message")

        for (i in 0..9)
        {
            File gitFile = new File("target/vcs/git/test${i}.xml")
            List<String> lines = gitFile.readLines()
            assertEquals(3, lines.size())
            assertEquals("Test - ${i}".toString(), lines[0])
            assertEquals("Second Test", lines[1])
            assertEquals("Third Test", lines[2])
        }

        repository = builder.setWorkTree(clone)
        .readEnvironment() // scan environment GIT_* variables
        .findGitDir(clone) // scan up the file system tree
        .build()
        commit = repository.resolve(Constants.HEAD)
        revWalk = new RevWalk(repository)
        revCommit = revWalk.parseCommit(commit)
        assertEquals(commit.getName(), rev)
        assertEquals("Commit Message", revCommit.getShortMessage())
        assertEquals("Commit Message", revCommit.getFullMessage())
        // and finally verify as admin
        authenticateAsAdmin()

        imports.each
        {
            it.append("Admin Test\n")
        }

        rev = vcsService.updateModel(model, imports, "Admin Commit Message")


        for (i in 0..9)
        {
            File gitFile = new File("target/vcs/git/test${i}.xml")
            List<String> lines = gitFile.readLines()
            assertEquals(4, lines.size())
            assertEquals("Test - ${i}".toString(), lines[0])
            assertEquals("Second Test", lines[1])
            assertEquals("Third Test", lines[2])
            assertEquals("Admin Test", lines[3])
        }

        
        repository = builder.setWorkTree(clone)
        .readEnvironment() // scan environment GIT_* variables
        .findGitDir(clone) // scan up the file system tree
        .build()
        commit = repository.resolve(Constants.HEAD)
        revWalk = new RevWalk(repository)
        revCommit = revWalk.parseCommit(commit)
        assertEquals(commit.getName(), rev)
        assertEquals("Admin Commit Message", revCommit.getShortMessage())
        assertEquals("Admin Commit Message", revCommit.getFullMessage())
        // and last but not least user should still get an AccessDeniedException
        authenticateAsUser()
        shouldFail(AccessDeniedException) {
            vcsService.updateModel(model, imports, "Commit Message")
        }
    }

    @Test
    void testRetrieve() {
        assertFalse(vcsService.isValid())
        // first create a model
        String modelIdentifier="target/vcs/git"
        assertFalse(vcsService.isValid())
        // first create a model
        Model model = new Model(name: "test", vcsIdentifier: modelIdentifier)
        Revision revision = new Revision(model: model, vcsId: "1", revisionNumber: 1, owner: User.findByUsername("testuser"), minorRevision: false, description:"", comment: "", uploadDate: new Date(), format: ModelFormat.findByIdentifier("UNKNOWN"))
        assertTrue(revision.validate())
        model.addToRevisions(revision)
        assertTrue(model.validate())
        model.save()
        // without authentication it should fail
        authenticateAnonymous()
        shouldFail(AccessDeniedException) {
            vcsService.retrieveFiles(revision)
        }
        // as user it should fail - no ACL yet
        authenticateAsTestUser()
        shouldFail(AccessDeniedException) {
            vcsService.retrieveFiles(revision)
        }
        // as admin it should fail with a VcsException cause the VCS is not yet valid
        authenticateAsAdmin()
        shouldFail(VcsException) {
            vcsService.retrieveFiles(revision)
        }
        modelService.grantReadAccess(model, User.findByUsername("testuser"))
        // as testuser it should fail now with a VcsException instead of AccessDeniedException
        authenticateAsTestUser()
        shouldFail(VcsException) {
            vcsService.retrieveFiles(revision)
        }
        // create a git repository
        List<File> imports=new LinkedList<File>();
        List<Integer> numbers=new LinkedList<Integer>();
        for (i in 0..9)
        {
            imports.add(new File("target/vcs/exchange/test${i}.xml"));
            FileUtils.touch(imports.get(i));
            imports.get(i).append("${i}\n");
            numbers.add("${i}".toString());
        }
        // setup VCS
        File clone = new File(model.vcsIdentifier)
        clone.mkdirs()

        FileRepositoryBuilder builder = new FileRepositoryBuilder()
        Repository repository = builder.setWorkTree(clone)
        .readEnvironment() // scan environment GIT_* variables
        .findGitDir() // scan up the file system tree
        .build()

        GitManagerFactory gitService = new GitManagerFactory()
        gitService.grailsApplication = grailsApplication
        grailsApplication.config.jummp.plugins.git.enabled = true
        grailsApplication.config.jummp.vcs.workingDirectory = "target/vcs/git"
        grailsApplication.config.jummp.vcs.exchangeDirectory = "target/vcs/exchange"
        vcsService.vcsManager = gitService.getInstance()
        assertTrue(vcsService.isValid())
        // git is valid, but file does not yet exist
        shouldFail(VcsException) {
            vcsService.retrieveFiles(revision)
        }
        
        revision.vcsId = vcsService.importModel(model, imports)
        revision.save(flush: true)
        List<File> exchangeFiles = vcsService.retrieveFiles(revision)
        exchangeFiles.each
        { exchange ->
            imports.each
            { imported ->
                 assertNotSame(exchange, imported)
            }
            List<String> lines = exchange.readLines()
            assertEquals(1, lines.size())
            numbers.remove(lines.first());
        }
        assertTrue(numbers.isEmpty());
    }
}
