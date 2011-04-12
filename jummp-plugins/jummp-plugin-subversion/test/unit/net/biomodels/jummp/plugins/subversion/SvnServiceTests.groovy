package net.biomodels.jummp.plugins.subversion

import grails.test.*
import net.biomodels.jummp.core.vcs.VcsNotInitedException
import org.apache.commons.io.FileUtils
import javax.servlet.ServletContext
import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.tmatesoft.svn.core.io.SVNRepositoryFactory
import net.biomodels.jummp.core.vcs.VcsManager

class SvnServiceTests extends GrailsUnitTestCase {
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
        mockConfig("")
        SvnManagerFactory svn = new SvnManagerFactory()
        shouldFail(VcsNotInitedException) {
            svn.getInstance()
        }
        assertFalse(svn.isValid())
        shouldFail(VcsNotInitedException) {
            svn.vcsManager()
        }
        // verifies that SvnManagerFactory does not get enabled if disabled in config
        mockConfig("jummp.plugins.subversion.enabled=false")
        svn = new SvnManagerFactory()
        shouldFail(VcsNotInitedException) {
            svn.getInstance()
        }
        assertFalse(svn.isValid())
        shouldFail(VcsNotInitedException) {
            svn.vcsManager()
        }
        // verifies that SvnManagerFactory does not get enabled if localRepository is not set
        mockConfig('''jummp.plugins.subversion.enabled=true''')
        svn = new SvnManagerFactory()
        shouldFail(VcsNotInitedException) {
            svn.getInstance()
        }
        assertFalse(svn.isValid())
        shouldFail(VcsNotInitedException) {
            svn.vcsManager()
        }
    }

    void testResourceDirectories() {
        // verifies that the exchange and working directories in resource are created
        mockConfig('''
            jummp.plugins.subversion.enabled=true
            jummp.plugins.subversion.localRepository="target/vcs/repository"
        ''')
        // will be used for two calls to setup SvnManagerFactory - therefore 4 mock calls expected
        def contextControl = mockFor(ServletContext)
        contextControl.demand.getRealPath(4..4) {path ->
            return "target/vcs" + path
        }
        ServletContextHolder.servletContext = (ServletContext)contextControl.createMock()
        File exchangeDirectory = new File("target/vcs/resource/exchangeDir")
        File workingDirectory = new File("target/vcs/resource/workingDir")
        assertFalse(exchangeDirectory.exists())
        assertFalse(workingDirectory.exists())
        SvnManagerFactory svn = new SvnManagerFactory()
        shouldFail(VcsNotInitedException) {
            svn.getInstance()
        }
        assertTrue(exchangeDirectory.exists())
        assertTrue(exchangeDirectory.isDirectory())
        assertTrue(workingDirectory.exists())
        assertTrue(workingDirectory.isDirectory())
        assertFalse(svn.isValid())
        shouldFail(VcsNotInitedException) {
            svn.vcsManager()
        }
        // verify that the working directory gets cleaned
        FileUtils.touch(new File("target/vcs/resource/workingDir/test"))
        assertLength(1, workingDirectory.list())
        svn = new SvnManagerFactory()
        shouldFail(VcsNotInitedException) {
            svn.getInstance()
        }
        assertLength(0, workingDirectory.list())
        assertFalse(svn.isValid())
        shouldFail(VcsNotInitedException) {
            svn.vcsManager()
        }
        contextControl.verify()
    }

    void testNormalDirectories() {
        // verifies that directories are not created in resource when path passed as config options
        def contextControl = mockFor(ServletContext)
        contextControl.demand.getRealPath(4..4) {path ->
            return "target/vcs" + path
        }
        ServletContextHolder.servletContext = (ServletContext)contextControl.createMock()
        // exchange directory in resources, working directory external
        mockConfig('''
            jummp.plugins.subversion.enabled=true
            jummp.plugins.subversion.localRepository="target/vcs/repository"
            jummp.vcs.workingDirectory="target/vcs/svn"
        ''')
        File resourceExchangeDirectory = new File("target/vcs/resource/exchangeDir")
        File workingDirectory = new File("target/vcs/svn")
        File resourceWorkingDirectory = new File("target/vcs/resource/workingDir")
        assertFalse(resourceExchangeDirectory.exists())
        assertFalse(workingDirectory.exists())
        assertFalse(resourceWorkingDirectory.exists())
        SvnManagerFactory svn = new SvnManagerFactory()
        shouldFail(VcsNotInitedException) {
            svn.getInstance()
        }
        assertTrue(resourceExchangeDirectory.exists())
        assertTrue(resourceExchangeDirectory.isDirectory())
        assertTrue(workingDirectory.exists())
        assertTrue(workingDirectory.isDirectory())
        assertFalse(resourceWorkingDirectory.exists())
        assertFalse(svn.isValid())
        shouldFail(VcsNotInitedException) {
            svn.vcsManager()
        }
        // test that working directory gets cleaned
        FileUtils.touch(new File("target/vcs/svn/test"))
        assertLength(1, workingDirectory.list())
        svn = new SvnManagerFactory()
        shouldFail(VcsNotInitedException) {
            svn.getInstance()
        }
        assertLength(0, workingDirectory.list())
        assertFalse(resourceWorkingDirectory.exists())
        assertFalse(svn.isValid())
        shouldFail(VcsNotInitedException) {
            svn.vcsManager()
        }
        // verify other way around: working directory in resource, exchange not in resource
        mockConfig('''
            jummp.plugins.subversion.enabled=true
            jummp.plugins.subversion.localRepository="target/vcs/repository"
            jummp.vcs.exchangeDirectory="target/vcs/exchange"
        ''')
        File exchangeDirectory = new File("target/vcs/exchange")
        FileUtils.deleteDirectory(resourceExchangeDirectory)
        assertFalse(exchangeDirectory.exists())
        assertFalse(resourceWorkingDirectory.exists())
        svn = new SvnManagerFactory()
        shouldFail(VcsNotInitedException) {
            svn.getInstance()
        }
        assertTrue(exchangeDirectory.exists())
        assertTrue(exchangeDirectory.isDirectory())
        assertTrue(resourceWorkingDirectory.exists())
        assertTrue(resourceWorkingDirectory.isDirectory())
        assertFalse(svn.isValid())
        shouldFail(VcsNotInitedException) {
            svn.vcsManager()
        }
        // test that working directory gets cleaned
        FileUtils.touch(new File("target/vcs/resource/workingDir/test"))
        assertLength(1, resourceWorkingDirectory.list())
        svn = new SvnManagerFactory()
        shouldFail(VcsNotInitedException) {
            svn.getInstance()
        }
        assertLength(0, resourceWorkingDirectory.list())
        assertFalse(svn.isValid())
        shouldFail(VcsNotInitedException) {
            svn.vcsManager()
        }
        contextControl.verify()
        // verify that exchange and working directory get created, if passed in
        mockConfig('''
            jummp.plugins.subversion.enabled=true
            jummp.plugins.subversion.localRepository="target/vcs/repository"
            jummp.vcs.exchangeDirectory="target/vcs/exchange"
            jummp.vcs.workingDirectory="target/vcs/svn"
        ''')
        FileUtils.deleteDirectory(resourceWorkingDirectory)
        FileUtils.deleteDirectory(resourceExchangeDirectory)
        FileUtils.deleteDirectory(exchangeDirectory)
        FileUtils.deleteDirectory(workingDirectory)
        assertFalse(resourceWorkingDirectory.exists())
        assertFalse(resourceExchangeDirectory.exists())
        assertFalse(exchangeDirectory.exists())
        assertFalse(workingDirectory.exists())
        svn = new SvnManagerFactory()
        shouldFail(VcsNotInitedException) {
            svn.getInstance()
        }
        assertFalse(resourceWorkingDirectory.exists())
        assertFalse(resourceExchangeDirectory.exists())
        assertTrue(exchangeDirectory.exists())
        assertTrue(workingDirectory.exists())
        assertFalse(svn.isValid())
        shouldFail(VcsNotInitedException) {
            svn.vcsManager()
        }
        // test that working directory gets cleaned
        FileUtils.touch(new File("target/vcs/svn/test"))
        assertLength(1, workingDirectory.list())
        svn = new SvnManagerFactory()
        shouldFail(VcsNotInitedException) {
            svn.getInstance()
        }
        assertLength(0, workingDirectory.list())
        assertFalse(svn.isValid())
        shouldFail(VcsNotInitedException) {
            svn.vcsManager()
        }
    }

    void testCreateManager() {
        // verifies that SvnManager gets created if correct values are passed in
        def contextControl = mockFor(ServletContext)
        contextControl.demand.getRealPath(2..2) {path ->
            return "target/vcs" + path
        }
        ServletContextHolder.servletContext = (ServletContext)contextControl.createMock()
        SVNRepositoryFactory.createLocalRepository(new File("target/vcs/repository"), true, false)
        // first test with resource directories
        mockConfig('''
            jummp.plugins.subversion.enabled=true
            jummp.plugins.subversion.localRepository="target/vcs/repository"
        ''')
        File resourceWorkingDirectory = new File("target/vcs/resource/workingDir")
        assertFalse(resourceWorkingDirectory.exists())
        SvnManagerFactory svn = new SvnManagerFactory()
        svn.getInstance()
        VcsManager manager = svn.vcsManager()
        assertNotNull(manager)
        assertTrue(svn.isValid())
        assertTrue(manager instanceof SvnManager)
        assertTrue(resourceWorkingDirectory.exists())
        assertLength(1, resourceWorkingDirectory.list())
        assertEquals(".svn", resourceWorkingDirectory.list()[0])
        // test with configured directory
        mockConfig('''
            jummp.plugins.subversion.enabled=true
            jummp.plugins.subversion.localRepository="target/vcs/repository"
            jummp.vcs.workingDirectory="target/vcs/svn"
            jummp.vcs.exchangeDirectory="target/vcs/exchange"
        ''')
        File workingDirectory = new File("target/vcs/svn")
        assertFalse(workingDirectory.exists())
        svn = new SvnManagerFactory()
        svn.getInstance()
        manager = svn.vcsManager()
        assertNotNull(manager)
        assertTrue(svn.isValid())
        assertTrue(manager instanceof SvnManager)
        assertTrue(workingDirectory.exists())
        assertLength(1, workingDirectory.list())
        assertEquals(".svn", workingDirectory.list()[0])
    }
}
