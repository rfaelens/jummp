package net.biomodels.jummp.plugins.configuration

import grails.test.*

import org.junit.Before
import grails.test.mixin.TestFor
import static org.junit.Assert.*
import org.junit.*

class ConfigurationServiceTests {

    def configurationService

    @Before
    void setUp() {
    }

    @SuppressWarnings("CatchException")
    def shouldFail = { exception, code ->
        try {
            code.call()
            fail("Exception of type ${exception} was expected")
        } catch (Exception e) {
            if (!exception.isAssignableFrom(e.class) && !exception.isAssignableFrom(e.getCause().class)) {
                fail("Exception of type ${exception} expected but got ${e.class}")
            }
        }
    }

    @Test
    void testConfigurationFile() {
        // default configuration file is in home directory
        assertNotNull(configurationService.configurationFile)
        configurationService.afterPropertiesSet()
        assertNotNull(configurationService.configurationFile)
        assertEquals(new File(System.getProperty("user.home") + System.getProperty("file.separator") + ".jummp.properties"), configurationService.configurationFile)
        // test setting a file
        configurationService.configurationFile = null
        assertNull(configurationService.configurationFile)
        configurationService.configurationFile = new File("target/jummp.properties")
        assertNotNull(configurationService.configurationFile)
        configurationService.afterPropertiesSet()
        assertEquals(new File("target/jummp.properties"), configurationService.configurationFile)
    }

    void testUpdateDatabaseConfiguration() {
        shouldFail(NullPointerException) {
            configurationService.updateDatabaseConfiguration(null, null)
        }
        Properties properties = new Properties()
        shouldFail(NullPointerException) {
            configurationService.updateDatabaseConfiguration(properties, null)
        }
        DatabaseCommand database = new DatabaseCommand()
        configurationService.updateDatabaseConfiguration(properties, database)
        assertTrue(properties.isEmpty())
        // set values
        database = new DatabaseCommand()
        database.type = "MYSQL"
        database.database = "jummp"
        database.server   = "localhost"
        database.port     = 3306
        database.username = "user"
        database.password = "pass"
        configurationService.updateDatabaseConfiguration(properties, database)
        assertFalse(properties.isEmpty())
        assertEquals(6, properties.size())
        assertEquals("MYSQL", properties.getProperty("jummp.database.type"))
        assertEquals("jummp", properties.getProperty("jummp.database.database"))
        assertEquals("localhost", properties.getProperty("jummp.database.server"))
        assertEquals("3306", properties.getProperty("jummp.database.port"))
        assertEquals("user", properties.getProperty("jummp.database.username"))
        assertEquals("pass", properties.getProperty("jummp.database.password"))
        // update values
        database.type = "POSTRGESQL"
        database.database = "database"
        database.server   = "host"
        database.port     = 1234
        database.username = "name"
        database.password = ""
        configurationService.updateDatabaseConfiguration(properties, database)
        assertFalse(properties.isEmpty())
        assertEquals(6, properties.size())
        assertEquals("POSTRGESQL", properties.getProperty("jummp.database.type"))
        assertEquals("database", properties.getProperty("jummp.database.database"))
        assertEquals("host", properties.getProperty("jummp.database.server"))
        assertEquals("1234", properties.getProperty("jummp.database.port"))
        assertEquals("name", properties.getProperty("jummp.database.username"))
        assertEquals("", properties.getProperty("jummp.database.password"))
        // invalid command should not change
        configurationService.updateDatabaseConfiguration(properties, database)
        assertFalse(properties.isEmpty())
        assertEquals(6, properties.size())
        assertEquals("POSTRGESQL", properties.getProperty("jummp.database.type"))
        assertEquals("database", properties.getProperty("jummp.database.database"))
        assertEquals("host", properties.getProperty("jummp.database.server"))
        assertEquals("1234", properties.getProperty("jummp.database.port"))
        assertEquals("name", properties.getProperty("jummp.database.username"))
        assertEquals("", properties.getProperty("jummp.database.password"))
    }

    void testUpdateRemoteConfiguration() {
        shouldFail(NullPointerException) {
            configurationService.updateRemoteConfiguration(null, null)
        }
        Properties properties = new Properties()
        shouldFail(NullPointerException) {
            configurationService.updateRemoteConfiguration(properties, null)
        }
        RemoteCommand remote = new RemoteCommand()
        configurationService.updateRemoteConfiguration(properties, remote)
        assertTrue(properties.isEmpty())
        // set values
        //remote = mockCommandObject(RemoteCommand)
        remote.jummpRemote = "jms"
        remote.jummpExportDbus=false
        remote.jummpExportJms=true
        configurationService.updateRemoteConfiguration(properties, remote)
        assertFalse(properties.isEmpty())
        assertEquals(3, properties.size())
        assertEquals("jms", properties.getProperty("jummp.remote"))
        assertEquals("false", properties.getProperty("jummp.export.dbus"))
        assertEquals("true", properties.getProperty("jummp.export.jms"))
        // update values
        //remote = mockCommandObject(RemoteCommand)
        remote.jummpRemote = "dbus"
        remote.jummpExportDbus=true
        remote.jummpExportJms=false
        configurationService.updateRemoteConfiguration(properties, remote)
        assertFalse(properties.isEmpty())
        assertEquals(3, properties.size())
        assertEquals("dbus", properties.getProperty("jummp.remote"))
        assertEquals("true", properties.getProperty("jummp.export.dbus"))
        assertEquals("false", properties.getProperty("jummp.export.jms"))
        // invalid command should not change
        configurationService.updateRemoteConfiguration(properties, remote)
        assertFalse(properties.isEmpty())
        assertEquals(3, properties.size())
        assertEquals("dbus", properties.getProperty("jummp.remote"))
        assertEquals("true", properties.getProperty("jummp.export.dbus"))
        assertEquals("false", properties.getProperty("jummp.export.jms"))
    }

    void testUpdateLdapConfiguration() {
        shouldFail(NullPointerException) {
            configurationService.updateLdapConfiguration(null, null)
        }
        Properties properties = new Properties()
        // null ldap should disable ldap usage
        configurationService.updateLdapConfiguration(properties, null)
        assertFalse(properties.isEmpty())
        assertEquals(1, properties.size())
        assertEquals("false", properties.getProperty("jummp.security.ldap.enabled"))

        // test with empty ldap command
        properties = new Properties()
        LdapCommand ldap = new LdapCommand()
        assertTrue(properties.isEmpty())
        configurationService.updateLdapConfiguration(properties, ldap)
        assertTrue(properties.isEmpty())
        // test with minimum values
        ldap.ldapManagerDn       = "manager"
        ldap.ldapManagerPassword = "password"
        ldap.ldapSearchBase      = "search"
        ldap.ldapSearchFilter    = "filter"
        ldap.ldapSearchSubtree   = true
        ldap.ldapServer          = "server"
        configurationService.updateLdapConfiguration(properties, ldap)
        assertFalse(properties.isEmpty())
        assertEquals(7, properties.size())
        assertEquals("true",     properties.getProperty("jummp.security.ldap.enabled"))
        assertEquals("server",   properties.getProperty("jummp.security.ldap.server"))
        assertEquals("manager",  properties.getProperty("jummp.security.ldap.managerDn"))
        assertEquals("password", properties.getProperty("jummp.security.ldap.managerPw"))
        assertEquals("search",   properties.getProperty("jummp.security.ldap.search.base"))
        assertEquals("filter",   properties.getProperty("jummp.security.ldap.search.filter"))
        assertEquals("true",     properties.getProperty("jummp.security.ldap.search.subTree"))
        // update values
        ldap.ldapManagerDn       = "ldap"
        ldap.ldapManagerPassword = "1234"
        ldap.ldapSearchBase      = "cn"
        ldap.ldapSearchFilter    = "ab"
        ldap.ldapSearchSubtree   = false
        ldap.ldapServer          = "localhost"
        configurationService.updateLdapConfiguration(properties, ldap)
        assertFalse(properties.isEmpty())
        assertEquals(7, properties.size())
        assertEquals("true",      properties.getProperty("jummp.security.ldap.enabled"))
        assertEquals("localhost", properties.getProperty("jummp.security.ldap.server"))
        assertEquals("ldap",      properties.getProperty("jummp.security.ldap.managerDn"))
        assertEquals("1234",      properties.getProperty("jummp.security.ldap.managerPw"))
        assertEquals("cn",        properties.getProperty("jummp.security.ldap.search.base"))
        assertEquals("ab",        properties.getProperty("jummp.security.ldap.search.filter"))
        assertEquals("false",     properties.getProperty("jummp.security.ldap.search.subTree"))
        // invalid command should not change
        configurationService.updateLdapConfiguration(properties, ldap)
        assertFalse(properties.isEmpty())
        assertEquals(7, properties.size())
        assertEquals("true",      properties.getProperty("jummp.security.ldap.enabled"))
        assertEquals("localhost", properties.getProperty("jummp.security.ldap.server"))
        assertEquals("ldap",      properties.getProperty("jummp.security.ldap.managerDn"))
        assertEquals("1234",      properties.getProperty("jummp.security.ldap.managerPw"))
        assertEquals("cn",        properties.getProperty("jummp.security.ldap.search.base"))
        assertEquals("ab",        properties.getProperty("jummp.security.ldap.search.filter"))
        assertEquals("false",     properties.getProperty("jummp.security.ldap.search.subTree"))
        // passing in a null ldap should delete all properties
        configurationService.updateLdapConfiguration(properties, null)
        assertFalse(properties.isEmpty())
        assertEquals(1, properties.size())
        assertEquals("false", properties.getProperty("jummp.security.ldap.enabled"))
    }

    void testUpdateVcsConfiguration() {
        shouldFail(NullPointerException) {
            configurationService.updateVcsConfiguration(null, null)
        }
        Properties properties = new Properties()
        shouldFail(NullPointerException) {
            configurationService.updateVcsConfiguration(properties, null)
        }
        VcsCommand vcs = new VcsCommand()
        // should not update when command is not valid
        configurationService.updateVcsConfiguration(properties, vcs)
        assertTrue(properties.isEmpty())
        vcs.exchangeDirectory = ""
        vcs.workingDirectory = ""
        vcs.vcs = "svn"
        configurationService.updateVcsConfiguration(properties, vcs)
        assertFalse(properties.isEmpty())
        assertEquals(3, properties.size())
        assertEquals("subversion", properties.getProperty("jummp.vcs.plugin"))
        assertEquals("", properties.getProperty("jummp.vcs.exchangeDirectory"))
        assertEquals("", properties.getProperty("jummp.vcs.workingDirectory"))
        // invalid command should not change
        configurationService.updateVcsConfiguration(properties, vcs)
        assertFalse(properties.isEmpty())
        assertEquals(3, properties.size())
        assertEquals("subversion", properties.getProperty("jummp.vcs.plugin"))
        assertEquals("", properties.getProperty("jummp.vcs.exchangeDirectory"))
        assertEquals("", properties.getProperty("jummp.vcs.workingDirectory"))
        // update to different settings
        vcs.exchangeDirectory = "target"
        vcs.workingDirectory = "."
        vcs.vcs = "git"
        configurationService.updateVcsConfiguration(properties, vcs)
        assertFalse(properties.isEmpty())
        assertEquals(3, properties.size())
        assertEquals("git", properties.getProperty("jummp.vcs.plugin"))
        assertEquals("target", properties.getProperty("jummp.vcs.exchangeDirectory"))
        assertEquals(".", properties.getProperty("jummp.vcs.workingDirectory"))
    }

    void testUpdateSvnConfiguration() {
        shouldFail(NullPointerException) {
            configurationService.updateSvnConfiguration(null, null)
        }
        Properties properties = new Properties()
        SvnCommand svn = new SvnCommand()
        configurationService.updateSvnConfiguration(properties, svn)
        assertTrue(properties.isEmpty())
        svn.localRepository = "target"
        configurationService.updateSvnConfiguration(properties, svn)
        assertFalse(properties.isEmpty())
        assertEquals(1, properties.size())
        assertEquals("target", properties.getProperty("jummp.plugins.subversion.localRepository"))
        // update configuration
        svn.localRepository = "."
        configurationService.updateSvnConfiguration(properties, svn)
        assertFalse(properties.isEmpty())
        assertEquals(1, properties.size())
        assertEquals(".", properties.getProperty("jummp.plugins.subversion.localRepository"))
        // invalid should not change
        configurationService.updateSvnConfiguration(properties, svn)
        assertFalse(properties.isEmpty())
        assertEquals(1, properties.size())
        assertEquals(".", properties.getProperty("jummp.plugins.subversion.localRepository"))
        // passing in null should delete the property
        configurationService.updateSvnConfiguration(properties, null)
        assertTrue(properties.isEmpty())
    }

    void testUpdateFirstRunConfiguration() {
        shouldFail(NullPointerException) {
            configurationService.updateFirstRunConfiguration(null, null)
        }
        Properties properties = new Properties()
        shouldFail(NullPointerException) {
            configurationService.updateFirstRunConfiguration(properties, null)
        }
        // invalid should not work
        FirstRunCommand firstRun = new FirstRunCommand()
        configurationService.updateFirstRunConfiguration(properties, firstRun)
        assertTrue(properties.isEmpty())
        // passing in a correct command should update
        firstRun.firstRun = "true"
        configurationService.updateFirstRunConfiguration(properties, firstRun)
        assertFalse(properties.isEmpty())
        assertEquals(1, properties.size())
        assertEquals("true", properties.getProperty("jummp.firstRun"))
        // passing in different options should update
        firstRun.firstRun = "false"
        configurationService.updateFirstRunConfiguration(properties, firstRun)
        assertFalse(properties.isEmpty())
        assertEquals(1, properties.size())
        assertEquals("false", properties.getProperty("jummp.firstRun"))
        // passing in invalid command should not update
        configurationService.updateFirstRunConfiguration(properties, firstRun)
        assertFalse(properties.isEmpty())
        assertEquals(1, properties.size())
        assertEquals("false", properties.getProperty("jummp.firstRun"))
    }

    void testUpdateServerConfiguration() {
        shouldFail(NullPointerException) {
            configurationService.updateServerConfiguration(null, null)
        }
        Properties properties = new Properties()
        shouldFail(NullPointerException) {
            configurationService.updateServerConfiguration(properties, null)
        }
        // invalid should not work
        ServerCommand server = new ServerCommand()
        configurationService.updateServerConfiguration(properties, server)
        assertTrue(properties.isEmpty())
        // passing in a correct command should update
        server.url = "http://127.0.0.1:8080/jummp/"
        server.weburl = "http://127.0.0.1:8080/jummp-web-application/"
        server.protectEverything = true
        configurationService.updateServerConfiguration(properties, server)
        assertFalse(properties.isEmpty())
        assertEquals(3, properties.size())
        assertEquals("http://127.0.0.1:8080/jummp/", properties.getProperty("jummp.server.url"))
        assertEquals("http://127.0.0.1:8080/jummp-web-application/", properties.getProperty("jummp.server.web.url"))
        assertEquals("true", properties.getProperty("jummp.server.protection"))
        // passing in different options should update
        server.url = "http://www.example.com/"
        server.weburl = "http://www.example.org/"
        server.protectEverything = false
        configurationService.updateServerConfiguration(properties, server)
        assertFalse(properties.isEmpty())
        assertEquals(3, properties.size())
        assertEquals("http://www.example.com/", properties.getProperty("jummp.server.url"))
        assertEquals("http://www.example.org/", properties.getProperty("jummp.server.web.url"))
        assertEquals("false", properties.getProperty("jummp.server.protection"))
        // passing in invalid command should not update
        configurationService.updateServerConfiguration(properties, server)
        assertFalse(properties.isEmpty())
        assertEquals(3, properties.size())
        assertEquals("http://www.example.com/", properties.getProperty("jummp.server.url"))
        assertEquals("http://www.example.org/", properties.getProperty("jummp.server.web.url"))
        assertEquals("false", properties.getProperty("jummp.server.protection"))
    }

    void testStoreConfiguration() {
        // set a file to use
        configurationService.configurationFile = new File("target/jummpProperties")
        configurationService.configurationFile.delete()
        configurationService.afterPropertiesSet()
        assertEquals(new File("target/jummpProperties"), configurationService.configurationFile)
        DatabaseCommand database = new DatabaseCommand()
        database.type = "MYSQL"
        database.database = "jummp"
        database.server   = "localhost"
        database.port     = 3306
        database.username = "user"
        database.password = "pass"
        assertTrue(database.validate())
        RemoteCommand remote = new RemoteCommand()
        remote.jummpRemote="jms"
        remote.jummpExportDbus=false
        remote.jummpExportJms=true
        assertTrue(remote.validate())
        DBusCommand dbus = new DBusCommand()
        dbus.systemBus = false
        assertTrue(dbus.validate())
        LdapCommand ldap = new LdapCommand()
        ldap.ldapManagerDn       = "manager"
        ldap.ldapManagerPassword = "password"
        ldap.ldapSearchBase      = "search"
        ldap.ldapSearchFilter    = "filter"
        ldap.ldapSearchSubtree   = true
        ldap.ldapServer          = "server"
        assertTrue(ldap.validate())
        FirstRunCommand firstRun = new FirstRunCommand()
        firstRun.firstRun = "false"
        assertTrue(firstRun.validate())
        SvnCommand svn = new SvnCommand()
        svn.localRepository = "target"
        assertTrue(svn.validate())
        VcsCommand vcs = new VcsCommand()
        vcs.exchangeDirectory = ""
        vcs.workingDirectory = ""
        vcs.vcs = "svn"
        assertTrue(vcs.validate())
        ServerCommand server = new ServerCommand()
        server.url = "http://127.0.0.1:8080/jummp/"
        server.weburl = "http://127.0.0.1:8080/jummp-web-application/"
        server.protectEverything = true
        assertTrue(server.validate())
        UserRegistrationCommand userRegistration = new UserRegistrationCommand()
        userRegistration.registration = true
        userRegistration.sendEmail = true
        userRegistration.sendToAdmin = true
        userRegistration.senderAddress = "test@example.com"
        userRegistration.adminAddress = "admin@example.com"
        userRegistration.url = "http://www.example.com"
        userRegistration.body = "This is the mail body"
        userRegistration.subject = "This is the subject"
        userRegistration.activationSubject = "This is the activation Subject"
        userRegistration.activationBody = "This is the activation body"
        userRegistration.activationUrl = "http://www.example.org"
        assertTrue(userRegistration.validate())
        ChangePasswordCommand changePassword = new ChangePasswordCommand()
        changePassword.changePassword = true
        changePassword.resetPassword  = true
        changePassword.senderAddress  = "test@example.com"
        changePassword.subject        = "Password Forgotten mail"
        changePassword.body           = "Body of the password forgotten mail"
        changePassword.url            = "http://www.example.com/"
        assertTrue(changePassword.validate())
        TriggerCommand trigger = new TriggerCommand()
        trigger.maxInactiveTime = 1000
        trigger.startRemoveOffset = 1001
        trigger.removeInterval = 1002
        assertTrue(trigger.validate())
        SBMLCommand sbml = new SBMLCommand()
        sbml.validation = false
        assertTrue(sbml.validate())
        BivesCommand bives = new BivesCommand()
        bives.diffDir = "/tmp/"
        assertTrue(bives.validate())
        BrandingCommand branding = new BrandingCommand()
        branding.internalColor = "#FFFFFF"
        assertTrue(branding.validate())

        // everything should be written to the properties file
        configurationService.storeConfiguration(database, ldap, vcs, svn, firstRun, server, userRegistration, changePassword, remote, dbus, trigger, sbml, bives, branding)
        Properties properties = new Properties()
        properties.load(new FileInputStream("target/jummpProperties"))
        assertEquals(50, properties.size())
        assertEquals("false", properties.getProperty("jummp.firstRun"))
        assertEquals("target", properties.getProperty("jummp.plugins.subversion.localRepository"))
        assertEquals("",           properties.getProperty("jummp.vcs.workingDirectory"))
        assertEquals("",           properties.getProperty("jummp.vcs.exchangeDirectory"))
        assertEquals("subversion", properties.getProperty("jummp.vcs.plugin"))
        assertEquals("ldap",     properties.getProperty("jummp.security.authenticationBackend"))
        assertEquals("true",     properties.getProperty("jummp.security.ldap.enabled"))
        assertEquals("true",     properties.getProperty("jummp.security.ldap.search.subTree"))
        assertEquals("filter",   properties.getProperty("jummp.security.ldap.search.filter"))
        assertEquals("search",   properties.getProperty("jummp.security.ldap.search.base"))
        assertEquals("password", properties.getProperty("jummp.security.ldap.managerPw"))
        assertEquals("manager",  properties.getProperty("jummp.security.ldap.managerDn"))
        assertEquals("server",   properties.getProperty("jummp.security.ldap.server"))
        assertEquals("MYSQL",     properties.getProperty("jummp.database.type"))
        assertEquals("jummp",     properties.getProperty("jummp.database.database"))
        assertEquals("localhost", properties.getProperty("jummp.database.server"))
        assertEquals("3306",      properties.getProperty("jummp.database.port"))
        assertEquals("user",      properties.getProperty("jummp.database.username"))
        assertEquals("pass",      properties.getProperty("jummp.database.password"))
        assertEquals("jms",      properties.getProperty("jummp.remote"))
        assertEquals("false",      properties.getProperty("jummp.export.dbus"))
        assertEquals("true",      properties.getProperty("jummp.export.jms"))
        assertEquals("false",      properties.getProperty("jummp.plugins.dbus.systemBus"))
        assertEquals("http://127.0.0.1:8080/jummp/", properties.getProperty("jummp.server.url"))
        assertEquals("http://127.0.0.1:8080/jummp-web-application/", properties.getProperty("jummp.server.web.url"))
        assertEquals("true", properties.getProperty("jummp.server.protection"))
        assertEquals("1001",    properties.getProperty("jummp.authenticationHash.startRemoveOffset"))
        assertEquals("1002",    properties.getProperty("jummp.authenticationHash.removeInterval"))
        assertEquals("1000",    properties.getProperty("jummp.authenticationHash.maxInactiveTime"))
        assertEquals("false",    properties.getProperty("jummp.plugins.sbml.validation"))
        assertEquals("/tmp/",    properties.getProperty("jummp.plugins.bives.diffdir"))
        assertEquals("#FFFFFF",     properties.getProperty("jummp.branding.internalColor"))

        // change configuration - no ldap, no svn
        firstRun.firstRun = "true"
        assertTrue(firstRun.validate())
        vcs.exchangeDirectory = ""
        vcs.workingDirectory = "target"
        vcs.vcs = "git"
        assertTrue(vcs.validate())

        configurationService.storeConfiguration(database, null, vcs, null, firstRun, server, userRegistration, changePassword, remote, dbus, trigger, sbml, bives, branding)
        properties = new Properties()
        properties.load(new FileInputStream("target/jummpProperties"))
        assertEquals(26, properties.size())
        assertEquals("true",      properties.getProperty("jummp.firstRun"))
        assertEquals("target",    properties.getProperty("jummp.vcs.workingDirectory"))
        assertEquals("",          properties.getProperty("jummp.vcs.exchangeDirectory"))
        assertEquals("git",       properties.getProperty("jummp.vcs.plugin"))
        assertEquals("jummp",     properties.getProperty("jummp.database.database"))
        assertEquals("localhost", properties.getProperty("jummp.database.server"))
        assertEquals("3306",      properties.getProperty("jummp.database.port"))
        assertEquals("user",      properties.getProperty("jummp.database.username"))
        assertEquals("pass",      properties.getProperty("jummp.database.password"))
        assertEquals("database",  properties.getProperty("jummp.security.authenticationBackend"))
        assertEquals("false",     properties.getProperty("jummp.security.ldap.enabled"))
        assertEquals("http://127.0.0.1:8080/jummp/", properties.getProperty("jummp.server.url"))
        assertEquals("http://127.0.0.1:8080/jummp-web-application/", properties.getProperty("jummp.server.web.url"))
        assertEquals("true", properties.getProperty("jummp.server.protection"))
    }

    void testLoadStoreDatabaseConfiguration() {
        populateProperties()
        // now load the data from the service
        DatabaseCommand database = configurationService.loadDatabaseConfiguration()
        assertEquals("MYSQL",     database.type.key)
        assertEquals("jummp",     database.database)
        assertEquals("localhost", database.server)
        assertEquals(3306,        database.port)
        assertEquals("user",      database.username)
        assertEquals("pass",      database.password)

        // store a new configuration
        database.type = "MYSQL"
        database.database = "database"
        database.server   = "server"
        database.port     = 1234
        database.username = "name"
        database.password = "secret"
        configurationService.saveDatabaseConfiguration(database)
        // verify the configuration
        DatabaseCommand config = configurationService.loadDatabaseConfiguration()
        assertEquals("MYSQL",    config.type.key)
        assertEquals("database", config.database)
        assertEquals("server",   config.server)
        assertEquals(1234,       config.port)
        assertEquals("name",     config.username)
        assertEquals("secret",   config.password)
        // verify that other config options are unchanged
        Properties properties = new Properties()
        properties.load(new FileInputStream("target/jummpProperties"))
        assertEquals(50, properties.size())
        assertEquals("false", properties.getProperty("jummp.firstRun"))
        assertEquals("target", properties.getProperty("jummp.plugins.subversion.localRepository"))
        assertEquals("",           properties.getProperty("jummp.vcs.workingDirectory"))
        assertEquals("",           properties.getProperty("jummp.vcs.exchangeDirectory"))
        assertEquals("subversion", properties.getProperty("jummp.vcs.plugin"))
        assertEquals("ldap",     properties.getProperty("jummp.security.authenticationBackend"))
        assertEquals("true",     properties.getProperty("jummp.security.ldap.enabled"))
        assertEquals("true",     properties.getProperty("jummp.security.ldap.search.subTree"))
        assertEquals("filter",   properties.getProperty("jummp.security.ldap.search.filter"))
        assertEquals("search",   properties.getProperty("jummp.security.ldap.search.base"))
        assertEquals("password", properties.getProperty("jummp.security.ldap.managerPw"))
        assertEquals("manager",  properties.getProperty("jummp.security.ldap.managerDn"))
        assertEquals("server",   properties.getProperty("jummp.security.ldap.server"))
        assertEquals("jms",   properties.getProperty("jummp.remote"))
        assertEquals("false",   properties.getProperty("jummp.export.dbus"))
        assertEquals("true",   properties.getProperty("jummp.export.jms"))
        assertEquals("false",      properties.getProperty("jummp.plugins.dbus.systemBus"))
        assertEquals("http://127.0.0.1:8080/jummp/", properties.getProperty("jummp.server.url"))
        assertEquals("http://127.0.0.1:8080/jummp-web-application/", properties.getProperty("jummp.server.web.url"))
        assertEquals("true", properties.getProperty("jummp.server.protection"))
        assertEquals("1001",    properties.getProperty("jummp.authenticationHash.startRemoveOffset"))
        assertEquals("1002",    properties.getProperty("jummp.authenticationHash.removeInterval"))
        assertEquals("1000",    properties.getProperty("jummp.authenticationHash.maxInactiveTime"))
        assertEquals("false",    properties.getProperty("jummp.plugins.sbml.validation"))
        assertEquals("/tmp/",    properties.getProperty("jummp.plugins.bives.diffdir"))
    }

    void testLoadStoreRemoteConfiguration() {
        populateProperties()
        // now load the data from the service
        RemoteCommand remote = configurationService.loadRemoteConfiguration()
        assertEquals("jms",  remote.jummpRemote)
        assertFalse(remote.jummpExportDbus)
        assertTrue( remote.jummpExportJms)

        // store a new configuration
        remote.jummpRemote="dbus"
        remote.jummpExportDbus=true
        remote.jummpExportJms=false
        configurationService.saveRemoteConfiguration(remote)
        // verify the configuration
        RemoteCommand config = configurationService.loadRemoteConfiguration()
        assertEquals("dbus", config.jummpRemote)
        assertEquals(true,   config.jummpExportDbus)
        assertEquals(false,       config.jummpExportJms)
        // verify that other config options are unchanged
        Properties properties = new Properties()
        properties.load(new FileInputStream("target/jummpProperties"))
        assertEquals(50, properties.size())
        assertEquals("false", properties.getProperty("jummp.firstRun"))
        assertEquals("target", properties.getProperty("jummp.plugins.subversion.localRepository"))
        assertEquals("",           properties.getProperty("jummp.vcs.workingDirectory"))
        assertEquals("",           properties.getProperty("jummp.vcs.exchangeDirectory"))
        assertEquals("subversion", properties.getProperty("jummp.vcs.plugin"))
        assertEquals("ldap",     properties.getProperty("jummp.security.authenticationBackend"))
        assertEquals("true",     properties.getProperty("jummp.security.ldap.enabled"))
        assertEquals("true",     properties.getProperty("jummp.security.ldap.search.subTree"))
        assertEquals("filter",   properties.getProperty("jummp.security.ldap.search.filter"))
        assertEquals("search",   properties.getProperty("jummp.security.ldap.search.base"))
        assertEquals("password", properties.getProperty("jummp.security.ldap.managerPw"))
        assertEquals("manager",  properties.getProperty("jummp.security.ldap.managerDn"))
        assertEquals("server",   properties.getProperty("jummp.security.ldap.server"))
        assertEquals("http://127.0.0.1:8080/jummp/", properties.getProperty("jummp.server.url"))
        assertEquals("http://127.0.0.1:8080/jummp-web-application/", properties.getProperty("jummp.server.web.url"))
        assertEquals("true", properties.getProperty("jummp.server.protection"))
        assertEquals("1001",    properties.getProperty("jummp.authenticationHash.startRemoveOffset"))
        assertEquals("1002",    properties.getProperty("jummp.authenticationHash.removeInterval"))
        assertEquals("1000",    properties.getProperty("jummp.authenticationHash.maxInactiveTime"))
        assertEquals("false",    properties.getProperty("jummp.plugins.sbml.validation"))
        assertEquals("/tmp/",    properties.getProperty("jummp.plugins.bives.diffdir"))
    }

    void testLoadStoreLdapConfiguration() {
        //populateProperties()
        // Load the data from properties
        LdapCommand ldap = configurationService.loadLdapConfiguration()
        assertTrue(ldap.ldapSearchSubtree)
        assertEquals("filter",   ldap.ldapSearchFilter)
        assertEquals("search",   ldap.ldapSearchBase)
        assertEquals("password", ldap.ldapManagerPassword)
        assertEquals("manager",  ldap.ldapManagerDn)
        assertEquals("server",   ldap.ldapServer)
        // store a new configuration
        LdapCommand ldap3 = new LdapCommand()
        ldap3.ldapSearchSubtree = false
        ldap3.ldapSearchFilter = "*"
        ldap3.ldapSearchBase = "base"
        ldap3.ldapManagerPassword = "secure"
        ldap3.ldapManagerDn = "cn"
        ldap3.ldapServer = "localhost"
        configurationService.saveLdapConfiguration(ldap3)
        // verify new configuration
        LdapCommand ldap2 = configurationService.loadLdapConfiguration()
        //println(ldap2.)
        assertFalse(ldap2.ldapSearchSubtree)
        assertEquals("*",         ldap2.ldapSearchFilter)
        assertEquals("base",      ldap2.ldapSearchBase)
        assertEquals("secure",    ldap2.ldapManagerPassword)
        assertEquals("cn",        ldap2.ldapManagerDn)
        assertEquals("localhost", ldap2.ldapServer)
        // verify that other config options are unchanged
        Properties properties = new Properties()
        properties.load(new FileInputStream("target/jummpProperties"))
        assertEquals(50, properties.size())
        assertEquals("false", properties.getProperty("jummp.firstRun"))
        assertEquals("target", properties.getProperty("jummp.plugins.subversion.localRepository"))
        assertEquals("",           properties.getProperty("jummp.vcs.workingDirectory"))
        assertEquals("",           properties.getProperty("jummp.vcs.exchangeDirectory"))
        assertEquals("subversion", properties.getProperty("jummp.vcs.plugin"))
        assertEquals("ldap",     properties.getProperty("jummp.security.authenticationBackend"))
        assertEquals("jummp",     properties.getProperty("jummp.database.database"))
        assertEquals("localhost", properties.getProperty("jummp.database.server"))
        assertEquals("3306",      properties.getProperty("jummp.database.port"))
        assertEquals("user",      properties.getProperty("jummp.database.username"))
        assertEquals("pass",      properties.getProperty("jummp.database.password"))
        assertEquals("jummp",     properties.getProperty("jummp.database.database"))
        assertEquals("localhost", properties.getProperty("jummp.database.server"))
        assertEquals("3306",      properties.getProperty("jummp.database.port"))
        assertEquals("user",      properties.getProperty("jummp.database.username"))
        assertEquals("pass",      properties.getProperty("jummp.database.password"))
        assertEquals("dbus",   properties.getProperty("jummp.remote"))
        assertEquals("true",   properties.getProperty("jummp.export.dbus"))
        assertEquals("false",   properties.getProperty("jummp.export.jms"))
        assertEquals("false",      properties.getProperty("jummp.plugins.dbus.systemBus"))
        assertEquals("http://127.0.0.1:8080/jummp/", properties.getProperty("jummp.server.url"))
        assertEquals("http://127.0.0.1:8080/jummp-web-application/", properties.getProperty("jummp.server.web.url"))
        assertEquals("true", properties.getProperty("jummp.server.protection"))
        assertEquals("1001",    properties.getProperty("jummp.authenticationHash.startRemoveOffset"))
        assertEquals("1002",    properties.getProperty("jummp.authenticationHash.removeInterval"))
        assertEquals("1000",    properties.getProperty("jummp.authenticationHash.maxInactiveTime"))
        assertEquals("false",    properties.getProperty("jummp.plugins.sbml.validation"))
        assertEquals("/tmp/",    properties.getProperty("jummp.plugins.bives.diffdir"))
    }

    void testLoadStoreSvnConfiguration() {
        populateProperties()
        // verify svn configuration
        SvnCommand svn = configurationService.loadSvnConfiguration()
        assertEquals("target", svn.localRepository)
        // set new configuration
        svn.localRepository = "/tmp/"
        configurationService.saveSvnConfiguration(svn)
        // verify new configuration
        SvnCommand svn2 = configurationService.loadSvnConfiguration()
        assertEquals("/tmp/", svn2.localRepository)
        // verify that other config options are unchanged
        Properties properties = new Properties()
        properties.load(new FileInputStream("target/jummpProperties"))
        assertEquals(50, properties.size())
        assertEquals("false", properties.getProperty("jummp.firstRun"))
        assertEquals("",           properties.getProperty("jummp.vcs.workingDirectory"))
        assertEquals("",           properties.getProperty("jummp.vcs.exchangeDirectory"))
        assertEquals("subversion", properties.getProperty("jummp.vcs.plugin"))
        assertEquals("ldap",     properties.getProperty("jummp.security.authenticationBackend"))
        assertEquals("true",     properties.getProperty("jummp.security.ldap.enabled"))
        assertEquals("true",     properties.getProperty("jummp.security.ldap.search.subTree"))
        assertEquals("filter",   properties.getProperty("jummp.security.ldap.search.filter"))
        assertEquals("search",   properties.getProperty("jummp.security.ldap.search.base"))
        assertEquals("password", properties.getProperty("jummp.security.ldap.managerPw"))
        assertEquals("manager",  properties.getProperty("jummp.security.ldap.managerDn"))
        assertEquals("server",   properties.getProperty("jummp.security.ldap.server"))
        assertEquals("jummp",     properties.getProperty("jummp.database.database"))
        assertEquals("localhost", properties.getProperty("jummp.database.server"))
        assertEquals("3306",      properties.getProperty("jummp.database.port"))
        assertEquals("user",      properties.getProperty("jummp.database.username"))
        assertEquals("pass",      properties.getProperty("jummp.database.password"))
        assertEquals("jms",   properties.getProperty("jummp.remote"))
        assertEquals("false",   properties.getProperty("jummp.export.dbus"))
        assertEquals("true",   properties.getProperty("jummp.export.jms"))
        assertEquals("false",      properties.getProperty("jummp.plugins.dbus.systemBus"))
        assertEquals("http://127.0.0.1:8080/jummp/", properties.getProperty("jummp.server.url"))
        assertEquals("http://127.0.0.1:8080/jummp-web-application/", properties.getProperty("jummp.server.web.url"))
        assertEquals("true", properties.getProperty("jummp.server.protection"))
        assertEquals("1001",    properties.getProperty("jummp.authenticationHash.startRemoveOffset"))
        assertEquals("1002",    properties.getProperty("jummp.authenticationHash.removeInterval"))
        assertEquals("1000",    properties.getProperty("jummp.authenticationHash.maxInactiveTime"))
        assertEquals("false",    properties.getProperty("jummp.plugins.sbml.validation"))
        assertEquals("/tmp/",    properties.getProperty("jummp.plugins.bives.diffdir"))
    }

    void testLoadStoreVcsConfiguration() {
        populateProperties()
        // verify the configuration
        VcsCommand vcs = configurationService.loadVcsConfiguration()
        assertEquals("svn", vcs.vcs)
        assertEquals("", vcs.exchangeDirectory)
        assertEquals("", vcs.workingDirectory)
        // set new configuration
        vcs.vcs = "git"
        vcs.exchangeDirectory = "/tmp/"
        vcs.workingDirectory = "target"
        configurationService.saveVcsConfiguration(vcs)
        // verify new configuration
        VcsCommand vcs2 = configurationService.loadVcsConfiguration()
        assertEquals("git", vcs2.vcs)
        assertEquals("/tmp/", vcs2.exchangeDirectory)
        assertEquals("target", vcs2.workingDirectory)
        // verify that other config options are unchanged
        Properties properties = new Properties()
        properties.load(new FileInputStream("target/jummpProperties"))
        assertEquals(50, properties.size())
        assertEquals("false", properties.getProperty("jummp.firstRun"))
        assertEquals("target", properties.getProperty("jummp.plugins.subversion.localRepository"))
        assertEquals("ldap",     properties.getProperty("jummp.security.authenticationBackend"))
        assertEquals("true",     properties.getProperty("jummp.security.ldap.enabled"))
        assertEquals("true",     properties.getProperty("jummp.security.ldap.search.subTree"))
        assertEquals("filter",   properties.getProperty("jummp.security.ldap.search.filter"))
        assertEquals("search",   properties.getProperty("jummp.security.ldap.search.base"))
        assertEquals("password", properties.getProperty("jummp.security.ldap.managerPw"))
        assertEquals("manager",  properties.getProperty("jummp.security.ldap.managerDn"))
        assertEquals("server",   properties.getProperty("jummp.security.ldap.server"))
        assertEquals("jummp",     properties.getProperty("jummp.database.database"))
        assertEquals("localhost", properties.getProperty("jummp.database.server"))
        assertEquals("3306",      properties.getProperty("jummp.database.port"))
        assertEquals("user",      properties.getProperty("jummp.database.username"))
        assertEquals("pass",      properties.getProperty("jummp.database.password"))
        assertEquals("jms",   properties.getProperty("jummp.remote"))
        assertEquals("false",   properties.getProperty("jummp.export.dbus"))
        assertEquals("true",   properties.getProperty("jummp.export.jms"))
        assertEquals("false",      properties.getProperty("jummp.plugins.dbus.systemBus"))
        assertEquals("http://127.0.0.1:8080/jummp/", properties.getProperty("jummp.server.url"))
        assertEquals("http://127.0.0.1:8080/jummp-web-application/", properties.getProperty("jummp.server.web.url"))
        assertEquals("true", properties.getProperty("jummp.server.protection"))
        assertEquals("1001",    properties.getProperty("jummp.authenticationHash.startRemoveOffset"))
        assertEquals("1002",    properties.getProperty("jummp.authenticationHash.removeInterval"))
        assertEquals("1000",    properties.getProperty("jummp.authenticationHash.maxInactiveTime"))
        assertEquals("false",    properties.getProperty("jummp.plugins.sbml.validation"))
        assertEquals("/tmp/",    properties.getProperty("jummp.plugins.bives.diffdir"))
    }

    void testLoadStoreServerConfiguration() {
        populateProperties()
        // verify the configuration
        ServerCommand server = configurationService.loadServerConfiguration()
        assertEquals("http://127.0.0.1:8080/jummp/", server.url)
        assertEquals("http://127.0.0.1:8080/jummp-web-application/", server.weburl)
        // set new configuration
        ServerCommand server3 = new ServerCommand()
        server3.url = "https://www.example.com/"
        server3.weburl = "https://www.example.org/"
        server3.protectEverything = true
        configurationService.saveServerConfiguration(server3)
        // verify new configuration
        ServerCommand server2 = configurationService.loadServerConfiguration()
        assertEquals("https://www.example.com/", server2.url)
        // verify that other configuration options are unchanged
        Properties properties = new Properties()
        properties.load(new FileInputStream("target/jummpProperties"))
        assertEquals(50, properties.size())
        assertEquals("false", properties.getProperty("jummp.firstRun"))
        assertEquals("target", properties.getProperty("jummp.plugins.subversion.localRepository"))
        assertEquals("",           properties.getProperty("jummp.vcs.workingDirectory"))
        assertEquals("",           properties.getProperty("jummp.vcs.exchangeDirectory"))
        assertEquals("subversion", properties.getProperty("jummp.vcs.plugin"))
        assertEquals("ldap",     properties.getProperty("jummp.security.authenticationBackend"))
        assertEquals("true",     properties.getProperty("jummp.security.ldap.enabled"))
        assertEquals("true",     properties.getProperty("jummp.security.ldap.search.subTree"))
        assertEquals("filter",   properties.getProperty("jummp.security.ldap.search.filter"))
        assertEquals("search",   properties.getProperty("jummp.security.ldap.search.base"))
        assertEquals("password", properties.getProperty("jummp.security.ldap.managerPw"))
        assertEquals("manager",  properties.getProperty("jummp.security.ldap.managerDn"))
        assertEquals("server",   properties.getProperty("jummp.security.ldap.server"))
        assertEquals("jummp",     properties.getProperty("jummp.database.database"))
        assertEquals("localhost", properties.getProperty("jummp.database.server"))
        assertEquals("3306",      properties.getProperty("jummp.database.port"))
        assertEquals("user",      properties.getProperty("jummp.database.username"))
        assertEquals("pass",      properties.getProperty("jummp.database.password"))
        assertEquals("jms",   properties.getProperty("jummp.remote"))
        assertEquals("false",   properties.getProperty("jummp.export.dbus"))
        assertEquals("true",   properties.getProperty("jummp.export.jms"))
        assertEquals("false",      properties.getProperty("jummp.plugins.dbus.systemBus"))
        assertEquals("https://www.example.com/", properties.getProperty("jummp.server.url"))
        assertEquals("https://www.example.org/", properties.getProperty("jummp.server.web.url"))
        assertEquals("true", properties.getProperty("jummp.server.protection"))
        assertEquals("1001",    properties.getProperty("jummp.authenticationHash.startRemoveOffset"))
        assertEquals("1002",    properties.getProperty("jummp.authenticationHash.removeInterval"))
        assertEquals("1000",    properties.getProperty("jummp.authenticationHash.maxInactiveTime"))
        assertEquals("false",    properties.getProperty("jummp.plugins.sbml.validation"))
        assertEquals("/tmp/",    properties.getProperty("jummp.plugins.bives.diffdir"))
    }

    void testLoadStoreBrandingConfiguration() {
        populateProperties()
        // verify the configuration
        BrandingCommand branding = configurationService.loadBrandingConfiguration()
        assertEquals("#FFFFFF", branding.internalColor)
        println("branding.externalColor: ${branding.externalColor}")
        assertEquals("null", branding.externalColor)
        // set new configuration
        branding.internalColor = "#BBBBBB"
        branding.externalColor = "#012345"
        configurationService.saveBrandingConfiguration(branding)
        // verify new configuration
        BrandingCommand branding2 = configurationService.loadBrandingConfiguration()
        assertEquals("#BBBBBB", branding2.internalColor)
        assertEquals("#012345", branding2.externalColor)
        // verify that other config options are unchanged
        Properties properties = new Properties()
        properties.load(new FileInputStream("target/jummpProperties"))
        assertEquals(50, properties.size())
        assertEquals("false", properties.getProperty("jummp.firstRun"))
        assertEquals("target", properties.getProperty("jummp.plugins.subversion.localRepository"))
        assertEquals("",           properties.getProperty("jummp.vcs.workingDirectory"))
        assertEquals("",           properties.getProperty("jummp.vcs.exchangeDirectory"))
        assertEquals("subversion", properties.getProperty("jummp.vcs.plugin"))
        assertEquals("ldap",     properties.getProperty("jummp.security.authenticationBackend"))
        assertEquals("true",     properties.getProperty("jummp.security.ldap.enabled"))
        assertEquals("true",     properties.getProperty("jummp.security.ldap.search.subTree"))
        assertEquals("filter",   properties.getProperty("jummp.security.ldap.search.filter"))
        assertEquals("search",   properties.getProperty("jummp.security.ldap.search.base"))
        assertEquals("password", properties.getProperty("jummp.security.ldap.managerPw"))
        assertEquals("manager",  properties.getProperty("jummp.security.ldap.managerDn"))
        assertEquals("server",   properties.getProperty("jummp.security.ldap.server"))
        assertEquals("jummp",     properties.getProperty("jummp.database.database"))
        assertEquals("localhost", properties.getProperty("jummp.database.server"))
        assertEquals("3306",      properties.getProperty("jummp.database.port"))
        assertEquals("user",      properties.getProperty("jummp.database.username"))
        assertEquals("pass",      properties.getProperty("jummp.database.password"))
        assertEquals("jms",   properties.getProperty("jummp.remote"))
        assertEquals("false",   properties.getProperty("jummp.export.dbus"))
        assertEquals("true",   properties.getProperty("jummp.export.jms"))
        assertEquals("false",      properties.getProperty("jummp.plugins.dbus.systemBus"))
        assertEquals("http://127.0.0.1:8080/jummp/", properties.getProperty("jummp.server.url"))
        assertEquals("http://127.0.0.1:8080/jummp-web-application/", properties.getProperty("jummp.server.web.url"))
        assertEquals("true", properties.getProperty("jummp.server.protection"))
        assertEquals("1001",    properties.getProperty("jummp.authenticationHash.startRemoveOffset"))
        assertEquals("1002",    properties.getProperty("jummp.authenticationHash.removeInterval"))
        assertEquals("1000",    properties.getProperty("jummp.authenticationHash.maxInactiveTime"))
        assertEquals("false",    properties.getProperty("jummp.plugins.sbml.validation"))
        assertEquals("/tmp/",    properties.getProperty("jummp.plugins.bives.diffdir"))
    }

    private void populateProperties() {
        configurationService.configurationFile = new File("target/jummpProperties")
        println configurationService.configurationFile
        //configurationService.afterPropertiesSet()
        assertEquals(new File("target/jummpProperties"), configurationService.configurationFile)
        DatabaseCommand database = new DatabaseCommand()
        database.type = "MYSQL"
        database.database = "jummp"
        database.server   = "localhost"
        database.port     = 3306
        database.username = "user"
        database.password = "pass"
        assertTrue(database.validate())
        RemoteCommand remote = new RemoteCommand()
        remote.jummpRemote="jms"
        remote.jummpExportDbus=false
        remote.jummpExportJms=true
        assertTrue(remote.validate())
        DBusCommand dbus = new DBusCommand()
        dbus.systemBus = false
        assertTrue(dbus.validate())
        LdapCommand ldap = new LdapCommand()
        ldap.ldapManagerDn       = "manager"
        ldap.ldapManagerPassword = "password"
        ldap.ldapSearchBase      = "search"
        ldap.ldapSearchFilter    = "filter"
        ldap.ldapSearchSubtree   = true
        ldap.ldapServer          = "server"
        assertTrue(ldap.validate())
        FirstRunCommand firstRun = new FirstRunCommand()
        firstRun.firstRun = "false"
        assertTrue(firstRun.validate())
        SvnCommand svn = new SvnCommand()
        svn.localRepository = "target"
        assertTrue(svn.validate())
        VcsCommand vcs = new VcsCommand()
        vcs.exchangeDirectory = ""
        vcs.workingDirectory = ""
        vcs.vcs = "svn"
        assertTrue(vcs.validate())
        ServerCommand server = new ServerCommand()
        server.url = "http://127.0.0.1:8080/jummp/"
        server.weburl = "http://127.0.0.1:8080/jummp-web-application/"
        server.protectEverything = true
        assertTrue(server.validate())
        UserRegistrationCommand userRegistration = new UserRegistrationCommand()
        userRegistration.registration = true
        userRegistration.sendEmail = true
        userRegistration.sendToAdmin = true
        userRegistration.senderAddress = "test@example.com"
        userRegistration.adminAddress = "admin@example.com"
        userRegistration.url = "http://www.example.com"
        userRegistration.body = "This is the mail body"
        userRegistration.subject = "This is the subject"
        userRegistration.activationSubject = "This is the activation Subject"
        userRegistration.activationBody = "This is the activation body"
        userRegistration.activationUrl = "http://www.example.org"
        assertTrue(userRegistration.validate())
        ChangePasswordCommand changePassword = new ChangePasswordCommand()
        changePassword.changePassword = true
        changePassword.resetPassword  = true
        changePassword.senderAddress  = "test@example.com"
        changePassword.subject        = "Password Forgotten mail"
        changePassword.body           = "Body of the password forgotten mail"
        changePassword.url            = "http://www.example.com/"
        assertTrue(changePassword.validate())
        TriggerCommand trigger = new TriggerCommand()
        trigger.maxInactiveTime = 1000
        trigger.startRemoveOffset = 1001
        trigger.removeInterval = 1002
        assertTrue(trigger.validate())
        SBMLCommand sbml = new SBMLCommand()
        sbml.validation = false
        assertTrue(sbml.validate())
        BivesCommand bives = new BivesCommand()
        bives.diffDir = "/tmp/"
        assertTrue(bives.validate())
        BrandingCommand branding = new BrandingCommand()
        branding.internalColor = "#FFFFFF"
        assertTrue(branding.validate())

        // everything should be written to the properties file
        configurationService.storeConfiguration(database, ldap, vcs, svn, firstRun, server, userRegistration, changePassword, remote, dbus, trigger, sbml, bives, branding)
        Properties properties = new Properties()
        properties.load(new FileInputStream("target/jummpProperties"))
        assertEquals(50, properties.size())
        assertEquals("false", properties.getProperty("jummp.firstRun"))
        assertEquals("target", properties.getProperty("jummp.plugins.subversion.localRepository"))
        assertEquals("",           properties.getProperty("jummp.vcs.workingDirectory"))
        assertEquals("",           properties.getProperty("jummp.vcs.exchangeDirectory"))
        assertEquals("subversion", properties.getProperty("jummp.vcs.plugin"))
        assertEquals("ldap",     properties.getProperty("jummp.security.authenticationBackend"))
        assertEquals("true",     properties.getProperty("jummp.security.ldap.enabled"))
        assertEquals("true",     properties.getProperty("jummp.security.ldap.search.subTree"))
        assertEquals("filter",   properties.getProperty("jummp.security.ldap.search.filter"))
        assertEquals("search",   properties.getProperty("jummp.security.ldap.search.base"))
        assertEquals("password", properties.getProperty("jummp.security.ldap.managerPw"))
        assertEquals("manager",  properties.getProperty("jummp.security.ldap.managerDn"))
        assertEquals("server",   properties.getProperty("jummp.security.ldap.server"))
        assertEquals("MYSQL",     properties.getProperty("jummp.database.type"))
        assertEquals("jummp",     properties.getProperty("jummp.database.database"))
        assertEquals("localhost", properties.getProperty("jummp.database.server"))
        assertEquals("3306",      properties.getProperty("jummp.database.port"))
        assertEquals("user",      properties.getProperty("jummp.database.username"))
        assertEquals("pass",      properties.getProperty("jummp.database.password"))
        assertEquals("jms",   properties.getProperty("jummp.remote"))
        assertEquals("false",   properties.getProperty("jummp.export.dbus"))
        assertEquals("true",   properties.getProperty("jummp.export.jms"))
        assertEquals("false",   properties.getProperty("jummp.plugins.dbus.systemBus"))
        assertEquals("http://127.0.0.1:8080/jummp/", properties.getProperty("jummp.server.url"))
        assertEquals("http://127.0.0.1:8080/jummp-web-application/", properties.getProperty("jummp.server.web.url"))
        assertEquals("true", properties.getProperty("jummp.server.protection"))
        assertEquals("true",                  properties.getProperty("jummp.security.anonymousRegistration"))
        assertEquals("true",                  properties.getProperty("jummp.security.registration.email.send"))
        assertEquals("true",                  properties.getProperty("jummp.security.registration.email.sendToAdmin"))
        assertEquals("test@example.com",      properties.getProperty("jummp.security.registration.email.sender"))
        assertEquals("admin@example.com",     properties.getProperty("jummp.security.registration.email.adminAddress"))
        assertEquals("This is the mail body", properties.getProperty("jummp.security.registration.email.body"))
        assertEquals("This is the subject",   properties.getProperty("jummp.security.registration.email.subject"))
        assertEquals("This is the activation Subject", properties.getProperty("jummp.security.activation.email.subject"))
        assertEquals("This is the activation body",    properties.getProperty("jummp.security.activation.email.body"))
        assertEquals("http://www.example.org/register/confirmRegistration/{{CODE}}", properties.getProperty("jummp.security.activation.activationURL"))
        assertEquals("http://www.example.com/register/validate/{{CODE}}", properties.getProperty("jummp.security.registration.verificationURL"))
        assertEquals("true",                                properties.getProperty("jummp.security.ui.changePassword"))
        assertEquals("true",                                properties.getProperty("jummp.security.resetPassword.email.send"))
        assertEquals("test@example.com",                    properties.getProperty("jummp.security.resetPassword.email.sender"))
        assertEquals("Password Forgotten mail",             properties.getProperty("jummp.security.resetPassword.email.subject"))
        assertEquals("Body of the password forgotten mail", properties.getProperty("jummp.security.resetPassword.email.body"))
        assertEquals("http://www.example.com/user/resetPassword/{{CODE}}", properties.getProperty("jummp.security.resetPassword.url"))
        assertEquals("1001",    properties.getProperty("jummp.authenticationHash.startRemoveOffset"))
        assertEquals("1002",    properties.getProperty("jummp.authenticationHash.removeInterval"))
        assertEquals("1000",    properties.getProperty("jummp.authenticationHash.maxInactiveTime"))
        assertEquals("false",    properties.getProperty("jummp.plugins.sbml.validation"))
        assertEquals("/tmp/",    properties.getProperty("jummp.plugins.bives.diffdir"))
        assertEquals("#FFFFFF",    properties.getProperty("jummp.branding.internalColor"))
    }
}
