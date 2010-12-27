package net.biomodels.jummp.core.vcs

import grails.test.*
import org.apache.commons.io.FileUtils
import org.tmatesoft.svn.core.SVNException
import org.tmatesoft.svn.core.SVNLogEntry
import org.tmatesoft.svn.core.SVNURL
import org.tmatesoft.svn.core.io.SVNRepository
import org.tmatesoft.svn.core.io.SVNRepositoryFactory

// TODO: the test only checks local repositories. A test for remote repositories is also required, though more difficult to setup. SVNKit does not provide an API to create remote repositories

class SvnManagerTests extends GrailsUnitTestCase {
    File workingCopy
    File exchangeDirectory
    SvnManager svn
    SVNURL url

    protected void setUp() {
        super.setUp()
        workingCopy = new File("target/vcs/workingCopy")
        workingCopy.mkdirs()
        exchangeDirectory = new File("target/vcs/exchange")
        exchangeDirectory.mkdirs()
        File repositoryFile = new File("target/vcs/svn")
        url = SVNRepositoryFactory.createLocalRepository(repositoryFile, true, false)
        svn = new SvnManager(repositoryFile)
    }

    protected void tearDown() {
        super.tearDown()
        FileUtils.deleteDirectory(new File("target/vcs/"))
    }

    void testCheckout() {
        shouldFail(VcsException) {
            // test that we cannot init into a file
            File noDirectory = new File("target/vcs/tmp")
            FileUtils.touch(noDirectory)
            svn.init(noDirectory, exchangeDirectory)
        }
        shouldFail(VcsException) {
            // test that we cannot init into a non-existing directory
            svn.init(new File("target/vcs/tmp2"), exchangeDirectory)
        }
        shouldFail(VcsException) {
            // test for non empty directory
            File nonEmpty = new File("target/vcs/tmp2")
            nonEmpty.mkdir()
            FileUtils.touch(new File("target/vcs/tmp2/tmp"))
            svn.init(nonEmpty, exchangeDirectory)
        }
        shouldFail(VcsException) {
            // test that exchange directory is not a file
            svn.init(workingCopy, new File("target/vcs/tmp"))
        }
        shouldFail(VcsException) {
            // test that exchange directory exists
            svn.init(workingCopy, new File("target/vcs/tmp3"))
        }
        // init should work now
        svn.init(workingCopy, exchangeDirectory)
        shouldFail(VcsAlreadyInitedException) {
            // test that we cannot init the checkout twice
            svn.init(workingCopy, exchangeDirectory)
        }
    }

    void testImport() {
        // not yet inited, so it should fail
        shouldFail(VcsNotInitedException) {
            svn.importFile(new File("/tmp"), "tmp")
        }
        svn.init(workingCopy, exchangeDirectory)
        shouldFail(VcsException) {
            // non existing file
            svn.importFile(new File("target/vcs/tmp"), "test")
        }
        shouldFail(VcsException) {
            // directory instead of file
            File directory = new File("target/vcs/tmp")
            directory.mkdirs()
            svn.importFile(directory, "test")
        }
        File importFile = new File("target/vcs/tmp/test")
        FileUtils.touch(importFile)
        shouldFail(FileAlreadyVersionedException) {
            // test whether the file already exists in the directory
            FileUtils.touch(new File(workingCopy.absolutePath + File.separator + "test"))
            svn.importFile(importFile, "test")
        }
        File importedFile = new File(workingCopy.absolutePath + File.separator + "test")
        // created the temp file in previous step - ensure it is deleted again
        importedFile.delete()
        String revision = svn.importFile(importFile, "test")
        assertTrue(importedFile.exists())
        assertTrue(importedFile.isFile())
        assertEquals("1", revision)
        // verify commit message
        SVNRepository repository = SVNRepositoryFactory.create(url)
        Collection logEntries = repository.log( new String[0], null, Long.parseLong(revision), Long.parseLong(revision), true, true)
        assertEquals(1, logEntries.size())
        assertEquals("Import of test", ((SVNLogEntry)logEntries[0]).getMessage())
        shouldFail(FileAlreadyVersionedException) {
            svn.importFile(importFile, "test")
        }
        // import a second file with custom commit message
        revision = svn.importFile(importFile, "test2", "Custom commit message")
        assertEquals("2", revision)
        logEntries = repository.log(new String[0], null, Long.parseLong(revision), Long.parseLong(revision), true, true)
        assertEquals(1, logEntries.size())
        assertEquals("Custom commit message", ((SVNLogEntry)logEntries[0]).getMessage())
    }

    void testUpdate() {
        shouldFail(VcsNotInitedException) {
            svn.updateFile(new File("/tmp"), "tmp")
        }
        svn.init(workingCopy, exchangeDirectory)
        shouldFail(VcsException) {
            // non existing file
            svn.updateFile(new File("target/vcs/tmp"), "test")
        }
        shouldFail(VcsException) {
            // directory instead of file
            File directory = new File("target/vcs/tmp")
            directory.mkdirs()
            svn.updateFile(directory, "test")
        }
        File importFile = new File("target/vcs/tmp/test")
        FileUtils.touch(importFile)
        shouldFail(FileNotVersionedException) {
            svn.updateFile(importFile, "test")
        }
        // import the file
        svn.importFile(importFile, "test")
        // now update it
        importFile.append("Some test text\n")
        String revision = svn.updateFile(importFile, "test")
        assertEquals("2", revision)
        // verify commit message
        SVNRepository repository = SVNRepositoryFactory.create(url)
        Collection logEntries = repository.log( new String[0], null, Long.parseLong(revision), Long.parseLong(revision), true, true)
        assertEquals(1, logEntries.size())
        assertEquals("Update of test", ((SVNLogEntry)logEntries[0]).getMessage())
        // custom commit message
        importFile.append("Some more text")
        revision = svn.updateFile(importFile, "test", "Test commit message")
        assertEquals("3", revision)
        logEntries = repository.log( new String[0], null, Long.parseLong(revision), Long.parseLong(revision), true, true)
        assertEquals(1, logEntries.size())
        assertEquals("Test commit message", ((SVNLogEntry)logEntries[0]).getMessage())
    }

    void testRetrieveFile() {
        shouldFail(VcsNotInitedException) {
            svn.retrieveFile("/tmp")
        }
        svn.init(workingCopy, exchangeDirectory)
        shouldFail(FileNotVersionedException) {
            svn.retrieveFile("test")
        }
        File importFile = new File("target/vcs/tmp/test")
        FileUtils.touch(importFile)
        // import the file
        svn.importFile(importFile, "test")
        // now update it
        importFile.append("Some test text\n")
        svn.updateFile(importFile, "test")
        // retrieve the file
        File retrievedFile = svn.retrieveFile("test")
        String path = retrievedFile.absolutePath
        assertTrue(retrievedFile.exists())
        assertTrue(retrievedFile.isFile())
        List<String> lines = retrievedFile.readLines()
        assertEquals(1, lines.size())
        assertEquals("Some test text", lines[0])
        // passing invalid revision should fail
        shouldFail(VcsException) {
            retrievedFile = svn.retrieveFile("test", "not a number")
        }
        shouldFailWithCause(SVNException) {
            retrievedFile = svn.retrieveFile("test", "3")
        }
        retrievedFile = svn.retrieveFile("test", "1")
        assertTrue("Retrieved files have same path, but each retrieved file should have a unique identifier", path != retrievedFile.absolutePath)
        assertEquals(0, retrievedFile.readBytes().length)
    }

    void testUpdateWorkingCopy() {
        shouldFail(VcsNotInitedException) {
            svn.updateWorkingCopy()
        }
        svn.init(workingCopy, exchangeDirectory)
        // TODO: write suited test case
    }
}
