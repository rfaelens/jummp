package net.biomodels.jummp.controllers

import grails.test.*
import org.apache.commons.io.FileUtils


class SetupControllerTests extends ControllerUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
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
}
