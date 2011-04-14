package net.biomodels.jummp.plugins.configuration

import grails.test.*
import org.apache.commons.io.FileUtils

class SetupControllerIntegrationTests extends WebFlowTestCase {
    SetupController setupController = new SetupController()

    def getFlow() {
        return setupController.setupFlow
    }
    
    protected void setUp() {
        super.setUp()
        File configFile = new File(System.getProperty("user.home") + System.getProperty("file.separator") + ".jummp.properties")
        File backupFile = new File(System.getProperty("user.home") + System.getProperty("file.separator") + ".jummp.properties.orig")
        if (configFile.exists()) {
            FileUtils.copyFile(configFile, backupFile)
        } else if (backupFile.exists()) {
            // we already have a backup file - delete it
            FileUtils.deleteQuietly(backupFile)
        }
    }

    protected void tearDown() {
        super.tearDown()
        FileUtils.deleteDirectory(new File("target/vcs/workingDirectory"))
        FileUtils.deleteDirectory(new File("target/vcs/repository"))
        File backupFile = new File(System.getProperty("user.home") + System.getProperty("file.separator") + ".jummp.properties.orig")
        if (backupFile.exists()) {
            FileUtils.copyFile(backupFile, new File(System.getProperty("user.home") + System.getProperty("file.separator") + ".jummp.properties"))
        }
    }

    void testBasicOrdering() {
        startFlow()
        assertCurrentStateEquals("start")
        setupController.params.server = 'localhost'
        setupController.params.port = '3306'
        setupController.params.username = 'jummp'
        setupController.params.database = 'jummp'
        setupController.params.password = ''
        signalEvent("next")
        assertCurrentStateEquals("authenticationBackend")
        // incorrect authentication backend
        setupController.params.authenticationBackend = "foo"
        signalEvent("next")
        assertCurrentStateEquals("authenticationBackend")
        // use database
        setupController.params.authenticationBackend = "database"
        signalEvent("next")
        assertCurrentStateEquals("vcs")
        // incorrect one should fails
        setupController.params.vcs = "foo"
        signalEvent("next")
        assertCurrentStateEquals("vcs")
        assertTrue(getFlowScope().vcs.hasErrors())
        // use svn
        setupController.params.vcs = "svn"
        setupController.params.workingDirectory = ""
        setupController.params.exchangeDirectory = ""
        signalEvent("next")
        assertCurrentStateEquals("svn")
        // incorrect value should fail
        setupController.params.localRepository = ""
        signalEvent("next")
        assertCurrentStateEquals("svn")
        assertTrue(getFlowScope().svn.hasErrors())
        // need a directory for next step
        File directory = new File("target/vcs/repository")
        directory.mkdirs()
        setupController.params.localRepository = "target/vcs/repository"
        signalEvent("next")
        assertCurrentStateEquals("firstRun")
        // incorrect value should fail
        setupController.params.firstRun = "test"
        signalEvent("next")
        assertCurrentStateEquals("firstRun")
        assertTrue(getFlowScope().firstRun.hasErrors())
        // correct value should transit to userRegistration
        setupController.params.firstRun = "true"
        signalEvent("next")
        assertCurrentStateEquals("userRegistration")
        // incorrect value should fail
        setupController.params.sendEmail = "true"
        setupController.params.senderAddress = "test"
        signalEvent("next")
        assertCurrentStateEquals("userRegistration")
        assertTrue(getFlowScope().userRegistration.hasErrors())
        // correct values should transit to changePassword state
        setupController.params.registration = "false"
        setupController.params.sendEmail = "false"
        setupController.params.subject = ""
        setupController.params.url = ""
        setupController.params.body = ""
        setupController.params.senderAddress = ""
        setupController.params.adminAddress = ""
        setupController.params.activationBody = ""
        setupController.params.activationSubject = ""
        setupController.params.activationUrl = ""
        signalEvent("next")
        assertFalse(getFlowScope().userRegistration.hasErrors())
        assertCurrentStateEquals("changePassword")
        // incorrect value should fail
        setupController.params.changePassword = "true"
        setupController.params.resetPassword = "true"
        signalEvent("next")
        assertCurrentStateEquals("changePassword")
        assertTrue(getFlowScope().changePassword.hasErrors())
        // correct values should transit to server state
        setupController.params.changePassword = "false"
        setupController.params.resetPassword = "false"
        setupController.params.subject = ""
        setupController.params.url = ""
        setupController.params.body = ""
        setupController.params.senderAddress = ""
        signalEvent("next")
        assertCurrentStateEquals("remoteExport")
         // incorrect values should fail
        setupController.params.jummpExportDbus = false
        setupController.params.jummpExportJms = false
        signalEvent("next")
        assertCurrentStateEquals("remoteExport")
        assertTrue(getFlowScope().remote.hasErrors())
        // correct values should transit to remote Remote state
        setupController.params.jummpExportDbus = true
        setupController.params.jummpExportJms = true
        signalEvent("next")
        assertCurrentStateEquals("remoteRemote")
        // incorrect value should fail
        setupController.params.jummpRemote = "smj"
        signalEvent("next")
        assertCurrentStateEquals("remoteRemote")
        assertTrue(getFlowScope().remote.hasErrors())
        // correct values should transit to end state
        setupController.params.jummpRemote = "jms"
        signalEvent("next")
        assertCurrentStateEquals("server")
        // incorrect value should fail
        setupController.params.url = "test"
        signalEvent("next")
        assertCurrentStateEquals("server")
        assertTrue(getFlowScope().server.hasErrors())
        assertFlowExecutionActive()
        // correct value should transit to remoteExport state
        setupController.params.url = "http://127.0.0.1:8080/jummp/"
        signalEvent("next")
        assertFlowExecutionEnded()
        assertFlowExecutionOutcomeEquals("finish")
    }

    void testAuthenticationBackendTransition() {
        setCurrentState("authenticationBackend")
        assertCurrentStateEquals("authenticationBackend")
        // without data set we stay in state
        signalEvent("next")
        assertCurrentStateEquals("authenticationBackend")
        // with database we go to vcs
        setupController.params.authenticationBackend = "database"
        signalEvent("next")
        assertCurrentStateEquals("vcs")
        // with ldap we end in ldap
        setCurrentState("authenticationBackend")
        setupController.params.authenticationBackend = "ldap"
        signalEvent("next")
        assertCurrentStateEquals("ldap")
    }

    void testVcsTransitions() {
        setCurrentState("vcs")
        assertCurrentStateEquals("vcs")
        // no vcs info should stay in current state
        signalEvent("next")
        assertCurrentStateEquals("vcs")
        setupController.params.vcs = "foo"
        signalEvent("next")
        assertCurrentStateEquals("vcs")
        setupController.params.vcs = "git"
        signalEvent("next")
        assertCurrentStateEquals("vcs")
        // svn should transit to next state
        setupController.params.vcs = "svn"
        setupController.params.workingDirectory = ""
        setupController.params.exchangeDirectory = ""
        signalEvent("next")
        assertCurrentStateEquals("svn")
        // go back to vcs state
        setCurrentState("vcs")
        assertCurrentStateEquals("vcs")
        // git alone should not transit
        setupController.params.vcs = "git"
        signalEvent("next")
        assertCurrentStateEquals("vcs")
        // with a working directory it should transit
        File directory = new File("target/vcs/workingDirectory")
        directory.mkdirs()
        setupController.params.workingDirectory = "target/vcs/workingDirectory"
        signalEvent("next")
        assertCurrentStateEquals("git")
    }

    void testSimpleBackTransitions() {
        // tests that all non-branching states work correctly
        setCurrentState("server")
        assertCurrentStateEquals("server")
        // go back to remoteExport
        signalEvent("back")
        assertCurrentStateEquals("remoteExport")
        // go back to changePassword
        signalEvent("back")
        assertCurrentStateEquals("changePassword")
        // go back to userRegistration
        signalEvent("back")
        assertCurrentStateEquals("userRegistration")
        // go back to firstRun
        signalEvent("back")
        assertCurrentStateEquals("firstRun")
        // first Run has a branching - jump to state git
        setCurrentState("git")
        assertCurrentStateEquals("git")
        // go back to vcs
        signalEvent("back")
        assertCurrentStateEquals("vcs")
        // go to svn to test back
        setCurrentState("svn")
        assertCurrentStateEquals("svn")
        // go back to vcs
        signalEvent("back")
        assertCurrentStateEquals("vcs")
        // vcs is after a branching - jump to state ldap
        setCurrentState("ldap")
        assertCurrentStateEquals("ldap")
        // go back to authenticationBackend
        signalEvent("back")
        assertCurrentStateEquals("authenticationBackend")
        // go back to start
        signalEvent("back")
        assertCurrentStateEquals("start")
    }

    void testVcsGoBack() {
        // tests that we end up in the correct state when going back from vcs
        // first navigate to the vcs
        setCurrentState("authenticationBackend")
        assertCurrentStateEquals("authenticationBackend")
        setupController.params.authenticationBackend = "database"
        signalEvent("next")
        assertCurrentStateEquals("vcs")
        // going back should be in authenticationBackend
        signalEvent("back")
        assertCurrentStateEquals("authenticationBackend")
        // try going over ldap
        setupController.params.authenticationBackend = "ldap"
        signalEvent("next")
        assertCurrentStateEquals("ldap")
        setupController.params.ldapServer = "server"
        setupController.params.ldapManagerDn = "manager"
        setupController.params.ldapManagerPassword = "password"
        setupController.params.ldapSearchBase = "search"
        setupController.params.ldapSearchFilter = "filter"
        setupController.params.ldapSearchSubtree = "true"
        signalEvent("next")
        assertCurrentStateEquals("vcs")
        // going back should end in ldap
        signalEvent("back")
        assertCurrentStateEquals("ldap")
        // let's go back to authentication backend and use database
        signalEvent("back")
        assertCurrentStateEquals("authenticationBackend")
        setupController.params.authenticationBackend = "database"
        signalEvent("next")
        assertCurrentStateEquals("vcs")
        // now going back should end again in authenticationBackend
        signalEvent("back")
        assertCurrentStateEquals("authenticationBackend")
    }

    void testFirstRunGoBack() {
        // tests that we end up in the correct state when going back from first Run
        // first navigate to first Run
        setCurrentState("vcs")
        assertCurrentStateEquals("vcs")
        // go to svn
        setupController.params.vcs = "svn"
        setupController.params.exchangeDirectory = ""
        setupController.params.workingDirectory = ""
        signalEvent("next")
        assertCurrentStateEquals("svn")
        // go to first run
        setupController.params.localRepository = "target"
        signalEvent("next")
        assertCurrentStateEquals("firstRun")
        // going back should end in svn
        signalEvent("back")
        assertCurrentStateEquals("svn")
        // going back and choose git
        signalEvent("back")
        assertCurrentStateEquals("vcs")
        setupController.params.vcs = "git"
        setupController.params.workingDirectory = "target"
        signalEvent("next")
        assertCurrentStateEquals("git")
        signalEvent("next")
        assertCurrentStateEquals("firstRun")
        // going back should end in git
        signalEvent("back")
        assertCurrentStateEquals("git")
        // going back again and test going through svn
        signalEvent("back")
        assertCurrentStateEquals("vcs")
        setupController.params.vcs = "svn"
        signalEvent("next")
        assertCurrentStateEquals("svn")
        // go to first run
        setupController.params.localRepository = "target"
        signalEvent("next")
        assertCurrentStateEquals("firstRun")
        // and one last to ensure we are in svn
        signalEvent("back")
        assertCurrentStateEquals("svn")
    }
}
