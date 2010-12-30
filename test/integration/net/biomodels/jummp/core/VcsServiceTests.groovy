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

class VcsServiceTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
        mockLogging(VcsService, true)
        mockLogging(GitService, true)
        mockLogging(SvnService, true)
    }

    protected void tearDown() {
        super.tearDown()
        FileUtils.deleteDirectory(new File("target/vcs/git"))
        FileUtils.deleteDirectory(new File("target/vcs/resource"))
        FileUtils.deleteDirectory(new File("target/vcs/repository"))
    }

    void testNotConfigured() {
        // verifies that the service is not valid, if plugin not specified
        mockConfig("")
        VcsService service = new VcsService()
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
}
