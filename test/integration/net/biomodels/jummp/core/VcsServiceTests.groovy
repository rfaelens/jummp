package net.biomodels.jummp.core

import grails.test.*
import net.biomodels.jummp.plugins.git.GitService
import net.biomodels.jummp.plugins.subversion.SvnService
import org.apache.commons.io.FileUtils
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.tmatesoft.svn.core.io.SVNRepositoryFactory
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

class VcsServiceTests extends JummpIntegrationTestCase {
    /**
     * Need a dependency injection for the tests using security
     */
    def vcsService
    /**
     * Dependency injection of ModelService for easier manipulation
     */
    def modelService
    protected void setUp() {
        super.setUp()
        createUserAndRoles()
        mockLogging(VcsService, true)
        mockLogging(GitService, true)
        mockLogging(SvnService, true)
    }

    protected void tearDown() {
        super.tearDown()
        FileUtils.deleteDirectory(new File("target/vcs/git"))
        FileUtils.deleteDirectory(new File("target/vcs/resource"))
        FileUtils.deleteDirectory(new File("target/vcs/repository"))
        FileUtils.deleteDirectory(new File("target/vcs/exchange"))
        vcsService.vcsManager = null
    }

    void testNotConfigured() {
        // verifies that the service is not valid, if plugin not specified
        mockConfig("")
        VcsService service = new VcsService()
        assertFalse(service.isValid())
        service.afterPropertiesSet()
        assertFalse(service.isValid())
        // verifies that the service is not valid, when config option is empty
        mockConfig('''jummp.vcs.pluginServiceName=""''')
        service = new VcsService()
        assertFalse(service.isValid())
        service.afterPropertiesSet()
        assertFalse(service.isValid())
        // verifies that the service is not valid, if not existing plugin is specified
        mockConfig('''jummp.vcs.pluginServiceName="novcs"''')
        service = new VcsService()
        assertFalse(service.isValid())
        service.afterPropertiesSet()
        assertFalse(service.isValid())
    }

    void testNoBackend(){
        // verifies that the service is not valid, if the backends are not configured correctly
        mockConfig('''jummp.vcs.pluginServiceName="svnService"''')
        VcsService service = new VcsService()
        assertFalse(service.isValid())
        service.afterPropertiesSet()
        assertFalse(service.isValid())
        mockConfig('''jummp.vcs.pluginServiceName="gitService"''')
        service = new VcsService()
        assertFalse(service.isValid())
        service.afterPropertiesSet()
        assertFalse(service.isValid())
    }

    void testGit() {
        // verifies that the service is valid, if git backend is configured correctly
        mockConfig('''
            jummp.vcs.pluginServiceName="gitService"
            jummp.plugins.git.enabled=true
            jummp.vcs.workingDirectory="target/vcs/git"
        ''')
        File gitDirectory = new File("target/vcs/git")
        FileRepositoryBuilder builder = new FileRepositoryBuilder()
        Repository repository = builder.setWorkTree(gitDirectory)
        .readEnvironment() // scan environment GIT_* variables
        .findGitDir() // scan up the file system tree
        .build()
        Git git = new Git(repository)
        git.init().setDirectory(gitDirectory).call()
        def contextControl = mockFor(org.springframework.context.ApplicationContext)
        contextControl.demand.getBean(1..1) {bean ->
            GitService gitService = new GitService()
            gitService.afterPropertiesSet()
            return gitService
        }
        ApplicationHolder.getApplication().mainContext = contextControl.createMock()
        VcsService service = new VcsService()
        assertFalse(service.isValid())
        service.afterPropertiesSet()
        assertTrue(service.isValid())
    }

    void testSvn() {
        // verifies that the service is valid, if svn backend is configured correctly
        mockConfig('''
            jummp.vcs.pluginServiceName="svnService"
            jummp.plugins.subversion.enabled=true
            jummp.plugins.subversion.localRepository="target/vcs/repository"
        ''')
        SVNRepositoryFactory.createLocalRepository(new File("target/vcs/repository"), true, false)
        def contextControl = mockFor(org.springframework.context.ApplicationContext)
        contextControl.demand.getBean(1..1) {bean ->
            SvnService svnService = new SvnService()
            svnService.afterPropertiesSet()
            return svnService
        }
        ApplicationHolder.getApplication().mainContext = contextControl.createMock()
        VcsService service = new VcsService()
        assertFalse(service.isValid())
        service.afterPropertiesSet()
        assertTrue(service.isValid())
    }

    void testUpdateFile() {
        assertFalse(vcsService.isValid())
        // first create a model
        Model model = new Model(name: "test", vcsIdentifier: "test.xml")
        Revision revision = new Revision(model: model, vcsId: "1", revisionNumber: 1, owner: User.findByUsername("testuser"), minorRevision: false, comment: "", uploadDate: new Date())
        assertTrue(revision.validate())
        model.addToRevisions(revision)
        assertTrue(model.validate())
        model.save()
        // without authentication it should fail
        authenticateAnonymous()
        shouldFail(AccessDeniedException) {
            vcsService.updateFile(model, null, null)
        }
        // as user we should get an access denied exception
        authenticateAsTestUser()
        shouldFail(AccessDeniedException) {
            vcsService.updateFile(model, null, null)
        }
        // as admin we should get an VcsException as the vcsService is not valid
        authenticateAsAdmin()
        shouldFail(VcsException) {
            vcsService.updateFile(model, null, null)
        }
        // now also the user should get a VcsException
        modelService.grantWriteAccess(model, User.findByUsername("testuser"))
        authenticateAsTestUser()
        shouldFail(VcsException) {
            vcsService.updateFile(model, null, null)
        }
        // create a git repository
        File importFile = new File("target/vcs/exchange/test.xml")
        FileUtils.touch(importFile)
        // setup VCS
        File clone = new File("target/vcs/git")
        clone.mkdirs()
        FileRepositoryBuilder builder = new FileRepositoryBuilder()
        Repository repository = builder.setWorkTree(clone)
        .readEnvironment() // scan environment GIT_* variables
        .findGitDir() // scan up the file system tree
        .build()
        Git git = new Git(repository)
        git.init().setDirectory(clone).call()
        GitService gitService = new GitService()
        mockConfig('''
            jummp.plugins.git.enabled=true
            jummp.vcs.workingDirectory="target/vcs/git"
            jummp.vcs.exchangeDirectory="target/vcs/exchange"
            ''')
        gitService.afterPropertiesSet()
        assertTrue(gitService.isValid())
        vcsService.vcsManager = gitService.vcsManager()
        assertTrue(vcsService.isValid())

        // file is not yet imported, so uploading should fail with a VcsException
        shouldFail(FileNotVersionedException) {
            vcsService.updateFile(model, importFile, null)
        }
        // import a file to the git repository, to make future updates possible
        gitService.vcsManager().importFile(importFile, "test.xml")
        // now we should be able to update the file
        File updateFile = new File("target/vcs/exchange/update.xml")
        updateFile.append("Test\n")
        String rev = vcsService.updateFile(model, updateFile, null)
        File gitFile = new File("target/vcs/git/test.xml")
        List<String> lines = gitFile.readLines()
        assertEquals(1, lines.size())
        assertEquals("Test", lines[0])
        // ensure the revision and commit message is correct
        ObjectId commit = repository.resolve(Constants.HEAD)
        RevWalk revWalk = new RevWalk(repository)
        RevCommit revCommit = revWalk.parseCommit(commit)
        assertEquals(commit.getName(), rev)
        // we passed null as commit message, so default should be used
        assertEquals("Update of test.xml", revCommit.getShortMessage())
        assertEquals("Update of test.xml", revCommit.getFullMessage())
        // try again with an empty commit message - should also be default message
        updateFile.append("Second Test\n")
        rev = vcsService.updateFile(model, updateFile, null)
        lines = gitFile.readLines()
        assertEquals(2, lines.size())
        assertEquals("Test", lines[0])
        assertEquals("Second Test", lines[1])
        repository = builder.setWorkTree(clone)
        .readEnvironment() // scan environment GIT_* variables
        .findGitDir() // scan up the file system tree
        .build()
        commit = repository.resolve(Constants.HEAD)
        revWalk = new RevWalk(repository)
        revCommit = revWalk.parseCommit(commit)
        assertEquals(commit.getName(), rev)
        assertEquals("Update of test.xml", revCommit.getShortMessage())
        assertEquals("Update of test.xml", revCommit.getFullMessage())
        // try with a custom commit message
        updateFile.append("Third Test\n")
        rev = vcsService.updateFile(model, updateFile, "Commit Message")
        lines = gitFile.readLines()
        assertEquals(3, lines.size())
        assertEquals("Test", lines[0])
        assertEquals("Second Test", lines[1])
        assertEquals("Third Test", lines[2])
        repository = builder.setWorkTree(clone)
        .readEnvironment() // scan environment GIT_* variables
        .findGitDir() // scan up the file system tree
        .build()
        commit = repository.resolve(Constants.HEAD)
        revWalk = new RevWalk(repository)
        revCommit = revWalk.parseCommit(commit)
        assertEquals(commit.getName(), rev)
        assertEquals("Commit Message", revCommit.getShortMessage())
        assertEquals("Commit Message", revCommit.getFullMessage())
        // and finally verify as admin
        authenticateAsAdmin()
        updateFile.append("Admin Test\n")
        rev = vcsService.updateFile(model, updateFile, "Admin Commit Message")
        lines = gitFile.readLines()
        assertEquals(4, lines.size())
        assertEquals("Test", lines[0])
        assertEquals("Second Test", lines[1])
        assertEquals("Third Test", lines[2])
        assertEquals("Admin Test", lines[3])
        repository = builder.setWorkTree(clone)
        .readEnvironment() // scan environment GIT_* variables
        .findGitDir() // scan up the file system tree
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
            vcsService.updateFile(model, updateFile, "Commit Message")
        }
    }
}
