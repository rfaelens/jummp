package net.biomodels.jummp.plugins.configuration

import grails.test.*
import org.apache.commons.io.FileUtils
import org.junit.After
import org.junit.Before

@TestFor(ConfigurationController)
class ConfigurationControllerTests {
    @Before
    void setUp() {
        // inject a ConfigurationService into the controller
        ConfigurationService service = new ConfigurationService()
        File configurationFile = new File("target/jummp.properties")
        FileUtils.touch(configurationFile)
        service.configurationFile = configurationFile
        service.afterPropertiesSet()
        controller.configurationService = service
    }
    @After
    void tearDown() {
        FileUtils.deleteQuietly(new File("target/jummp.properties"))
    }

    void testSaveLdap() {
        // test for incorrect command
        LdapCommand cmd = mockCommandObject(LdapCommand)
        cmd.validate()
        controller.saveLdap(cmd)
        assertEquals("/configuration/configuration", view)
        assertEquals("ldap", model.template)
        assertEquals(cmd, model.ldap)
        // test for correct command
        cmd = mockCommandObject(LdapCommand)
        cmd.ldapManagerDn = "user"
        cmd.ldapManagerPassword = "secure"
        cmd.ldapSearchBase = "search"
        cmd.ldapSearchFilter = "filter"
        cmd.ldapSearchSubtree = true
        cmd.ldapServer = "localhost"
        cmd.validate()
        controller.saveLdap(cmd)
        assertEquals("/configuration/saved", view)
        assertEquals("LDAP", model.module)
        LdapCommand saved = this.controller.configurationService.loadLdapConfiguration()
        assertTrue(saved.ldapSearchSubtree)
        assertEquals("filter", saved.ldapSearchFilter)
        assertEquals("search", saved.ldapSearchBase)
        assertEquals("secure", saved.ldapManagerPassword)
        assertEquals("localhost", saved.ldapServer)
        assertEquals("user", saved.ldapManagerDn)
    }

    void testSaveMysql() {
        // test for incorrect command
        MysqlCommand cmd = mockCommandObject(MysqlCommand)
        cmd.validate()
        controller.saveMysql(cmd)
        assertEquals("/configuration/configuration", view)
        assertEquals("mysql", model.template)
        assertEquals(cmd, model.mysql)
        // test for correct command
        cmd = mockCommandObject(MysqlCommand)
        cmd.database = "jummp"
        cmd.password = "secure"
        cmd.port = 3306
        cmd.server = "localhost"
        cmd.username = "user"
        cmd.validate()
        controller.saveMysql(cmd)
        assertEquals("/configuration/saved", view)
        assertEquals("MySQL", model.module)
        MysqlCommand saved = this.controller.configurationService.loadMysqlConfiguration()
        assertEquals(3306, saved.port)
        assertEquals("jummp", saved.database)
        assertEquals("secure", saved.password)
        assertEquals("localhost", saved.server)
        assertEquals("user", saved.username)

    }

    void testSaveRemote() {
        // test for incorrect command
        RemoteCommand cmd = mockCommandObject(RemoteCommand)
        cmd.validate()
        controller.saveRemote(cmd)
        assertEquals("/configuration/configuration", view)
        assertEquals("remote", model.template)
        assertEquals(cmd, model.remote)
        // test for correct command
        cmd = mockCommandObject(RemoteCommand)
        cmd.jummpRemote="jms"
        cmd.jummpExportDbus=false
        cmd.jummpExportJms=true
        cmd.validate()
        controller.saveRemote(cmd)
        assertEquals("/configuration/saved", view)
        assertEquals("Remote", model.module)
        RemoteCommand saved = this.controller.configurationService.loadRemoteConfiguration()
        assertEquals("jms", saved.jummpRemote)
        assertFalse(saved.jummpExportDbus)
        assertTrue(saved.jummpExportJms)
    }

    void testSaveServer() {
        // test for incorrect command
        ServerCommand cmd = mockCommandObject(ServerCommand)
        cmd.validate()
        controller.saveServer(cmd)
        assertEquals("/configuration/configuration", view)
        assertEquals("server", model.template)
        assertEquals(cmd, model.server)
        // test for correct command
        cmd = mockCommandObject(ServerCommand)
        cmd.url = "http://127.0.0.1:8080/jummp/"
        cmd.weburl = "http://127.0.0.1:8080/jummp-web-application/"
        cmd.validate()
        controller.saveServer(cmd)
        assertEquals("/configuration/saved", view)
        assertEquals("Server", model.module)
        ServerCommand saved = this.controller.configurationService.loadServerConfiguration()
        assertEquals("http://127.0.0.1:8080/jummp/", saved.url)
        assertEquals("http://127.0.0.1:8080/jummp-web-application/", saved.weburl)
    }

    void testSaveSvn() {
        SvnCommand cmd = mockCommandObject(SvnCommand)
        cmd.validate()
        controller.saveSvn(cmd)
        assertEquals("/configuration/configuration", view)
        assertEquals("svn", model.template)
        assertEquals(cmd, model.svn)
        // test for correct command
        cmd = mockCommandObject(SvnCommand)
        cmd.localRepository = "target"
        cmd.validate()
        controller.saveSvn(cmd)
        assertEquals("/configuration/saved", view)
        assertEquals("Subversion", model.module)
        SvnCommand saved = this.controller.configurationService.loadSvnConfiguration()
        assertEquals("target", saved.localRepository)
    }

    void testSaveVcs() {
        // test for incorrect command
        VcsCommand cmd = mockCommandObject(VcsCommand)
        cmd.validate()
        controller.saveVcs(cmd)
        assertEquals("/configuration/configuration", view)
        assertEquals("vcs", model.template)
        assertEquals(cmd, model.vcs)
        // test for correct command
        cmd = mockCommandObject(VcsCommand)
        cmd.vcs = "svn"
        cmd.exchangeDirectory = ""
        cmd.workingDirectory = ""
        cmd.validate()
        controller.saveVcs(cmd)
        assertEquals("/configuration/saved", view)
        assertEquals("Version Control System", model.module)
        VcsCommand saved = this.controller.configurationService.loadVcsConfiguration()
        assertEquals("svn", saved.vcs)
    }
}
