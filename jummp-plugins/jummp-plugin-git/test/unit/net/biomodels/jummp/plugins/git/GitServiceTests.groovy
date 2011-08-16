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
        mockLogging(GitManagerFactory)
    }

    protected void tearDown() {
        super.tearDown()
        FileUtils.deleteDirectory(new File("target/vcs/exchange"))
        FileUtils.deleteDirectory(new File("target/vcs/git"))
        FileUtils.deleteDirectory(new File("target/vcs/resource"))
    }

    void testDisabled() {
        // verifies that GitManagerFactory does not get enabled if there is no config
        mockConfig("")
        GitManagerFactory git = new GitManagerFactory()
        shouldFail(VcsNotInitedException) {
            git.getInstance()
        }
        shouldFail(VcsNotInitedException) {
            git.getInstance()
        }
        // verifies that GitManagerFactory does not get enabled if disabled in config
        mockConfig("jummp.plugins.git.enabled=false")
        git = new GitManagerFactory()
        shouldFail(VcsNotInitedException) {
            git.getInstance()
        }
        shouldFail(VcsNotInitedException) {
            git.getInstance()
        }
    }

    void testDirectories() {
        // verifies that GitManagerFactory does not get enabled if no git directory is passed
        mockConfig('''
            jummp.plugins.git.enabled=true
        ''')
        GitManagerFactory git = new GitManagerFactory()
        shouldFail(VcsNotInitedException) {
            git.getInstance()
        }
        shouldFail(VcsNotInitedException) {
            git.getInstance()
        }
        // verifies that GitManagerFactory creates exchangeDirectory if passed in and does not get enabled
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
        git = new GitManagerFactory()
        shouldFail(VcsNotInitedException) {
            git.getInstance()
        }
        assertTrue(exchangeDirectory.exists())
        assertTrue(exchangeDirectory.isDirectory())
        assertLength(0, exchangeDirectory.list())
        shouldFail(VcsNotInitedException) {
            git.getInstance()
        }
        // verifies that GitManagerFactory creates exchangeDirectory in resources if exchange directory is not set
        // git directory is not valid, so GitManagerFactory won't be enabled
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
        File resourceDirectory = new File("target/vcs/resource/exchangeDir")
        assertFalse(resourceDirectory.exists())
        git = new GitManagerFactory()
        git.servletContext = (ServletContext)contextControl.createMock()
        shouldFail(VcsNotInitedException) {
            git.getInstance()
        }
        assertTrue(resourceDirectory.exists())
        assertTrue(resourceDirectory.isDirectory())
        contextControl.verify()
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
        File resourceDirectory = new File("target/vcs/resource/exchangeDir")
        assertFalse(resourceDirectory.exists())
        GitManagerFactory service = new GitManagerFactory()
        service.servletContext = (ServletContext)contextControl.createMock()
        service.getInstance()
        assertTrue(resourceDirectory.exists())
        assertTrue(resourceDirectory.isDirectory())
        contextControl.verify()
        VcsManager manager = service.getInstance()
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
        service = new GitManagerFactory()
        service.getInstance()
        assertTrue(exchangeDirectory.exists())
        assertTrue(exchangeDirectory.isDirectory())
        manager = service.getInstance()
        assertNotNull(manager)
        assertTrue(manager instanceof GitManager)
    }
}
