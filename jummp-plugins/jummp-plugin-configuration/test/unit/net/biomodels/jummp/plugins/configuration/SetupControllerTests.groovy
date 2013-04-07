package net.biomodels.jummp.plugins.configuration

import grails.test.*
import org.apache.commons.io.FileUtils
import grails.test.mixin.TestFor

@TestFor(SetupController)
class SetupControllerTests  {

    void testDatabaseCommand() {
        // null should fail
        DatabaseCommand cmd = mockCommandObject(DatabaseCommand)
        cmd.server = null
        assertFalse(cmd.validate())
        assertEquals("nullable", cmd.errors["type"].code)
        assertEquals("nullable", cmd.errors["server"].code)
        assertEquals("nullable", cmd.errors["port"].code)
        assertEquals("nullable", cmd.errors["database"].code)
        assertEquals("nullable", cmd.errors["username"].code)
        assertEquals("nullable", cmd.errors["password"].code)
        // test for blanks
        cmd = mockCommandObject(DatabaseCommand)
        cmd.type = null
        cmd.server = ''
        cmd.database = ''
        cmd.username = ''
        cmd.password = ''
        assertFalse(cmd.validate())
        assertEquals("nullable", cmd.errors["type"].code)
        assertEquals("nullable", cmd.errors["port"].code)
        assertEquals("blank", cmd.errors["server"].code)
        assertEquals("blank", cmd.errors["database"].code)
        assertEquals("blank", cmd.errors["username"].code)
        assertNull(cmd.errors["password"])
        // port in range 0 to 65535
        cmd = mockCommandObject(DatabaseCommand)
        cmd.port = -1
        assertFalse(cmd.validate())
        assertEquals("range.toosmall", cmd.errors["port"].code)
        cmd = mockCommandObject(DatabaseCommand)
        cmd.port = 65536
        assertFalse(cmd.validate())
        assertEquals("range.toobig", cmd.errors["port"].code)
        cmd = mockCommandObject(DatabaseCommand)
        cmd.port = 0
        assertFalse(cmd.validate())
        assertNull(cmd.errors["port"])
        cmd = mockCommandObject(DatabaseCommand)
        cmd.port = 65535
        assertFalse(cmd.validate())
        assertNull(cmd.errors["port"])
        // and one test that should work
        cmd = mockCommandObject(DatabaseCommand)
        cmd.type = 'MYSQL'
        cmd.server = 'localhost'
        cmd.database = 'jummp'
        cmd.username = 'jummp'
        cmd.password = 'jummp'
        cmd.port = 3306
        assertTrue(cmd.validate())
    }

    void testRemoteCommand() {
        // null should fail
        RemoteCommand cmd = mockCommandObject(RemoteCommand)
        assertFalse(cmd.validate())
        assertEquals("nullable", cmd.errors["jummpExportDbus"].code)
        assertEquals("nullable", cmd.errors["jummpExportJms"].code)
        // test for blanks
        cmd = mockCommandObject(RemoteCommand)
        cmd.jummpRemote=""
        assertFalse(cmd.validate())
        assertEquals("blank", cmd.errors["jummpRemote"].code)
        // jummpRemote has value in list
        cmd = mockCommandObject(RemoteCommand)
        cmd.jummpRemote="smj"
        assertFalse(cmd.validate())
        assertEquals("not.inList", cmd.errors["jummpRemote"].code)
        cmd = mockCommandObject(RemoteCommand)
        cmd.jummpRemote="subD"
        assertFalse(cmd.validate())
        assertEquals("not.inList", cmd.errors["jummpRemote"].code)
        cmd = mockCommandObject(RemoteCommand)
        cmd.jummpRemote="jms"
        assertFalse(cmd.validate())
        assertNull(cmd.errors["jummpRemote"])
        cmd = mockCommandObject(RemoteCommand)
        cmd.jummpRemote="Dbus"
        assertFalse(cmd.validate())
        // and one test that should work
        cmd = mockCommandObject(RemoteCommand)
        cmd.jummpRemote="jms"
        cmd.jummpExportDbus=false
        cmd.jummpExportJms=true
        assertTrue(cmd.validate())
    }

    void testDBusCommand() {
        // null should fail
        DBusCommand cmd = mockCommandObject(DBusCommand)
        cmd.systemBus = null
        assertFalse(cmd.validate())
        assertEquals("nullable", cmd.errors["systemBus"].code)
        // test for blanks
        cmd = mockCommandObject(DBusCommand)
        cmd.systemBus
        assertFalse(cmd.validate())
        assertEquals("nullable", cmd.errors["systemBus"].code)
        // test for null
        cmd = mockCommandObject(DBusCommand)
        cmd.systemBus = null
        assertFalse(cmd.validate())
        assertEquals("nullable", cmd.errors["systemBus"].code)
        // this test should work
        cmd = mockCommandObject(DBusCommand)
        cmd.systemBus = false
        assertTrue(cmd.validate())
    }

    void testLdapCommand() {
        // null should fail
        LdapCommand cmd = mockCommandObject(LdapCommand)
        assertFalse(cmd.validate())
        assertEquals("nullable", cmd.errors["ldapServer"].code)
        assertEquals("nullable", cmd.errors["ldapManagerDn"].code)
        assertEquals("nullable", cmd.errors["ldapManagerPassword"].code)
        assertEquals("nullable", cmd.errors["ldapSearchBase"].code)
        assertEquals("nullable", cmd.errors["ldapSearchFilter"].code)
        assertEquals("nullable", cmd.errors["ldapSearchSubtree"].code)
        // blank should fail some
        cmd = mockCommandObject(LdapCommand)
        cmd.ldapServer = ""
        cmd.ldapManagerDn = ""
        cmd.ldapManagerPassword = ""
        // allowed
        cmd.ldapSearchBase = ""
        cmd.ldapSearchFilter = ""
        assertFalse(cmd.validate())
        assertEquals("blank", cmd.errors["ldapServer"].code)
        assertEquals("blank", cmd.errors["ldapManagerDn"].code)
        assertEquals("blank", cmd.errors["ldapManagerPassword"].code)
        assertNull(cmd.errors["ldapSearchBase"])
        assertNull(cmd.errors["ldapSearchFilter"])
        assertEquals("nullable", cmd.errors["ldapSearchSubtree"].code)
        // TODO: test for a LDAP URL
        // setting all fields except the boolean should only fail the boolean
        cmd = mockCommandObject(LdapCommand)
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
        assertEquals("nullable", cmd.errors["ldapSearchSubtree"].code)
        // last but not least - a successful test
        cmd = mockCommandObject(LdapCommand)
        cmd.ldapServer = "foo"
        cmd.ldapManagerDn = "bar"
        cmd.ldapManagerPassword = "baz"
        cmd.ldapSearchBase = "foobar"
        cmd.ldapSearchFilter = "foobarbaz"
        cmd.ldapSearchSubtree = true
        assertTrue(cmd.validate())
    }

    void testVcsCommand() {
        // test vcs system
        // null
        VcsCommand cmd = mockCommandObject(VcsCommand)
        cmd.vcs = null
        assertFalse(cmd.validate())
        assertEquals("nullable", cmd.errors["vcs"].code)
        // blank
        cmd = mockCommandObject(VcsCommand)
        cmd.vcs = ""
        assertFalse(cmd.validate())
        assertEquals("blank", cmd.errors["vcs"].code)
        assertEquals("nullable", cmd.errors["exchangeDirectory"].code)
        assertEquals("nullable", cmd.errors["workingDirectory"].code)
        assertFalse(cmd.isGit())
        assertFalse(cmd.isSvn())
        assertEquals("", cmd.pluginName())
        // random string
        cmd = mockCommandObject(VcsCommand)
        cmd.vcs = "test"
        assertFalse(cmd.validate())
        assertEquals("validator.invalid", cmd.errors["vcs"].code)
        assertEquals("nullable", cmd.errors["exchangeDirectory"].code)
        assertEquals("nullable", cmd.errors["workingDirectory"].code)
        assertFalse(cmd.isGit())
        assertFalse(cmd.isSvn())
        assertEquals("", cmd.pluginName())
        // with svn it should just work
        cmd = mockCommandObject(VcsCommand)
        cmd.vcs = "svn"
        cmd.workingDirectory = ""
        cmd.exchangeDirectory = ""
        assertTrue(cmd.validate())
        assertFalse(cmd.isGit())
        assertTrue(cmd.isSvn())
        assertEquals("subversion", cmd.pluginName())
        // with git we need a working directory
        cmd = mockCommandObject(VcsCommand)
        cmd.vcs = "git"
        cmd.workingDirectory = ""
        cmd.exchangeDirectory = ""
        assertFalse(cmd.validate())
        assertEquals("validator.invalid", cmd.errors["workingDirectory"].code)
        assertTrue(cmd.isGit())
        assertFalse(cmd.isSvn())
        assertEquals("git", cmd.pluginName())
        // when workingDirectory is not empty it needs to be a directory
        cmd = mockCommandObject(VcsCommand)
        cmd.vcs = "svn"
        cmd.workingDirectory = "foo"
        cmd.exchangeDirectory = ""
        assertFalse(cmd.validate())
        assertEquals("validator.invalid", cmd.errors["workingDirectory"].code)
        assertTrue(cmd.isSvn())
        assertFalse(cmd.isGit())
        assertEquals("subversion", cmd.pluginName())
        // when exchangeDirectory is not empty it needs to be a directory
        cmd =mockCommandObject(VcsCommand)
        cmd.vcs = "svn"
        cmd.workingDirectory = ""
        cmd.exchangeDirectory = "foo"
        assertFalse(cmd.validate())
        assertEquals("validator.invalid", cmd.errors["exchangeDirectory"].code)
        assertTrue(cmd.isSvn())
        assertFalse(cmd.isGit())
        assertEquals("subversion", cmd.pluginName())
        // exchange and working directory pointing to same directory should fail
        File directory = new File("target/testDir")
        directory.mkdirs()
        cmd = mockCommandObject(VcsCommand)
        cmd.vcs = "svn"
        cmd.workingDirectory = "target/testDir"
        cmd.exchangeDirectory = "target/testDir"
        assertFalse(cmd.validate())
        assertEquals("validator.invalid", cmd.errors["workingDirectory"].code)
        assertEquals("validator.invalid", cmd.errors["exchangeDirectory"].code)
        assertTrue(cmd.isSvn())
        assertFalse(cmd.isGit())
        assertEquals("subversion", cmd.pluginName())
        // two different directories should work
        File directory2 = new File("target/testDir2")
        directory2.mkdirs()
        cmd = mockCommandObject(VcsCommand)
        cmd.vcs = "svn"
        cmd.workingDirectory = "target/testDir"
        cmd.exchangeDirectory = "target/testDir2"
        assertTrue(cmd.validate())
        assertTrue(cmd.isSvn())
        assertFalse(cmd.isGit())
        assertEquals("subversion", cmd.pluginName())
        cmd = mockCommandObject(VcsCommand)
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
        // blank
        SvnCommand cmd = mockCommandObject(SvnCommand)
        cmd.localRepository = ""
        assertFalse(cmd.validate())
        assertEquals("blank", cmd.errors["localRepository"].code)
        cmd = mockCommandObject(SvnCommand)
        // null
        cmd.localRepository = null
        assertFalse(cmd.validate())
        assertEquals("nullable", cmd.errors["localRepository"].code)
        // random text
        cmd = mockCommandObject(SvnCommand)
        cmd.localRepository = "foo"
        assertFalse(cmd.validate())
        assertEquals("validator.invalid", cmd.errors["localRepository"].code)
        // correct test
        File directory = new File("target/testDir")
        directory.mkdirs()
        cmd = mockCommandObject(SvnCommand)
        cmd.localRepository = "target/testDir"
        assertTrue(cmd.validate())
        FileUtils.deleteDirectory(directory)
    }

    void testFirstRunCommand() {
        // test for blank
        FirstRunCommand cmd = mockCommandObject(FirstRunCommand)
        cmd.firstRun = ""
        assertFalse(cmd.validate())
        assertEquals("blank", cmd.errors["firstRun"].code)
        // test for null
        cmd = mockCommandObject(FirstRunCommand)
        cmd.firstRun = null
        assertFalse(cmd.validate())
        assertEquals("nullable", cmd.errors["firstRun"].code)
        // test for false value
        cmd = mockCommandObject(FirstRunCommand)
        cmd.firstRun = "test"
        assertFalse(cmd.validate())
        assertEquals("validator.invalid", cmd.errors["firstRun"].code)
        // test for correct values
        cmd = mockCommandObject(FirstRunCommand)
        cmd.firstRun = "true"
        assertTrue(cmd.validate())
        cmd = mockCommandObject(FirstRunCommand)
        cmd.firstRun = "false"
        assertTrue(cmd.validate())
    }

    void testServerCommand() {
        // test for null
        ServerCommand cmd = mockCommandObject(ServerCommand)
        cmd.url = null
        cmd.protectEverything = null
        assertFalse(cmd.validate())
        assertEquals("nullable", cmd.errors["url"].code)
        assertEquals("nullable", cmd.errors["protectEverything"].code)
        // test for blank
        cmd = mockCommandObject(ServerCommand)
        cmd.url = ""
        assertFalse(cmd.validate())
        assertEquals("blank", cmd.errors["url"].code)
        assertEquals("nullable", cmd.errors["protectEverything"].code)
        // test for not a url
        cmd = mockCommandObject(ServerCommand)
        cmd.url = "test"
        assertFalse(cmd.validate())
        assertEquals("url.invalid", cmd.errors["url"].code)
        assertEquals("nullable", cmd.errors["protectEverything"].code)

        // test with proper settings
        // url fails for localhost, that's why 127.0.0.1 is used
        cmd = mockCommandObject(ServerCommand)
        cmd.url = "http://127.0.0.1:8080/jummp/"
        cmd.protectEverything = true
        assertTrue(cmd.validate())
        // and the same with protectEverything as false
        cmd = mockCommandObject(ServerCommand)
        cmd.url = "http://127.0.0.1:8080/jummp/"
        cmd.protectEverything = false
        assertTrue(cmd.validate())
    }

    void testUserRegistrationCommand() {
        // test for null
        UserRegistrationCommand cmd = mockCommandObject(UserRegistrationCommand)
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
        assertEquals("nullable", cmd.errors["senderAddress"].code)
        assertEquals("nullable", cmd.errors["adminAddress"].code)
        assertEquals("nullable", cmd.errors["subject"].code)
        assertEquals("nullable", cmd.errors["body"].code)
        assertEquals("nullable", cmd.errors["activationSubject"].code)
        assertEquals("nullable", cmd.errors["activationBody"].code)
        assertEquals("nullable", cmd.errors["activationUrl"].code)
        // now set a minimum for validation
        cmd = mockCommandObject(UserRegistrationCommand)
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
        assertEquals("email.invalid", cmd.errors["senderAddress"].code)
        assertEquals("email.invalid", cmd.errors["adminAddress"].code)
        assertEquals("url.invalid", cmd.errors["url"].code)
        assertEquals("url.invalid", cmd.errors["activationUrl"].code)
        // subject and body should be invalid as they are empty
        assertEquals("validator.invalid", cmd.errors["subject"].code)
        assertEquals("validator.invalid", cmd.errors["body"].code)
        // test validator for email and url
        cmd.clearErrors()
        cmd.senderAddress = ""
        cmd.adminAddress = ""
        cmd.url = ""
        cmd.activationUrl = ""
        assertFalse(cmd.validate())
        assertEquals("validator.invalid", cmd.errors["senderAddress"].code)
        assertEquals("validator.invalid", cmd.errors["adminAddress"].code)
        assertEquals("validator.invalid", cmd.errors["url"].code)
        assertEquals("validator.invalid", cmd.errors["activationUrl"].code)
        // disabling sendEmail should pass validation
        cmd.sendEmail = false
        cmd.clearErrors()
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
        cmd.clearErrors()
        assertTrue(cmd.validate())
        // just to be sure
        cmd.sendToAdmin = true
        cmd.clearErrors()
        assertFalse(cmd.validate())
        // set the admin address
        cmd.adminAddress = "admin@example.com"
        cmd.clearErrors()
        assertTrue(cmd.validate())
    }

    void testChangePasswordCommand() {
        // test for null
        ChangePasswordCommand cmd = mockCommandObject(ChangePasswordCommand)
        cmd.changePassword = null
        cmd.resetPassword  = null
        cmd.senderAddress  = null
        cmd.subject        = null
        cmd.body           = null
        cmd.url            = null
        assertFalse(cmd.validate())
        assertEquals("nullable", cmd.errors["senderAddress"].code)
        assertEquals("nullable", cmd.errors["subject"].code)
        assertEquals("nullable", cmd.errors["body"].code)
        assertEquals("nullable", cmd.errors["url"].code)
        // now set a minimum for validation
        cmd.changePassword = true
        cmd.resetPassword = true
        cmd.subject = ""
        cmd.body = ""
        cmd.url = "test"
        cmd.senderAddress = "test"
        // test email and url constraint
        cmd.clearErrors()
        assertFalse(cmd.validate())
        assertEquals("email.invalid", cmd.errors["senderAddress"].code)
        assertEquals("url.invalid", cmd.errors["url"].code)
        // subject and body should be invalid as they are empty
        assertEquals("validator.invalid", cmd.errors["subject"].code)
        assertEquals("validator.invalid", cmd.errors["body"].code)
        // test validator for email and url
        cmd.senderAddress = ""
        cmd.url = ""
        cmd.clearErrors()
        assertFalse(cmd.validate())
        assertEquals("validator.invalid", cmd.errors["senderAddress"].code)
        assertEquals("validator.invalid", cmd.errors["url"].code)
        // disabling sendEmail should pass validation
        cmd.resetPassword = false
        cmd.clearErrors()
        assertTrue(cmd.validate())
        // enabling so that it validates
        cmd.changePassword = true
        cmd.senderAddress = "test@example.com"
        cmd.subject = "Test"
        cmd.url = "http://www.example.com"
        cmd.body = "Body Test"
        cmd.clearErrors()
        assertTrue(cmd.validate())
    }

    void testTriggerCommand() {
        // test for null
        TriggerCommand cmd = mockCommandObject(TriggerCommand)
        cmd.maxInactiveTime = null
        cmd.removeInterval = null
        cmd.startRemoveOffset = null
        assertFalse(cmd.validate())
        assertEquals("nullable", cmd.errors["maxInactiveTime"].code)
        assertEquals("nullable", cmd.errors["removeInterval"].code)
        assertEquals("nullable", cmd.errors["startRemoveOffset"].code)
        // too short values should not validate
        cmd.maxInactiveTime = 100
        cmd.removeInterval = 100
        cmd.startRemoveOffset = 100
        assertFalse(cmd.validate())
        // correct values should pass
        cmd.maxInactiveTime = 10000
        cmd.removeInterval = 10000
        cmd.startRemoveOffset = 10000
        cmd.clearErrors()
        assertTrue(cmd.validate())
    }

    void testSBMLCommand() {
        // test for null
        SBMLCommand cmd = mockCommandObject(SBMLCommand)
        cmd.validation = null
        assertFalse(cmd.validate())
        assertEquals("nullable", cmd.errors["validation"].code)
        // blank should not validate
        cmd.validation
        assertEquals("nullable", cmd.errors["validation"].code)
        // correct value should pass
        cmd.validation = false
        cmd.clearErrors()
        assertTrue(cmd.validate())
    }

    void testBrandingCommand() {
        // test for null
        BrandingCommand cmd = mockCommandObject(BrandingCommand)
        cmd.internalColor = null
        assertFalse(cmd.validate())
        assertEquals("nullable", cmd.errors["internalColor"].code)
        // correct value should pass
        cmd.internalColor = "#FFFFFF"
        cmd.clearErrors()
        assertTrue(cmd.validate())
    }
}
