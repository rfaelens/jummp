package net.biomodels.jummp.plugins.configuration

import grails.test.*
import org.apache.commons.io.FileUtils


class SetupControllerTests extends ControllerUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testMysqlCommand() {
        mockForConstraintsTests(MysqlCommand)
        // null should fail
        MysqlCommand cmd = new MysqlCommand()
        assertFalse(cmd.validate())
        assertEquals("nullable", cmd.errors["server"])
        assertEquals("nullable", cmd.errors["port"])
        assertEquals("nullable", cmd.errors["database"])
        assertEquals("nullable", cmd.errors["username"])
        assertEquals("nullable", cmd.errors["password"])
        // test for blanks
        cmd = new MysqlCommand()
        cmd.server = ''
        cmd.database = ''
        cmd.username = ''
        cmd.password = ''
        assertFalse(cmd.validate())
        assertEquals("nullable", cmd.errors["port"])
        assertEquals("blank", cmd.errors["server"])
        assertEquals("blank", cmd.errors["database"])
        assertEquals("blank", cmd.errors["username"])
        assertNull(cmd.errors["password"])
        // port in range 0 to 65535
        cmd = new MysqlCommand()
        cmd.port = -1
        assertFalse(cmd.validate())
        assertEquals("range", cmd.errors["port"])
        cmd = new MysqlCommand()
        cmd.port = 65536
        assertFalse(cmd.validate())
        assertEquals("range", cmd.errors["port"])
        cmd = new MysqlCommand()
        cmd.port = 0
        assertFalse(cmd.validate())
        assertNull(cmd.errors["port"])
        cmd = new MysqlCommand()
        cmd.port = 65535
        assertFalse(cmd.validate())
        assertNull(cmd.errors["port"])
        // and one test that should work
        cmd = new MysqlCommand()
        cmd.server = 'localhost'
        cmd.database = 'jummp'
        cmd.username = 'jummp'
        cmd.password = 'jummp'
        cmd.port = 3306
        assertTrue(cmd.validate())
    }

    void testRemoteCommand() {
        mockForConstraintsTests(RemoteCommand)
        // null should fail
        RemoteCommand cmd = new RemoteCommand()
        assertFalse(cmd.validate())
        assertEquals("validator", cmd.errors["jummpExportDbus"])
        assertEquals("validator", cmd.errors["jummpExportJms"])
        // test for blanks
        cmd = new RemoteCommand()
        cmd.jummpRemote=""
        assertFalse(cmd.validate())
        assertEquals("blank", cmd.errors["jummpRemote"])
        // jummpRemote has value in list
        cmd = new RemoteCommand()
        cmd.jummpRemote="smj"
        assertFalse(cmd.validate())
        assertEquals("inList", cmd.errors["jummpRemote"])
        cmd = new RemoteCommand()
        cmd.jummpRemote="subD"
        assertFalse(cmd.validate())
        assertEquals("inList", cmd.errors["jummpRemote"])
        cmd = new RemoteCommand()
        cmd.jummpRemote="jms"
        assertFalse(cmd.validate())
        assertNull(cmd.errors["jummpRemote"])
        cmd = new RemoteCommand()
        cmd.jummpRemote="Dbus"
        assertFalse(cmd.validate())
        // and one test that should work
        cmd = new RemoteCommand()
        cmd.jummpRemote="jms"
        cmd.jummpExportDbus=false
        cmd.jummpExportJms=true
        assertTrue(cmd.validate())
    }

    void testDBusCommand() {
        mockForConstraintsTests(DBusCommand)
        // null should fail
        DBusCommand cmd = new DBusCommand()
        cmd.systemBus = null
        assertFalse(cmd.validate())
        assertEquals("nullable", cmd.errors["systemBus"])
        // test for blanks
        cmd = new DBusCommand()
        cmd.systemBus
        assertFalse(cmd.validate())
        assertEquals("nullable", cmd.errors["systemBus"])
        // test for null
        cmd = new DBusCommand()
        cmd.systemBus = null
        assertFalse(cmd.validate())
        assertEquals("nullable", cmd.errors["systemBus"])
        // this test should work
        cmd = new DBusCommand()
        cmd.systemBus = false
        assertTrue(cmd.validate())
    }

    void testLdapCommand() {
        mockForConstraintsTests(LdapCommand)
        // null should fail
        LdapCommand cmd = new LdapCommand()
        assertFalse(cmd.validate())
        assertEquals("nullable", cmd.errors["ldapServer"])
        assertEquals("nullable", cmd.errors["ldapManagerDn"])
        assertEquals("nullable", cmd.errors["ldapManagerPassword"])
        assertEquals("nullable", cmd.errors["ldapSearchBase"])
        assertEquals("nullable", cmd.errors["ldapSearchFilter"])
        assertEquals("nullable", cmd.errors["ldapSearchSubtree"])
        // blank should fail some
        cmd = new LdapCommand()
        cmd.ldapServer = ""
        cmd.ldapManagerDn = ""
        cmd.ldapManagerPassword = ""
        // allowed
        cmd.ldapSearchBase = ""
        cmd.ldapSearchFilter = ""
        assertFalse(cmd.validate())
        assertEquals("blank", cmd.errors["ldapServer"])
        assertEquals("blank", cmd.errors["ldapManagerDn"])
        assertEquals("blank", cmd.errors["ldapManagerPassword"])
        assertNull(cmd.errors["ldapSearchBase"])
        assertNull(cmd.errors["ldapSearchFilter"])
        assertEquals("nullable", cmd.errors["ldapSearchSubtree"])
        // TODO: test for a LDAP URL
        // setting all fields except the boolean should only fail the boolean
        cmd = new LdapCommand()
        cmd.ldapServer = "foo"
        cmd.ldapManagerDn = "bar"
        cmd.ldapManagerPassword = "baz"
        cmd.ldapSearchBase = "foobar"
        cmd.ldapSearchFilter = "foobarbaz"
        assertFalse(cmd.validate())
        assertNull(cmd.errors["ldapServer"])
        assertNull(cmd.errors["ldapManagerDn"])
        assertNull(cmd.errors["ldapManagerPassword"])
        assertNull(cmd.errors["ldapSearchBase"])
        assertNull(cmd.errors["ldapSearchFilter"])
        assertEquals("nullable", cmd.errors["ldapSearchSubtree"])
        // last but not least - a successful test
        cmd = new LdapCommand()
        cmd.ldapServer = "foo"
        cmd.ldapManagerDn = "bar"
        cmd.ldapManagerPassword = "baz"
        cmd.ldapSearchBase = "foobar"
        cmd.ldapSearchFilter = "foobarbaz"
        cmd.ldapSearchSubtree = true
        assertTrue(cmd.validate())
    }

    void testVcsCommand() {
        mockForConstraintsTests(VcsCommand)
        // test vcs system
        // null
        VcsCommand cmd = new VcsCommand()
        cmd.vcs = null
        assertFalse(cmd.validate())
        assertEquals("nullable", cmd.errors["vcs"])
        // blank
        cmd = new VcsCommand()
        cmd.vcs = ""
        assertFalse(cmd.validate())
        assertEquals("blank", cmd.errors["vcs"])
        assertEquals("nullable", cmd.errors["exchangeDirectory"])
        assertEquals("nullable", cmd.errors["workingDirectory"])
        assertFalse(cmd.isGit())
        assertFalse(cmd.isSvn())
        assertEquals("", cmd.pluginName())
        // random string
        cmd = new VcsCommand()
        cmd.vcs = "test"
        assertFalse(cmd.validate())
        assertEquals("validator", cmd.errors["vcs"])
        assertEquals("nullable", cmd.errors["exchangeDirectory"])
        assertEquals("nullable", cmd.errors["workingDirectory"])
        assertFalse(cmd.isGit())
        assertFalse(cmd.isSvn())
        assertEquals("", cmd.pluginName())
        // with svn it should just work
        cmd = new VcsCommand()
        cmd.vcs = "svn"
        cmd.workingDirectory = ""
        cmd.exchangeDirectory = ""
        assertTrue(cmd.validate())
        assertFalse(cmd.isGit())
        assertTrue(cmd.isSvn())
        assertEquals("subversion", cmd.pluginName())
        // with git we need a working directory
        cmd = new VcsCommand()
        cmd.vcs = "git"
        cmd.workingDirectory = ""
        cmd.exchangeDirectory = ""
        assertFalse(cmd.validate())
        assertEquals("validator", cmd.errors["workingDirectory"])
        assertTrue(cmd.isGit())
        assertFalse(cmd.isSvn())
        assertEquals("git", cmd.pluginName())
        // when workingDirectory is not empty it needs to be a directory
        cmd = new VcsCommand()
        cmd.vcs = "svn"
        cmd.workingDirectory = "foo"
        cmd.exchangeDirectory = ""
        assertFalse(cmd.validate())
        assertEquals("validator", cmd.errors["workingDirectory"])
        assertTrue(cmd.isSvn())
        assertFalse(cmd.isGit())
        assertEquals("subversion", cmd.pluginName())
        // when exchangeDirectory is not empty it needs to be a directory
        cmd = new VcsCommand()
        cmd.vcs = "svn"
        cmd.workingDirectory = ""
        cmd.exchangeDirectory = "foo"
        assertFalse(cmd.validate())
        assertEquals("validator", cmd.errors["exchangeDirectory"])
        assertTrue(cmd.isSvn())
        assertFalse(cmd.isGit())
        assertEquals("subversion", cmd.pluginName())
        // exchange and working directory pointing to same directory should fail
        File directory = new File("target/testDir")
        directory.mkdirs()
        cmd = new VcsCommand()
        cmd.vcs = "svn"
        cmd.workingDirectory = "target/testDir"
        cmd.exchangeDirectory = "target/testDir"
        assertFalse(cmd.validate())
        assertEquals("validator", cmd.errors["workingDirectory"])
        assertEquals("validator", cmd.errors["exchangeDirectory"])
        assertTrue(cmd.isSvn())
        assertFalse(cmd.isGit())
        assertEquals("subversion", cmd.pluginName())
        // two different directories should work
        File directory2 = new File("target/testDir2")
        directory2.mkdirs()
        cmd = new VcsCommand()
        cmd.vcs = "svn"
        cmd.workingDirectory = "target/testDir"
        cmd.exchangeDirectory = "target/testDir2"
        assertTrue(cmd.validate())
        assertTrue(cmd.isSvn())
        assertFalse(cmd.isGit())
        assertEquals("subversion", cmd.pluginName())
        cmd = new VcsCommand()
        cmd.vcs = "git"
        cmd.workingDirectory = "target/testDir"
        cmd.exchangeDirectory = "target/testDir2"
        assertTrue(cmd.validate())
        assertFalse(cmd.isSvn())
        assertTrue(cmd.isGit())
        assertEquals("git", cmd.pluginName())

        // cleanup
        FileUtils.deleteDirectory(directory)
        FileUtils.deleteDirectory(directory2)
    }

    void testSvnCommand() {
        mockForConstraintsTests(SvnCommand)
        // blank
        SvnCommand cmd = new SvnCommand()
        cmd.localRepository = ""
        assertFalse(cmd.validate())
        assertEquals("blank", cmd.errors["localRepository"])
        cmd = new SvnCommand()
        // null
        cmd.localRepository = null
        assertFalse(cmd.validate())
        assertEquals("nullable", cmd.errors["localRepository"])
        // random text
        cmd = new SvnCommand()
        cmd.localRepository = "foo"
        assertFalse(cmd.validate())
        assertEquals("validator", cmd.errors["localRepository"])
        // correct test
        File directory = new File("target/testDir")
        directory.mkdirs()
        cmd = new SvnCommand()
        cmd.localRepository = "target/testDir"
        assertTrue(cmd.validate())
        FileUtils.deleteDirectory(directory)
    }

    void testFirstRunCommand() {
        mockForConstraintsTests(FirstRunCommand)
        // test for blank
        FirstRunCommand cmd = new FirstRunCommand()
        cmd.firstRun = ""
        assertFalse(cmd.validate())
        assertEquals("blank", cmd.errors["firstRun"])
        // test for null
        cmd = new FirstRunCommand()
        cmd.firstRun = null
        assertFalse(cmd.validate())
        assertEquals("nullable", cmd.errors["firstRun"])
        // test for false value
        cmd = new FirstRunCommand()
        cmd.firstRun = "test"
        assertFalse(cmd.validate())
        assertEquals("validator", cmd.errors["firstRun"])
        // test for correct values
        cmd = new FirstRunCommand()
        cmd.firstRun = "true"
        assertTrue(cmd.validate())
        cmd = new FirstRunCommand()
        cmd.firstRun = "false"
        assertTrue(cmd.validate())
    }

    void testServerCommand() {
        mockForConstraintsTests(ServerCommand)
        // test for null
        ServerCommand cmd = new ServerCommand()
        cmd.url = null
        assertFalse(cmd.validate())
        assertEquals("nullable", cmd.errors["url"])
        // test for blank
        cmd = new ServerCommand()
        cmd.url = ""
        assertFalse(cmd.validate())
        assertEquals("blank", cmd.errors["url"])
        // test for not a url
        cmd = new ServerCommand()
        cmd.url = "test"
        assertFalse(cmd.validate())
        assertEquals("url", cmd.errors["url"])
        // test with proper settings
        // url fails for localhost, that's why 127.0.0.1 is used
        cmd = new ServerCommand()
        cmd.url = "http://127.0.0.1:8080/jummp/"
        assertTrue(cmd.validate())

    }

    void testUserRegistrationCommand() {
        mockForConstraintsTests(UserRegistrationCommand)
        // test for null
        UserRegistrationCommand cmd = new UserRegistrationCommand()
        cmd.registration  = null
        cmd.sendEmail     = null
        cmd.sendToAdmin   = null
        cmd.senderAddress = null
        cmd.adminAddress  = null
        cmd.subject       = null
        cmd.body          = null
        cmd.url           = null
        cmd.activationSubject = null
        cmd.activationBody    = null
        cmd.activationUrl     = null
        assertFalse(cmd.validate())
        assertEquals("nullable", cmd.errors["senderAddress"])
        assertEquals("nullable", cmd.errors["adminAddress"])
        assertEquals("nullable", cmd.errors["subject"])
        assertEquals("nullable", cmd.errors["body"])
        assertEquals("nullable", cmd.errors["activationSubject"])
        assertEquals("nullable", cmd.errors["activationBody"])
        assertEquals("nullable", cmd.errors["activationUrl"])
        // now set a minimum for validation
        cmd.registration = true
        cmd.sendEmail = true
        cmd.sendToAdmin = true
        cmd.subject = ""
        cmd.body = ""
        cmd.url = "test"
        cmd.activationBody = ""
        cmd.activationSubject = ""
        cmd.activationUrl = "foo"
        cmd.senderAddress = "test"
        cmd.adminAddress = "test"
        // test email and url constraint
        assertFalse(cmd.validate())
        assertEquals("email", cmd.errors["senderAddress"])
        assertEquals("email", cmd.errors["adminAddress"])
        assertEquals("url", cmd.errors["url"])
        assertEquals("url", cmd.errors["activationUrl"])
        // subject and body should be invalid as they are empty
        assertEquals("validator", cmd.errors["subject"])
        assertEquals("validator", cmd.errors["body"])
        // test validator for email and url
        cmd.senderAddress = ""
        cmd.adminAddress = ""
        cmd.url = ""
        cmd.activationUrl = ""
        assertFalse(cmd.validate())
        assertEquals("validator", cmd.errors["senderAddress"])
        assertEquals("validator", cmd.errors["adminAddress"])
        assertEquals("validator", cmd.errors["url"])
        assertEquals("validator", cmd.errors["activationUrl"])
        // disabling sendEmail should pass validation
        cmd.sendEmail = false
        assertTrue(cmd.validate())
        // enabling sendEmail but disabling sendToAdmin should ignore adminAddress
        cmd.sendEmail = true
        cmd.sendToAdmin = false
        cmd.senderAddress = "test@example.com"
        cmd.subject = "Test"
        cmd.url = "http://www.example.com"
        cmd.body = "Body Test"
        cmd.activationBody = "Activation Body"
        cmd.activationSubject = "Activation"
        cmd.activationUrl = "http://www.example.org"
        assertTrue(cmd.validate())
        // just to be sure
        cmd.sendToAdmin = true
        assertFalse(cmd.validate())
        // set the admin address
        cmd.adminAddress = "admin@example.com"
        assertTrue(cmd.validate())
    }

    void testChangePasswordCommand() {
        mockForConstraintsTests(ChangePasswordCommand)
        // test for null
        ChangePasswordCommand cmd = new ChangePasswordCommand()
        cmd.changePassword = null
        cmd.resetPassword  = null
        cmd.senderAddress  = null
        cmd.subject        = null
        cmd.body           = null
        cmd.url            = null
        assertFalse(cmd.validate())
        assertEquals("nullable", cmd.errors["senderAddress"])
        assertEquals("nullable", cmd.errors["subject"])
        assertEquals("nullable", cmd.errors["body"])
        assertEquals("nullable", cmd.errors["url"])
        // now set a minimum for validation
        cmd.changePassword = true
        cmd.resetPassword = true
        cmd.subject = ""
        cmd.body = ""
        cmd.url = "test"
        cmd.senderAddress = "test"
        // test email and url constraint
        assertFalse(cmd.validate())
        assertEquals("email", cmd.errors["senderAddress"])
        assertEquals("url", cmd.errors["url"])
        // subject and body should be invalid as they are empty
        assertEquals("validator", cmd.errors["subject"])
        assertEquals("validator", cmd.errors["body"])
        // test validator for email and url
        cmd.senderAddress = ""
        cmd.url = ""
        assertFalse(cmd.validate())
        assertEquals("validator", cmd.errors["senderAddress"])
        assertEquals("validator", cmd.errors["url"])
        // disabling sendEmail should pass validation
        cmd.resetPassword = false
        assertTrue(cmd.validate())
        // enabling so that it validates
        cmd.changePassword = true
        cmd.senderAddress = "test@example.com"
        cmd.subject = "Test"
        cmd.url = "http://www.example.com"
        cmd.body = "Body Test"
        assertTrue(cmd.validate())
    }

    void testTriggerCommand() {
        mockForConstraintsTests(TriggerCommand)
        // test for null
        TriggerCommand cmd = new TriggerCommand()
        cmd.maxInactiveTime = null
        cmd.removeInterval = null
        cmd.startRemoveOffset = null
        assertFalse(cmd.validate())
        assertEquals("nullable", cmd.errors["maxInactiveTime"])
        assertEquals("nullable", cmd.errors["removeInterval"])
        assertEquals("nullable", cmd.errors["startRemoveOffset"])
        // too short values should not validate
        cmd.maxInactiveTime = 100
        cmd.removeInterval = 100
        cmd.startRemoveOffset = 100
        assertFalse(cmd.validate())
        // correct values should pass
        cmd.maxInactiveTime = 10000
        cmd.removeInterval = 10000
        cmd.startRemoveOffset = 10000
        assertTrue(cmd.validate())
    }

    void testSBMLCommand() {
        mockForConstraintsTests(SBMLCommand)
        // test for null
        SBMLCommand cmd = new SBMLCommand()
        cmd.validation = null
        assertFalse(cmd.validate())
        assertEquals("nullable", cmd.errors["validation"])
        // blank should not validate
        cmd.validation
        assertEquals("nullable", cmd.errors["validation"])
        // correct value should pass
        cmd.validation = false
        assertTrue(cmd.validate())
    }
}
