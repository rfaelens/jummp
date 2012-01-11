package net.biomodels.jummp.plugins.subversion

import grails.test.*
import net.biomodels.jummp.core.vcs.VcsNotInitedException
import org.apache.commons.io.FileUtils
import javax.servlet.ServletContext
import org.tmatesoft.svn.core.io.SVNRepositoryFactory
import net.biomodels.jummp.core.vcs.VcsManager

class SvnServiceTests extends GrailsUnitTestCase {
    def grailsApplication
    protected void setUp() {
        super.setUp()
        mockLogging(SvnManagerFactory)
    }

    protected void tearDown() {
        super.tearDown()
        FileUtils.deleteDirectory(new File("target/vcs/exchange"))
        FileUtils.deleteDirectory(new File("target/vcs/svn"))
        FileUtils.deleteDirectory(new File("target/vcs/resource"))
        FileUtils.deleteDirectory(new File("target/vcs/repository"))
    }

    void testDisabled() {
        // verifies that SvnManagerFactory does not get enabled if there is no config
        grailsApplication.config.jummp.plugins.subversion = [:]
        SvnManagerFactory svn = new SvnManagerFactory()
        svn.grailsApplication = grailsApplication
        shouldFail(VcsNotInitedException) {
            svn.getInstance()
        }
        shouldFail(VcsNotInitedException) {
            svn.getInstance()
        }
        // verifies that SvnManagerFactory does not get enabled if disabled in config
        grailsApplication.config.jummp.plugins.subversion.enabled = false
        svn = new SvnManagerFactory()
        svn.grailsApplication = grailsApplication
        shouldFail(VcsNotInitedException) {
            svn.getInstance()
        }
        shouldFail(VcsNotInitedException) {
            svn.getInstance()
        }
        // verifies that SvnManagerFactory does not get enabled if localRepository is not set
        grailsApplication.config.jummp.plugins.subversion.enabled = true
        svn = new SvnManagerFactory()
        svn.grailsApplication = grailsApplication
        shouldFail(VcsNotInitedException) {
            svn.getInstance()
        }
        shouldFail(VcsNotInitedException) {
            svn.getInstance()
        }
    }

    void testResourceDirectories() {
        // verifies that the exchange and working directories in resource are created
        grailsApplication.config.jummp.plugins.subversion.enabled = true
        grailsApplication.config.jummp.plugins.subversion.localRepository = "target/vcs/repository"
        // will be used for two calls to setup SvnManagerFactory - therefore 4 mock calls expected
        def contextControl = mockFor(ServletContext)
        contextControl.demand.getRealPath(4..4) {path ->
            return "target/vcs" + path
        }
        File exchangeDirectory = new File("target/vcs/resource/exchangeDir")
        File workingDirectory = new File("target/vcs/resource/workingDir")
        assertFalse(exchangeDirectory.exists())
        assertFalse(workingDirectory.exists())
        SvnManagerFactory svn = new SvnManagerFactory()
        svn.grailsApplication = grailsApplication
        def servletContext = (ServletContext)contextControl.createMock()
        svn.servletContext = servletContext
        shouldFail(VcsNotInitedException) {
            svn.getInstance()
        }
        assertTrue(exchangeDirectory.exists())
        assertTrue(exchangeDirectory.isDirectory())
        assertTrue(workingDirectory.exists())
        assertTrue(workingDirectory.isDirectory())
        // verify that the working directory gets cleaned
        FileUtils.touch(new File("target/vcs/resource/workingDir/test"))
        assertLength(1, workingDirectory.list())
        svn = new SvnManagerFactory()
        svn.grailsApplication = grailsApplication
        svn.servletContext = servletContext
        shouldFail(VcsNotInitedException) {
            svn.getInstance()
        }
        assertLength(0, workingDirectory.list())
        contextControl.verify()
    }

    void testNormalDirectories() {
        // verifies that directories are not created in resource when path passed as config options
        def contextControl = mockFor(ServletContext)
        contextControl.demand.getRealPath(4..4) {path ->
            return "target/vcs" + path
        }
        // exchange directory in resources, working directory external
        grailsApplication.config.jummp.plugins.subversion.enabled = true
        grailsApplication.config.jummp.plugins.subversion.localRepository = "target/vcs/repository"
        grailsApplication.config.jummp.vcs.workingDirectory = "target/vcs/svn"
        File resourceExchangeDirectory = new File("target/vcs/resource/exchangeDir")
        File workingDirectory = new File("target/vcs/svn")
        File resourceWorkingDirectory = new File("target/vcs/resource/workingDir")
        assertFalse(resourceExchangeDirectory.exists())
        assertFalse(workingDirectory.exists())
        assertFalse(resourceWorkingDirectory.exists())
        SvnManagerFactory svn = new SvnManagerFactory()
        svn.grailsApplication = grailsApplication
        def servletContext = (ServletContext)contextControl.createMock()
        svn.servletContext = servletContext
        shouldFail(VcsNotInitedException) {
            svn.getInstance()
        }
        assertTrue(resourceExchangeDirectory.exists())
        assertTrue(resourceExchangeDirectory.isDirectory())
        assertTrue(workingDirectory.exists())
        assertTrue(workingDirectory.isDirectory())
        assertFalse(resourceWorkingDirectory.exists())
        // test that working directory gets cleaned
        FileUtils.touch(new File("target/vcs/svn/test"))
        assertLength(1, workingDirectory.list())
        svn = new SvnManagerFactory()
        svn.grailsApplication = grailsApplication
        svn.servletContext = servletContext
        shouldFail(VcsNotInitedException) {
            svn.getInstance()
        }
        assertLength(0, workingDirectory.list())
        assertFalse(resourceWorkingDirectory.exists())
        // verify other way around: working directory in resource, exchange not in resource
        grailsApplication.config.jummp.plugins.subversion.enabled = true
        grailsApplication.config.jummp.plugins.subversion.localRepository = "target/vcs/repository"
        grailsApplication.config.jummp.vcs.exchangeDirectory = "target/vcs/exchange"
        grailsApplication.config.jummp.vcs.workingDirectory = [:]
        File exchangeDirectory = new File("target/vcs/exchange")
        FileUtils.deleteDirectory(resourceExchangeDirectory)
        assertFalse(exchangeDirectory.exists())
        assertFalse(resourceWorkingDirectory.exists())
        svn = new SvnManagerFactory()
        svn.grailsApplication = grailsApplication
        svn.servletContext = servletContext
        shouldFail(VcsNotInitedException) {
            svn.getInstance()
        }
        assertTrue(exchangeDirectory.exists())
        assertTrue(exchangeDirectory.isDirectory())
        assertTrue(resourceWorkingDirectory.exists())
        assertTrue(resourceWorkingDirectory.isDirectory())
        // test that working directory gets cleaned
        FileUtils.touch(new File("target/vcs/resource/workingDir/test"))
        assertLength(1, resourceWorkingDirectory.list())
        svn = new SvnManagerFactory()
        svn.grailsApplication = grailsApplication
        svn.servletContext = servletContext
        shouldFail(VcsNotInitedException) {
            svn.getInstance()
        }
        assertLength(0, resourceWorkingDirectory.list())
        contextControl.verify()
        // verify that exchange and working directory get created, if passed in
        grailsApplication.config.jummp.plugins.subversion.enabled = true
        grailsApplication.config.jummp.plugins.subversion.localRepository = "target/vcs/repository"
        grailsApplication.config.jummp.vcs.exchangeDirectory = "target/vcs/exchange"
        grailsApplication.config.jummp.vcs.workingDirectory = "target/vcs/svn"
        FileUtils.deleteDirectory(resourceWorkingDirectory)
        FileUtils.deleteDirectory(resourceExchangeDirectory)
        FileUtils.deleteDirectory(exchangeDirectory)
        FileUtils.deleteDirectory(workingDirectory)
        assertFalse(resourceWorkingDirectory.exists())
        assertFalse(resourceExchangeDirectory.exists())
        assertFalse(exchangeDirectory.exists())
        assertFalse(workingDirectory.exists())
        svn = new SvnManagerFactory()
        svn.grailsApplication = grailsApplication
        svn.servletContext = servletContext
        shouldFail(VcsNotInitedException) {
            svn.getInstance()
        }
        assertFalse(resourceWorkingDirectory.exists())
        assertFalse(resourceExchangeDirectory.exists())
        assertTrue(exchangeDirectory.exists())
        assertTrue(workingDirectory.exists())
        shouldFail(VcsNotInitedException) {
            svn.getInstance()
        }
        // test that working directory gets cleaned
        FileUtils.touch(new File("target/vcs/svn/test"))
        assertLength(1, workingDirectory.list())
        svn = new SvnManagerFactory()
        svn.grailsApplication = grailsApplication
        svn.servletContext = servletContext
        shouldFail(VcsNotInitedException) {
            svn.getInstance()
        }
        assertLength(0, workingDirectory.list())
        shouldFail(VcsNotInitedException) {
            svn.getInstance()
        }
    }

    void testCreateManager() {
        // verifies that SvnManager gets created if correct values are passed in
        def contextControl = mockFor(ServletContext)
        contextControl.demand.getRealPath(2..2) {path ->
            return "target/vcs" + path
        }
        SVNRepositoryFactory.createLocalRepository(new File("target/vcs/repository"), true, false)
        // first test with resource directories
        grailsApplication.config.jummp.plugins.subversion.enabled = true
        grailsApplication.config.jummp.plugins.subversion.localRepository = "target/vcs/repository"
        grailsApplication.config.jummp.vcs.workingDirectory = [:]
        File resourceWorkingDirectory = new File("target/vcs/resource/workingDir")
        assertFalse(resourceWorkingDirectory.exists())
        SvnManagerFactory svn = new SvnManagerFactory()
        svn.grailsApplication = grailsApplication
        svn.servletContext = (ServletContext)contextControl.createMock()
        VcsManager manager = svn.getInstance()
        assertNotNull(manager)
        assertTrue(manager instanceof SvnManager)
        assertTrue(resourceWorkingDirectory.exists())
        assertLength(1, resourceWorkingDirectory.list())
        assertEquals(".svn", resourceWorkingDirectory.list()[0])
        // test with configured directory
        grailsApplication.config.jummp.plugins.subversion.enabled = true
        grailsApplication.config.jummp.plugins.subversion.localRepository = "target/vcs/repository"
        grailsApplication.config.jummp.vcs.workingDirectory = "target/vcs/svn"
        grailsApplication.config.jummp.vcs.exchangeDirectory = "target/vcs/exchange"
        File workingDirectory = new File("target/vcs/svn")
        assertFalse(workingDirectory.exists())
        svn = new SvnManagerFactory()
        svn.grailsApplication = grailsApplication
        svn.getInstance()
        manager = svn.getInstance()
        assertNotNull(manager)
        assertTrue(manager instanceof SvnManager)
        assertTrue(workingDirectory.exists())
        assertLength(1, workingDirectory.list())
        assertEquals(".svn", workingDirectory.list()[0])
    }
}
