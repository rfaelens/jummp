package net.biomodels.jummp.plugins.configuration

import grails.test.*
import org.apache.commons.io.FileUtils

class ConfigurationControllerTests extends ControllerUnitTestCase {
    protected void setUp() {
        super.setUp()
        // inject a ConfigurationService into the controller
        ConfigurationService service = new ConfigurationService()
        File configurationFile = new File("target/jummp.properties")
        FileUtils.touch(configurationFile)
        service.configurationFile = configurationFile
        service.afterPropertiesSet()
        this.controller.configurationService = service
    }

    protected void tearDown() {
        super.tearDown()
        FileUtils.deleteQuietly(new File("target/jummp.properties"))
    }

    void testSaveLdap() {
        mockForConstraintsTests(LdapCommand)
        // test for incorrect command
        LdapCommand cmd = new LdapCommand()
        cmd.validate()
        this.controller.saveLdap(cmd)
        assertEquals("configuration", this.controller.renderArgs["view"])
        assertEquals("saveLdap", this.controller.renderArgs["model"].action)
        assertEquals("ldap", this.controller.renderArgs["model"].template)
        assertEquals(cmd, this.controller.renderArgs["model"].ldap)
        // test for correct command
        cmd = new LdapCommand()
        cmd.ldapManagerDn = "user"
        cmd.ldapManagerPassword = "secure"
        cmd.ldapSearchBase = "search"
        cmd.ldapSearchFilter = "filter"
        cmd.ldapSearchSubtree = true
        cmd.ldapServer = "localhost"
        cmd.validate()
        this.controller.saveLdap(cmd)
        assertEquals("saved", this.controller.renderArgs["view"])
        assertEquals("LDAP", this.controller.renderArgs["model"].module)
        LdapCommand saved = this.controller.configurationService.loadLdapConfiguration()
        assertTrue(saved.ldapSearchSubtree)
        assertEquals("filter", saved.ldapSearchFilter)
        assertEquals("search", saved.ldapSearchBase)
        assertEquals("secure", saved.ldapManagerPassword)
        assertEquals("localhost", saved.ldapServer)
        assertEquals("user", saved.ldapManagerDn)
    }

    void testSaveMysql() {
        mockForConstraintsTests(MysqlCommand)
        // test for incorrect command
        MysqlCommand cmd = new MysqlCommand()
        cmd.validate()
        this.controller.saveMysql(cmd)
        assertEquals("configuration", this.controller.renderArgs["view"])
        assertEquals("saveMysql", this.controller.renderArgs["model"].action)
        assertEquals("mysql", this.controller.renderArgs["model"].template)
        assertEquals(cmd, this.controller.renderArgs["model"].mysql)
        // test for correct command
        cmd = new MysqlCommand()
        cmd.database = "jummp"
        cmd.password = "secure"
        cmd.port = 3306
        cmd.server = "localhost"
        cmd.username = "user"
        cmd.validate()
        this.controller.saveMysql(cmd)
        assertEquals("saved", this.controller.renderArgs["view"])
        assertEquals("MySQL", this.controller.renderArgs["model"].module)
        MysqlCommand saved = this.controller.configurationService.loadMysqlConfiguration()
        assertEquals(3306, saved.port)
        assertEquals("jummp", saved.database)
        assertEquals("secure", saved.password)
        assertEquals("localhost", saved.server)
        assertEquals("user", saved.username)

    }

    void testSaveServer() {
        mockForConstraintsTests(ServerCommand)
        // test for incorrect command
        ServerCommand cmd = new ServerCommand()
        cmd.validate()
        this.controller.saveServer(cmd)
        assertEquals("configuration", this.controller.renderArgs["view"])
        assertEquals("saveServer", this.controller.renderArgs["model"].action)
        assertEquals("server", this.controller.renderArgs["model"].template)
        assertEquals(cmd, this.controller.renderArgs["model"].server)
        // test for correct command
        cmd = new ServerCommand()
        cmd.url = "http://127.0.0.1:8080/jummp/"
        cmd.validate()
        this.controller.saveServer(cmd)
        assertEquals("saved", this.controller.renderArgs["view"])
        assertEquals("Server", this.controller.renderArgs["model"].module)
        ServerCommand saved = this.controller.configurationService.loadServerConfiguration()
        assertEquals("http://127.0.0.1:8080/jummp/", saved.url)
    }

    void testSaveSvn() {
        mockForConstraintsTests(SvnCommand)
        SvnCommand cmd = new SvnCommand()
        cmd.validate()
        this.controller.saveSvn(cmd)
        assertEquals("configuration", this.controller.renderArgs["view"])
        assertEquals("saveSvn", this.controller.renderArgs["model"].action)
        assertEquals("svn", this.controller.renderArgs["model"].template)
        assertEquals(cmd, this.controller.renderArgs["model"].svn)
        // test for correct command
        cmd = new SvnCommand()
        cmd.localRepository = "target"
        cmd.validate()
        this.controller.saveSvn(cmd)
        assertEquals("saved", this.controller.renderArgs["view"])
        assertEquals("Subversion", this.controller.renderArgs["model"].module)
        SvnCommand saved = this.controller.configurationService.loadSvnConfiguration()
        assertEquals("target", saved.localRepository)
    }

    void testSaveVcs() {
        mockForConstraintsTests(VcsCommand)
        // test for incorrect command
        VcsCommand cmd = new VcsCommand()
        cmd.validate()
        this.controller.saveVcs(cmd)
        assertEquals("configuration", this.controller.renderArgs["view"])
        assertEquals("saveVcs", this.controller.renderArgs["model"].action)
        assertEquals("vcs", this.controller.renderArgs["model"].template)
        assertEquals(cmd, this.controller.renderArgs["model"].vcs)
        // test for correct command
        cmd = new VcsCommand()
        cmd.vcs = "svn"
        cmd.exchangeDirectory = ""
        cmd.workingDirectory = ""
        cmd.validate()
        this.controller.saveVcs(cmd)
        assertEquals("saved", this.controller.renderArgs["view"])
        assertEquals("Version Control System", this.controller.renderArgs["model"].module)
        VcsCommand saved = this.controller.configurationService.loadVcsConfiguration()
        assertEquals("svn", saved.vcs)
    }
}
