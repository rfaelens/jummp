package net.biomodels.jummp.plugins.configuration

import grails.test.*
import org.apache.commons.io.FileUtils
import org.junit.After
import org.junit.Before
import grails.test.mixin.support.GrailsUnitTestMixin

@TestFor(ConfigurationController)
class ConfigurationControllerTests extends GrailsUnitTestMixin {
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
        grailsApplication.config.jummp.security.ldap.server = "localhost"
        grailsApplication.config.jummp.security.ldap.managerDn = "user"
        grailsApplication.config.jummp.security.ldap.managerPw = "secure"
        grailsApplication.config.jummp.security.ldap.search.base = "search"
        grailsApplication.config.jummp.security.ldap.search.filter = "filter"
        grailsApplication.config.jummp.security.ldap.search.subTree = "true"
        LdapCommand saved = this.controller.configurationService.loadLdapConfiguration()
        assertTrue(saved.ldapSearchSubtree)
        assertEquals("filter", saved.ldapSearchFilter)
        assertEquals("search", saved.ldapSearchBase)
        assertEquals("secure", saved.ldapManagerPassword)
        assertEquals("localhost", saved.ldapServer)
        assertEquals("user", saved.ldapManagerDn)
    }

    void testSaveDatabase() {
        // test for incorrect command
        DatabaseCommand cmd = mockCommandObject(DatabaseCommand)
        cmd.validate()
        controller.saveDatabase(cmd)
        assertEquals("/configuration/configuration", view)
        assertEquals("database", model.template)
        assertEquals(cmd, model.database)
        // test for correct command
        cmd = mockCommandObject(DatabaseCommand)
        cmd.type = "MYSQL"
        cmd.database = "jummp"
        cmd.password = "secure"
        cmd.port = 3306
        cmd.server = "localhost"
        cmd.username = "user"
        cmd.validate()
        controller.saveDatabase(cmd)
        assertEquals("/configuration/saved", view)
        assertEquals("Database", model.module)
        grailsApplication.config.jummp.database.type = "MYSQL"
        grailsApplication.config.jummp.database.server = "localhost"
        grailsApplication.config.jummp.database.port = 3306
        grailsApplication.config.jummp.database.database = "jummp"
        grailsApplication.config.jummp.database.username = "user"
        grailsApplication.config.jummp.database.password = "secure"
        DatabaseCommand saved = this.controller.configurationService.loadDatabaseConfiguration()
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
        grailsApplication.config.jummp.remote = "jms"
        grailsApplication.config.jummp.export.dbus = "false"
        grailsApplication.config.jummp.export.jms = "true"
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
        cmd.protectEverything = false
        cmd.validate()
        controller.saveServer(cmd)
        assertEquals("/configuration/saved", view)
        assertEquals("Server", model.module)
        grailsApplication.config.jummp.server.url = "http://127.0.0.1:8080/jummp/"
        grailsApplication.config.jummp.server.web.url = "http://127.0.0.1:8080/jummp-web-application/"
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
        grailsApplication.config.jummp.plugins.subversion.localRepository = "target"
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
        grailsApplication.config.jummp.vcs.plugin = "subversion"
        grailsApplication.config.jummp.vcs.exchangeDirectory = ""
        grailsApplication.config.jummp.vcs.workingDirectory = ""
        VcsCommand saved = this.controller.configurationService.loadVcsConfiguration()
        assertEquals("svn", saved.vcs)
    }

    void testSaveBranding() {
        // test for incorrect command
        BrandingCommand cmd = mockCommandObject(BrandingCommand)
        cmd.validate()
        controller.saveBranding(cmd)
        assertEquals("/configuration/configuration", view)
        assertEquals("branding", model.template)
        assertEquals(cmd, model.branding)
        // test for correct command
        cmd = mockCommandObject(BrandingCommand)
        cmd.internalColor = "#FFFFFF"
        cmd.externalColor = "#000000"
        cmd.validate()
        controller.saveBranding(cmd)
        assertEquals("/configuration/saved", view)
        assertEquals("Select Branding", model.module)
        grailsApplication.config.jummp.branding.internalColor = "#FFFFFF"
        grailsApplication.config.jummp.branding.externalColor = "#000000"
        BrandingCommand saved = this.controller.configurationService.loadBrandingConfiguration()
        assertEquals("#FFFFFF", saved.internalColor)
        assertEquals("#000000", saved.externalColor)
    }

    void testSaveCms() {
        // test for incorrect command
        CmsCommand cmd = mockCommandObject(CmsCommand)
        cmd.validate()
        controller.saveCms(cmd)
        assertEquals("/configuration/configuration", view)
        assertEquals("cms", model.template)
        assertEquals(cmd, model.cms)

        // test for correct command: blanc
        cmd = mockCommandObject(CmsCommand)
        cmd.policyFile = ""
        cmd.validate()
        controller.saveCms(cmd)
        assertEquals("/configuration/saved", view)
        assertEquals("Content Management System", model.module)
        grailsApplication.config.jummp.cms.policyFile = ""
        CmsCommand saved = this.controller.configurationService.loadCmsConfiguration()
        assertEquals("", saved.policyFile)

        //test for correct command: value
        cmd = mockCommandObject(CmsCommand)
        cmd.policyFile = "/file/"
        cmd.validate()
        controller.saveCms(cmd)
        assertEquals("/configuration/saved", view)
        assertEquals("Content Management System", model.module)
        grailsApplication.config.jummp.cms.policyFile = "/file/"
        saved = this.controller.configurationService.loadCmsConfiguration()
        assertEquals("/file/", saved.policyFile)
    }
}
