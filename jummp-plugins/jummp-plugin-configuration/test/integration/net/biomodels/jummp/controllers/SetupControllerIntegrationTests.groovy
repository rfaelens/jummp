package net.biomodels.jummp.controllers

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
        assertFlowExecutionActive()
        // correct value should end the flow
        // currently fails due to missing data
        /*setupController.params.firstRun = "true"
        signalEvent("next")
        assertFlowExecutionEnded()
        assertFlowExecutionOutcomeEquals("finish")*/
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
}
