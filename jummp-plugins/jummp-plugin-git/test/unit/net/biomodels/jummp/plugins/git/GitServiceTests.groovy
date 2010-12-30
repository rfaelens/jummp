package net.biomodels.jummp.plugins.git

import grails.test.*
import javax.servlet.ServletContext
import net.biomodels.jummp.core.vcs.VcsManager
import net.biomodels.jummp.core.vcs.VcsNotInitedException
import org.apache.commons.io.FileUtils
import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

class GitServiceTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
        mockLogging(GitService)
    }

    protected void tearDown() {
        super.tearDown()
        FileUtils.deleteDirectory(new File("target/vcs/exchange"))
        FileUtils.deleteDirectory(new File("target/vcs/git"))
        FileUtils.deleteDirectory(new File("target/vcs/resource"))
    }

    void testDisabled() {
        // verifies that GitService does not get enabled if there is no config
        mockConfig("")
        GitService git = new GitService()
        git.afterPropertiesSet()
        assertFalse(git.isValid())
        shouldFail(VcsNotInitedException) {
            git.vcsManager()
        }
        // verifies that GitService does not get enabled if disabled in config
        mockConfig("jummp.plugins.git.enabled=false")
        git = new GitService()
        git.afterPropertiesSet()
        assertFalse(git.isValid())
        shouldFail(VcsNotInitedException) {
            git.vcsManager()
        }
    }

    void testDirectories() {
        // verifies that GitService does not get enabled if no git directory is passed
        mockConfig('''
            jummp.plugins.git.enabled=true
        ''')
        GitService git = new GitService()
        git.afterPropertiesSet()
        assertFalse(git.isValid())
        shouldFail(VcsNotInitedException) {
            git.vcsManager()
        }
        // verifies that GitService creates exchangeDirectory if passed in and does not get enabled
        // if workingDirectory is not a git directory
        mockConfig('''
            jummp.plugins.git.enabled=true
            jummp.vcs.workingDirectory="target/vcs/git"
            jummp.vcs.exchangeDirectory="target/vcs/exchange"
        ''')
        File exchangeDirectory = new File("target/vcs/exchange")
        File gitDirectory = new File("target/vcs/git")
        gitDirectory.mkdirs()
        assertTrue(gitDirectory.isDirectory())
        assertLength(0, gitDirectory.list())
        assertFalse(exchangeDirectory.exists())
        git = new GitService()
        git.afterPropertiesSet()
        assertTrue(exchangeDirectory.exists())
        assertTrue(exchangeDirectory.isDirectory())
        assertLength(0, exchangeDirectory.list())
        assertFalse(git.isValid())
        shouldFail(VcsNotInitedException) {
            git.vcsManager()
        }
        // verifies that GitService creates exchangeDirectory in resources if exchange directory is not set
        // git directory is not valid, so GitService won't be enabled
        mockConfig('''
            jummp.plugins.git.enabled=true
            jummp.vcs.workingDirectory="target/vcs/git"
        ''')
        assertTrue(gitDirectory.isDirectory())
        assertLength(0, gitDirectory.list())
        def contextControl = mockFor(ServletContext)
        contextControl.demand.getRealPath(1..1) {path ->
            return "target/vcs" + path
        }
        ServletContextHolder.servletContext = (ServletContext)contextControl.createMock()
        File resourceDirectory = new File("target/vcs/resource/exchangeDir")
        assertFalse(resourceDirectory.exists())
        git = new GitService()
        git.afterPropertiesSet()
        assertTrue(resourceDirectory.exists())
        assertTrue(resourceDirectory.isDirectory())
        assertFalse(git.isValid())
        contextControl.verify()
        shouldFail(VcsNotInitedException) {
            git.vcsManager()
        }
    }

    void testCreateManager() {
        // verifies the setups which should return a working GitManager
        mockConfig('''
            jummp.plugins.git.enabled=true
            jummp.vcs.workingDirectory="target/vcs/git"
        ''')
        FileRepositoryBuilder builder = new FileRepositoryBuilder()
        File gitDirectory = new File("target/vcs/git")
        gitDirectory.mkdirs()
        Repository repository = builder.setWorkTree(gitDirectory)
        .readEnvironment() // scan environment GIT_* variables
        .findGitDir() // scan up the file system tree
        .build()
        Git git = new Git(repository)
        git.init().setDirectory(gitDirectory).call()
        assertLength(1, gitDirectory.list())

        def contextControl = mockFor(ServletContext)
        contextControl.demand.getRealPath(1..1) {path ->
            return "target/vcs" + path
        }
        ServletContextHolder.servletContext = (ServletContext)contextControl.createMock()
        File resourceDirectory = new File("target/vcs/resource/exchangeDir")
        assertFalse(resourceDirectory.exists())
        GitService service = new GitService()
        service.afterPropertiesSet()
        assertTrue(resourceDirectory.exists())
        assertTrue(resourceDirectory.isDirectory())
        contextControl.verify()
        VcsManager manager = service.vcsManager()
        assertTrue(service.isValid())
        assertNotNull(manager)
        assertTrue(manager instanceof GitManager)

        // verify with existing exchange directory
        mockConfig('''
            jummp.plugins.git.enabled=true
            jummp.vcs.workingDirectory="target/vcs/git"
            jummp.vcs.exchangeDirectory="target/vcs/exchange"
        ''')
        File exchangeDirectory = new File("target/vcs/exchange")
        assertFalse(exchangeDirectory.exists())
        service = new GitService()
        service.afterPropertiesSet()
        assertTrue(exchangeDirectory.exists())
        assertTrue(exchangeDirectory.isDirectory())
        manager = service.vcsManager()
        assertTrue(service.isValid())
        assertNotNull(manager)
        assertTrue(manager instanceof GitManager)
    }
}
