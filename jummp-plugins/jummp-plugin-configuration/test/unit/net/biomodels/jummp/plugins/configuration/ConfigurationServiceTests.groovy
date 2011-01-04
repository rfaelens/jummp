package net.biomodels.jummp.plugins.configuration

import grails.test.*
import net.biomodels.jummp.controllers.FirstRunCommand
import net.biomodels.jummp.controllers.LdapCommand
import net.biomodels.jummp.controllers.MysqlCommand
import net.biomodels.jummp.controllers.SvnCommand
import net.biomodels.jummp.controllers.VcsCommand

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

    void testStoreConfiguration() {
        ConfigurationService service = new ConfigurationService()
        mockForConstraintsTests(FirstRunCommand)
        mockForConstraintsTests(LdapCommand)
        mockForConstraintsTests(MysqlCommand)
        mockForConstraintsTests(SvnCommand)
        mockForConstraintsTests(VcsCommand)
        // set a file to use
        service.configurationFile = new File("target/immunoblotProperties")
        service.configurationFile.delete()
        service.afterPropertiesSet()
        assertEquals(new File("target/immunoblotProperties"), service.configurationFile)
        MysqlCommand mysql = new MysqlCommand()
        mysql.database = "jummp"
        mysql.server   = "localhost"
        mysql.port     = 3306
        mysql.username = "user"
        mysql.password = "pass"
        assertTrue(mysql.validate())
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

        // everything should be written to the properties file
        service.storeConfiguration(mysql, ldap, vcs, svn, firstRun)
        Properties properties = new Properties()
        properties.load(new FileInputStream("target/immunoblotProperties"))
        assertEquals(18, properties.size())
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

        // change configuration - no ldap, no svn
        firstRun = new FirstRunCommand()
        firstRun.firstRun = "true"
        assertTrue(firstRun.validate())
        vcs = new VcsCommand()
        vcs.exchangeDirectory = ""
        vcs.workingDirectory = "target"
        vcs.vcs = "git"
        assertTrue(vcs.validate())

        service.storeConfiguration(mysql, null, vcs, null, firstRun)
        properties = new Properties()
        properties.load(new FileInputStream("target/immunoblotProperties"))
        assertEquals(11, properties.size())
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
    }
}
