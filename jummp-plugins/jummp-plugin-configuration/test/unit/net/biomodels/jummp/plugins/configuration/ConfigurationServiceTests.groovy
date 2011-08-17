package net.biomodels.jummp.plugins.configuration

import grails.test.*

class ConfigurationServiceTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testConfigurationFile() {
        // default configuration file is in home directory
        ConfigurationService service = new ConfigurationService()
        assertNull(service.configurationFile)
        service.afterPropertiesSet()
        assertNotNull(service.configurationFile)
        assertEquals(new File(System.getProperty("user.home") + System.getProperty("file.separator") + ".jummp.properties"), service.configurationFile)
        // test setting a file
        service = new ConfigurationService()
        assertNull(service.configurationFile)
        service.configurationFile = new File("target/jummp.properties")
        assertNotNull(service.configurationFile)
        service.afterPropertiesSet()
        assertEquals(new File("target/jummp.properties"), service.configurationFile)
    }

    void testUpdateMysqlConfiguration() {
        ConfigurationService service = new ConfigurationService()
        shouldFail(NullPointerException) {
            service.updateMysqlConfiguration(null, null)
        }
        Properties properties = new Properties()
        shouldFail(NullPointerException) {
            service.updateMysqlConfiguration(properties, null)
        }
        mockForConstraintsTests(MysqlCommand)
        MysqlCommand mysql = new MysqlCommand()
        service.updateMysqlConfiguration(properties, mysql)
        assertTrue(properties.isEmpty())
        // set values
        mysql = new MysqlCommand()
        mysql.database = "jummp"
        mysql.server   = "localhost"
        mysql.port     = 3306
        mysql.username = "user"
        mysql.password = "pass"
        service.updateMysqlConfiguration(properties, mysql)
        assertFalse(properties.isEmpty())
        assertEquals(5, properties.size())
        assertEquals("jummp", properties.getProperty("jummp.database.database"))
        assertEquals("localhost", properties.getProperty("jummp.database.server"))
        assertEquals("3306", properties.getProperty("jummp.database.port"))
        assertEquals("user", properties.getProperty("jummp.database.username"))
        assertEquals("pass", properties.getProperty("jummp.database.password"))
        // update values
        mysql = new MysqlCommand()
        mysql.database = "database"
        mysql.server   = "host"
        mysql.port     = 1234
        mysql.username = "name"
        mysql.password = ""
        service.updateMysqlConfiguration(properties, mysql)
        assertFalse(properties.isEmpty())
        assertEquals(5, properties.size())
        assertEquals("database", properties.getProperty("jummp.database.database"))
        assertEquals("host", properties.getProperty("jummp.database.server"))
        assertEquals("1234", properties.getProperty("jummp.database.port"))
        assertEquals("name", properties.getProperty("jummp.database.username"))
        assertEquals("", properties.getProperty("jummp.database.password"))
        // invalid command should not change
        service.updateMysqlConfiguration(properties, new MysqlCommand())
        assertFalse(properties.isEmpty())
        assertEquals(5, properties.size())
        assertEquals("database", properties.getProperty("jummp.database.database"))
        assertEquals("host", properties.getProperty("jummp.database.server"))
        assertEquals("1234", properties.getProperty("jummp.database.port"))
        assertEquals("name", properties.getProperty("jummp.database.username"))
        assertEquals("", properties.getProperty("jummp.database.password"))
    }

    void testUpdateRemoteConfiguration() {
        ConfigurationService service = new ConfigurationService()
        shouldFail(NullPointerException) {
            service.updateRemoteConfiguration(null, null)
        }
        Properties properties = new Properties()
        shouldFail(NullPointerException) {
            service.updateRemoteConfiguration(properties, null)
        }
        mockForConstraintsTests(RemoteCommand)
        RemoteCommand remote = new RemoteCommand()
        service.updateRemoteConfiguration(properties, remote)
        assertTrue(properties.isEmpty())
        // set values
        remote = new RemoteCommand()
        remote.jummpRemote = "jms"
        remote.jummpExportDbus=false
        remote.jummpExportJms=true
        service.updateRemoteConfiguration(properties, remote)
        assertFalse(properties.isEmpty())
        assertEquals(3, properties.size())
        assertEquals("jms", properties.getProperty("jummp.remote"))
        assertEquals("false", properties.getProperty("jummp.export.dbus"))
        assertEquals("true", properties.getProperty("jummp.export.jms"))
        // update values
        remote = new RemoteCommand()
        remote.jummpRemote = "dbus"
        remote.jummpExportDbus=true
        remote.jummpExportJms=false
        service.updateRemoteConfiguration(properties, remote)
        assertFalse(properties.isEmpty())
        assertEquals(3, properties.size())
        assertEquals("dbus", properties.getProperty("jummp.remote"))
        assertEquals("true", properties.getProperty("jummp.export.dbus"))
        assertEquals("false", properties.getProperty("jummp.export.jms"))
        // invalid command should not change
        service.updateRemoteConfiguration(properties, new RemoteCommand())
        assertFalse(properties.isEmpty())
        assertEquals(3, properties.size())
        assertEquals("dbus", properties.getProperty("jummp.remote"))
        assertEquals("true", properties.getProperty("jummp.export.dbus"))
        assertEquals("false", properties.getProperty("jummp.export.jms"))
    }

    void testUpdateLdapConfiguration() {
        ConfigurationService service = new ConfigurationService()
        shouldFail(NullPointerException) {
            service.updateLdapConfiguration(null, null)
        }
        Properties properties = new Properties()
        // null ldap should disable ldap usage
        service.updateLdapConfiguration(properties, null)
        assertFalse(properties.isEmpty())
        assertEquals(1, properties.size())
        assertEquals("false", properties.getProperty("jummp.security.ldap.enabled"))

        // test with empty ldap command
        properties = new Properties()
        mockForConstraintsTests(LdapCommand)
        LdapCommand ldap = new LdapCommand()
        assertTrue(properties.isEmpty())
        service.updateLdapConfiguration(properties, ldap)
        assertTrue(properties.isEmpty())
        // test with minimum values
        ldap = new LdapCommand()
        ldap.ldapManagerDn       = "manager"
        ldap.ldapManagerPassword = "password"
        ldap.ldapSearchBase      = "search"
        ldap.ldapSearchFilter    = "filter"
        ldap.ldapSearchSubtree   = true
        ldap.ldapServer          = "server"
        service.updateLdapConfiguration(properties, ldap)
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
        ldap = new LdapCommand()
        ldap.ldapManagerDn       = "ldap"
        ldap.ldapManagerPassword = "1234"
        ldap.ldapSearchBase      = "cn"
        ldap.ldapSearchFilter    = "ab"
        ldap.ldapSearchSubtree   = false
        ldap.ldapServer          = "localhost"
        service.updateLdapConfiguration(properties, ldap)
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
        service.updateLdapConfiguration(properties, new LdapCommand())
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
        service.updateLdapConfiguration(properties, null)
        assertFalse(properties.isEmpty())
        assertEquals(1, properties.size())
        assertEquals("false", properties.getProperty("jummp.security.ldap.enabled"))
    }

    void testUpdateVcsConfiguration() {
        ConfigurationService service = new ConfigurationService()
        shouldFail(NullPointerException) {
            service.updateVcsConfiguration(null, null)
        }
        Properties properties = new Properties()
        shouldFail(NullPointerException) {
            service.updateVcsConfiguration(properties, null)
        }
        mockForConstraintsTests(VcsCommand)
        // should not update when command is not valid
        VcsCommand vcs = new VcsCommand()
        service.updateVcsConfiguration(properties, vcs)
        assertTrue(properties.isEmpty())
        vcs = new VcsCommand()
        vcs.exchangeDirectory = ""
        vcs.workingDirectory = ""
        vcs.vcs = "svn"
        service.updateVcsConfiguration(properties, vcs)
        assertFalse(properties.isEmpty())
        assertEquals(3, properties.size())
        assertEquals("subversion", properties.getProperty("jummp.vcs.plugin"))
        assertEquals("", properties.getProperty("jummp.vcs.exchangeDirectory"))
        assertEquals("", properties.getProperty("jummp.vcs.workingDirectory"))
        // invalid command should not change
        service.updateVcsConfiguration(properties, new VcsCommand())
        assertFalse(properties.isEmpty())
        assertEquals(3, properties.size())
        assertEquals("subversion", properties.getProperty("jummp.vcs.plugin"))
        assertEquals("", properties.getProperty("jummp.vcs.exchangeDirectory"))
        assertEquals("", properties.getProperty("jummp.vcs.workingDirectory"))
        // update to different settings
        vcs = new VcsCommand()
        vcs.exchangeDirectory = "target"
        vcs.workingDirectory = "."
        vcs.vcs = "git"
        service.updateVcsConfiguration(properties, vcs)
        assertFalse(properties.isEmpty())
        assertEquals(3, properties.size())
        assertEquals("git", properties.getProperty("jummp.vcs.plugin"))
        assertEquals("target", properties.getProperty("jummp.vcs.exchangeDirectory"))
        assertEquals(".", properties.getProperty("jummp.vcs.workingDirectory"))
    }

    void testUpdateSvnConfiguration() {
        ConfigurationService service = new ConfigurationService()
        shouldFail(NullPointerException) {
            service.updateSvnConfiguration(null, null)
        }
        Properties properties = new Properties()
        mockForConstraintsTests(SvnCommand)
        SvnCommand svn = new SvnCommand()
        service.updateSvnConfiguration(properties, svn)
        assertTrue(properties.isEmpty())
        svn = new SvnCommand()
        svn.localRepository = "target"
        service.updateSvnConfiguration(properties, svn)
        assertFalse(properties.isEmpty())
        assertEquals(1, properties.size())
        assertEquals("target", properties.getProperty("jummp.plugins.subversion.localRepository"))
        // update configuration
        svn = new SvnCommand()
        svn.localRepository = "."
        service.updateSvnConfiguration(properties, svn)
        assertFalse(properties.isEmpty())
        assertEquals(1, properties.size())
        assertEquals(".", properties.getProperty("jummp.plugins.subversion.localRepository"))
        // invalid should not change
        service.updateSvnConfiguration(properties, new SvnCommand())
        assertFalse(properties.isEmpty())
        assertEquals(1, properties.size())
        assertEquals(".", properties.getProperty("jummp.plugins.subversion.localRepository"))
        // passing in null should delete the property
        service.updateSvnConfiguration(properties, null)
        assertTrue(properties.isEmpty())
    }

    void testUpdateFirstRunConfiguration() {
        ConfigurationService service = new ConfigurationService()
        shouldFail(NullPointerException) {
            service.updateFirstRunConfiguration(null, null)
        }
        Properties properties = new Properties()
        shouldFail(NullPointerException) {
            service.updateFirstRunConfiguration(properties, null)
        }
        mockForConstraintsTests(FirstRunCommand)
        // invalid should not work
        FirstRunCommand firstRun = new FirstRunCommand()
        service.updateFirstRunConfiguration(properties, firstRun)
        assertTrue(properties.isEmpty())
        // passing in a correct command should update
        firstRun = new FirstRunCommand()
        firstRun.firstRun = "true"
        service.updateFirstRunConfiguration(properties, firstRun)
        assertFalse(properties.isEmpty())
        assertEquals(1, properties.size())
        assertEquals("true", properties.getProperty("jummp.firstRun"))
        // passing in different options should update
        firstRun = new FirstRunCommand()
        firstRun.firstRun = "false"
        service.updateFirstRunConfiguration(properties, firstRun)
        assertFalse(properties.isEmpty())
        assertEquals(1, properties.size())
        assertEquals("false", properties.getProperty("jummp.firstRun"))
        // passing in invalid command should not update
        service.updateFirstRunConfiguration(properties, new FirstRunCommand())
        assertFalse(properties.isEmpty())
        assertEquals(1, properties.size())
        assertEquals("false", properties.getProperty("jummp.firstRun"))
    }

    void testUpdateServerConfiguration() {
        ConfigurationService service = new ConfigurationService()
        shouldFail(NullPointerException) {
            service.updateServerConfiguration(null, null)
        }
        Properties properties = new Properties()
        shouldFail(NullPointerException) {
            service.updateServerConfiguration(properties, null)
        }
        mockForConstraintsTests(ServerCommand)
        // invalid should not work
        ServerCommand server = new ServerCommand()
        service.updateServerConfiguration(properties, server)
        assertTrue(properties.isEmpty())
        // passing in a correct command should update
        server = new ServerCommand()
        server.url = "http://127.0.0.1:8080/jummp/"
        service.updateServerConfiguration(properties, server)
        assertFalse(properties.isEmpty())
        assertEquals(1, properties.size())
        assertEquals("http://127.0.0.1:8080/jummp/", properties.getProperty("jummp.server.url"))
        // passing in different options should update
        server = new ServerCommand()
        server.url = "http://www.example.com/"
        service.updateServerConfiguration(properties, server)
        assertFalse(properties.isEmpty())
        assertEquals(1, properties.size())
        assertEquals("http://www.example.com/", properties.getProperty("jummp.server.url"))
        // passing in incalid vommand should not update
        service.updateServerConfiguration(properties, new ServerCommand())
        assertFalse(properties.isEmpty())
        assertEquals(1, properties.size())
        assertEquals("http://www.example.com/", properties.getProperty("jummp.server.url"))
    }

    void testStoreConfiguration() {
        ConfigurationService service = new ConfigurationService()
        mockForConstraintsTests(FirstRunCommand)
        mockForConstraintsTests(LdapCommand)
        mockForConstraintsTests(MysqlCommand)
        mockForConstraintsTests(RemoteCommand)
        mockForConstraintsTests(SvnCommand)
        mockForConstraintsTests(VcsCommand)
        mockForConstraintsTests(ServerCommand)
        mockForConstraintsTests(UserRegistrationCommand)
        mockForConstraintsTests(ChangePasswordCommand)
        mockForConstraintsTests(TriggerCommand)
        mockForConstraintsTests(BivesCommand)
        // set a file to use
        service.configurationFile = new File("target/jummpProperties")
        service.configurationFile.delete()
        service.afterPropertiesSet()
        assertEquals(new File("target/jummpProperties"), service.configurationFile)
        MysqlCommand mysql = new MysqlCommand()
        mysql.database = "jummp"
        mysql.server   = "localhost"
        mysql.port     = 3306
        mysql.username = "user"
        mysql.password = "pass"
        assertTrue(mysql.validate())
        RemoteCommand remote = new RemoteCommand()
        remote.jummpRemote="jms"
        remote.jummpExportDbus=false
        remote.jummpExportJms=true
        assertTrue(remote.validate())
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
        BivesCommand bives = new BivesCommand()
        bives.diffDir = "/tmp"
        assertTrue(bives.validate())

        // everything should be written to the properties file
        service.storeConfiguration(mysql, ldap, vcs, svn, firstRun, server, userRegistration, changePassword, remote, trigger, bives)
        Properties properties = new Properties()
        properties.load(new FileInputStream("target/jummpProperties"))
        assertEquals(43, properties.size())
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
        assertEquals("jms",      properties.getProperty("jummp.remote"))
        assertEquals("false",      properties.getProperty("jummp.export.dbus"))
        assertEquals("true",      properties.getProperty("jummp.export.jms"))
        assertEquals("http://127.0.0.1:8080/jummp/", properties.getProperty("jummp.server.url"))
        assertEquals("1001",    properties.getProperty("jummp.authenticationHash.startRemoveOffset"))
        assertEquals("1002",    properties.getProperty("jummp.authenticationHash.removeInterval"))
        assertEquals("1000",    properties.getProperty("jummp.authenticationHash.maxInactiveTime"))
        assertEquals("/tmp",    properties.getProperty("jummp.plugins.bives.diffdir"))

        // change configuration - no ldap, no svn
        firstRun = new FirstRunCommand()
        firstRun.firstRun = "true"
        assertTrue(firstRun.validate())
        vcs = new VcsCommand()
        vcs.exchangeDirectory = ""
        vcs.workingDirectory = "target"
        vcs.vcs = "git"
        assertTrue(vcs.validate())

        service.storeConfiguration(mysql, null, vcs, null, firstRun, server, userRegistration, changePassword, remote, trigger, bives)
        properties = new Properties()
        properties.load(new FileInputStream("target/jummpProperties"))
        assertEquals(19, properties.size())
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
    }

    void testLoadStoreMysqlConfiguration() {
        ConfigurationService service = new ConfigurationService()
        mockForConstraintsTests(FirstRunCommand)
        mockForConstraintsTests(LdapCommand)
        mockForConstraintsTests(MysqlCommand)
        mockForConstraintsTests(RemoteCommand)
        mockForConstraintsTests(SvnCommand)
        mockForConstraintsTests(VcsCommand)
        mockForConstraintsTests(ServerCommand)
        mockForConstraintsTests(UserRegistrationCommand)
        mockForConstraintsTests(ChangePasswordCommand)
        mockForConstraintsTests(TriggerCommand)
        mockForConstraintsTests(BivesCommand)
        populateProperties(service)

        // now load the data from the service
        MysqlCommand mysql = service.loadMysqlConfiguration()
        assertEquals("jummp",     mysql.database)
        assertEquals("localhost", mysql.server)
        assertEquals(3306,        mysql.port)
        assertEquals("user",      mysql.username)
        assertEquals("pass",      mysql.password)

        // store a new configuration
        mysql = new MysqlCommand()
        mysql.database = "database"
        mysql.server   = "server"
        mysql.port     = 1234
        mysql.username = "name"
        mysql.password = "secret"
        service.saveMysqlConfiguration(mysql)
        // verify the configuration
        MysqlCommand config = service.loadMysqlConfiguration()
        assertEquals("database", config.database)
        assertEquals("server",   config.server)
        assertEquals(1234,       config.port)
        assertEquals("name",     config.username)
        assertEquals("secret",   config.password)
        // verify that other config options are unchanged
        Properties properties = new Properties()
        properties.load(new FileInputStream("target/jummpProperties"))
        assertEquals(43, properties.size())
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
        assertEquals("http://127.0.0.1:8080/jummp/", properties.getProperty("jummp.server.url"))
    }

    void testLoadStoreRemoteConfiguration() {
        ConfigurationService service = new ConfigurationService()
        mockForConstraintsTests(FirstRunCommand)
        mockForConstraintsTests(LdapCommand)
        mockForConstraintsTests(MysqlCommand)
        mockForConstraintsTests(RemoteCommand)
        mockForConstraintsTests(SvnCommand)
        mockForConstraintsTests(VcsCommand)
        mockForConstraintsTests(ServerCommand)
        mockForConstraintsTests(UserRegistrationCommand)
        mockForConstraintsTests(ChangePasswordCommand)
        mockForConstraintsTests(TriggerCommand)
        mockForConstraintsTests(BivesCommand)
        populateProperties(service)

        // now load the data from the service
        RemoteCommand remote = service.loadRemoteConfiguration()
        assertEquals("jms",  remote.jummpRemote)
        assertFalse(remote.jummpExportDbus)
        assertTrue( remote.jummpExportJms)

        // store a new configuration
        remote = new RemoteCommand()
        remote.jummpRemote="dbus"
        remote.jummpExportDbus=true
        remote.jummpExportJms=false
        service.saveRemoteConfiguration(remote)
        // verify the configuration
        RemoteCommand config = service.loadRemoteConfiguration()
        assertEquals("dbus", config.jummpRemote)
        assertEquals(true,   config.jummpExportDbus)
        assertEquals(false,       config.jummpExportJms)
        // verify that other config options are unchanged
        Properties properties = new Properties()
        properties.load(new FileInputStream("target/jummpProperties"))
        assertEquals(43, properties.size())
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
    }

    void testLoadStoreLdapConfiguration() {
        ConfigurationService service = new ConfigurationService()
        mockForConstraintsTests(FirstRunCommand)
        mockForConstraintsTests(LdapCommand)
        mockForConstraintsTests(MysqlCommand)
        mockForConstraintsTests(RemoteCommand)
        mockForConstraintsTests(SvnCommand)
        mockForConstraintsTests(VcsCommand)
        mockForConstraintsTests(ServerCommand)
        mockForConstraintsTests(UserRegistrationCommand)
        mockForConstraintsTests(ChangePasswordCommand)
        mockForConstraintsTests(TriggerCommand)
        mockForConstraintsTests(BivesCommand)
        populateProperties(service)
        // Load the data from properties
        LdapCommand ldap = service.loadLdapConfiguration()
        assertTrue(ldap.ldapSearchSubtree)
        assertEquals("filter",   ldap.ldapSearchFilter)
        assertEquals("search",   ldap.ldapSearchBase)
        assertEquals("password", ldap.ldapManagerPassword)
        assertEquals("manager",  ldap.ldapManagerDn)
        assertEquals("server",   ldap.ldapServer)
        // store a new configuration
        ldap = new LdapCommand()
        ldap.ldapSearchSubtree = false
        ldap.ldapSearchFilter = "*"
        ldap.ldapSearchBase = "base"
        ldap.ldapManagerPassword = "secure"
        ldap.ldapManagerDn = "cn"
        ldap.ldapServer = "localhost"
        service.saveLdapConfiguration(ldap)
        // verify new configuration
        LdapCommand ldap2 = service.loadLdapConfiguration()
        assertFalse(ldap2.ldapSearchSubtree)
        assertEquals("*",         ldap2.ldapSearchFilter)
        assertEquals("base",      ldap2.ldapSearchBase)
        assertEquals("secure",    ldap2.ldapManagerPassword)
        assertEquals("cn",        ldap2.ldapManagerDn)
        assertEquals("localhost", ldap2.ldapServer)
        // verify that other config options are unchanged
        Properties properties = new Properties()
        properties.load(new FileInputStream("target/jummpProperties"))
        assertEquals(43, properties.size())
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
        assertEquals("jms",   properties.getProperty("jummp.remote"))
        assertEquals("false",   properties.getProperty("jummp.export.dbus"))
        assertEquals("true",   properties.getProperty("jummp.export.jms"))
        assertEquals("http://127.0.0.1:8080/jummp/", properties.getProperty("jummp.server.url"))
    }

    void testLoadStoreSvnConfiguration() {
        ConfigurationService service = new ConfigurationService()
        mockForConstraintsTests(FirstRunCommand)
        mockForConstraintsTests(LdapCommand)
        mockForConstraintsTests(MysqlCommand)
        mockForConstraintsTests(RemoteCommand)
        mockForConstraintsTests(SvnCommand)
        mockForConstraintsTests(VcsCommand)
        mockForConstraintsTests(ServerCommand)
        mockForConstraintsTests(UserRegistrationCommand)
        mockForConstraintsTests(ChangePasswordCommand)
        mockForConstraintsTests(TriggerCommand)
        mockForConstraintsTests(BivesCommand)
        populateProperties(service)
        // verify svn configuration
        SvnCommand svn = service.loadSvnConfiguration()
        assertEquals("target", svn.localRepository)
        // set new configuration
        svn = new SvnCommand()
        svn.localRepository = "/tmp"
        service.saveSvnConfiguration(svn)
        // verify new configuration
        SvnCommand svn2 = service.loadSvnConfiguration()
        assertEquals("/tmp", svn2.localRepository)
        // verify that other config options are unchanged
        Properties properties = new Properties()
        properties.load(new FileInputStream("target/jummpProperties"))
        assertEquals(43, properties.size())
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
        assertEquals("http://127.0.0.1:8080/jummp/", properties.getProperty("jummp.server.url"))
    }

    void testLoadStoreVcsConfiguration() {
        ConfigurationService service = new ConfigurationService()
        mockForConstraintsTests(FirstRunCommand)
        mockForConstraintsTests(LdapCommand)
        mockForConstraintsTests(MysqlCommand)
        mockForConstraintsTests(RemoteCommand)
        mockForConstraintsTests(SvnCommand)
        mockForConstraintsTests(VcsCommand)
        mockForConstraintsTests(ServerCommand)
        mockForConstraintsTests(UserRegistrationCommand)
        mockForConstraintsTests(ChangePasswordCommand)
        mockForConstraintsTests(TriggerCommand)
        mockForConstraintsTests(BivesCommand)
        populateProperties(service)
        // verify the configuration
        VcsCommand vcs = service.loadVcsConfiguration()
        assertEquals("svn", vcs.vcs)
        assertEquals("", vcs.exchangeDirectory)
        assertEquals("", vcs.workingDirectory)
        // set new configuration
        vcs = new VcsCommand()
        vcs.vcs = "git"
        vcs.exchangeDirectory = "/tmp"
        vcs.workingDirectory = "target"
        service.saveVcsConfiguration(vcs)
        // verify new configuration
        VcsCommand vcs2 = service.loadVcsConfiguration()
        assertEquals("git", vcs2.vcs)
        assertEquals("/tmp", vcs2.exchangeDirectory)
        assertEquals("target", vcs2.workingDirectory)
        // verify that other config options are unchanged
        Properties properties = new Properties()
        properties.load(new FileInputStream("target/jummpProperties"))
        assertEquals(43, properties.size())
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
        assertEquals("http://127.0.0.1:8080/jummp/", properties.getProperty("jummp.server.url"))
    }

    void testLoadStoreServerConfiguration() {
        ConfigurationService service = new ConfigurationService()
        mockForConstraintsTests(FirstRunCommand)
        mockForConstraintsTests(LdapCommand)
        mockForConstraintsTests(MysqlCommand)
        mockForConstraintsTests(RemoteCommand)
        mockForConstraintsTests(SvnCommand)
        mockForConstraintsTests(VcsCommand)
        mockForConstraintsTests(ServerCommand)
        mockForConstraintsTests(UserRegistrationCommand)
        mockForConstraintsTests(ChangePasswordCommand)
        mockForConstraintsTests(TriggerCommand)
        mockForConstraintsTests(BivesCommand)
        populateProperties(service)
        // verify the configuration
        ServerCommand server = service.loadServerConfiguration()
        assertEquals("http://127.0.0.1:8080/jummp/", server.url)
        // set new configuration
        server = new ServerCommand()
        server.url = "https://www.example.com/"
        service.saveServerConfiguration(server)
        // verify new configuration
        ServerCommand server2 = service.loadServerConfiguration()
        assertEquals("https://www.example.com/", server2.url)
        // verify that other configuration options are unchanged
        Properties properties = new Properties()
        properties.load(new FileInputStream("target/jummpProperties"))
        assertEquals(43, properties.size())
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
    }

    private void populateProperties(ConfigurationService service) {
        service.configurationFile = new File("target/jummpProperties")
        service.configurationFile.delete()
        service.afterPropertiesSet()
        assertEquals(new File("target/jummpProperties"), service.configurationFile)
        MysqlCommand mysql = new MysqlCommand()
        mysql.database = "jummp"
        mysql.server   = "localhost"
        mysql.port     = 3306
        mysql.username = "user"
        mysql.password = "pass"
        assertTrue(mysql.validate())
        RemoteCommand remote = new RemoteCommand()
        remote.jummpRemote="jms"
        remote.jummpExportDbus=false
        remote.jummpExportJms=true
        assertTrue(remote.validate())
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
        trigger.maxInactiveTime = 2000
        trigger.startRemoveOffset = 2001
        trigger.removeInterval = 2002
        assertTrue(trigger.validate())
        BivesCommand bives = new BivesCommand()
        bives.diffDir = "/tmp/"
        assertTrue(bives.validate())

        // everything should be written to the properties file
        service.storeConfiguration(mysql, ldap, vcs, svn, firstRun, server, userRegistration, changePassword, remote, trigger, bives)
        Properties properties = new Properties()
        properties.load(new FileInputStream("target/jummpProperties"))
        assertEquals(43, properties.size())
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
        assertEquals("http://127.0.0.1:8080/jummp/", properties.getProperty("jummp.server.url"))
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
        assertEquals("2001",    properties.getProperty("jummp.authenticationHash.startRemoveOffset"))
        assertEquals("2002",    properties.getProperty("jummp.authenticationHash.removeInterval"))
        assertEquals("2000",    properties.getProperty("jummp.authenticationHash.maxInactiveTime"))
        assertEquals("/tmp/",    properties.getProperty("jummp.plugins.bives.diffdir"))
    }
}
