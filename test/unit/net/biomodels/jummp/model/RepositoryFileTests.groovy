package net.biomodels.jummp.model

import grails.test.mixin.*
import org.apache.commons.io.FileUtils
import org.junit.*
import net.biomodels.jummp.model.Revision

@TestFor(RepositoryFile)
class RepositoryFileTests {
    @Before
    public void setUp() {
        new File("target/vcs/aaa/model1/").mkdirs()
     }

    @After
    public void tearDown() {
        FileUtils.deleteQuietly(new File("target/vcs/aaa"))
    }

    @Test
    void testConstraints() {
        def f = createFile("target/vcs/aaa/model1/m1.xml")
        def repositoryFile = new RepositoryFile(path: f.absolutePath, description: "Model1",
               mainFile: true, hidden: false, mimeType: "application/xml", revision: null)
        mockForConstraintsTests(RepositoryFile, [repositoryFile])

        //nulls are not accepted
        repositoryFile = new RepositoryFile()
        assertFalse(repositoryFile.validate())
        assertEquals(3, repositoryFile.errors.getErrorCount())
        assertEquals("nullable", repositoryFile.errors["path"])
        assertEquals("nullable", repositoryFile.errors["description"])
        assertEquals("nullable", repositoryFile.errors["revision"])

        // path cannot be blank
        def rev = new Revision(vcsId: "1", revisionNumber: 1, minorRevision: false,
                uploadDate: new Date(), comment: "", owner: null, model: null)
        repositoryFile = new RepositoryFile(path: "", description: "nothing", hidden: false,
                mainFile: false, mimeType: "application/octet-stream", revision: rev)
        assertFalse(repositoryFile.validate())
        assertEquals("blank", repositoryFile.errors["path"])
        assertEquals(1, repositoryFile.errors.getErrorCount())

        // empty file descriptions and mimeTypes are acceptable
        def newPath = createFile("target/vcs/aaa/model1/m1a.xml").absolutePath
        assertTrue(new File(newPath).exists())
        def repositoryFile2 = new RepositoryFile(path: newPath, description: "", mainFile: false,
                mimeType: "", hidden: false, revision: new Revision())
        assertTrue(repositoryFile2.validate())

        //paths must be unique
        mockForConstraintsTests(RepositoryFile, [repositoryFile2])
        def duplicateFile = new RepositoryFile(path: newPath, description: "",
            mainFile: true, hidden: false, mimeType: "text/plain", revision: rev)
        assertFalse(duplicateFile.validate())
        assertEquals(1, duplicateFile.errors.getErrorCount())
        assertEquals("unique", duplicateFile.errors["path"])

        //descriptions can have at-most 500 characters
        repositoryFile2.description = "a"*500
        assertTrue(repositoryFile2.validate())
        assertNull(repositoryFile2.errors["description"])
        repositoryFile2.description += "b"
        assertFalse(repositoryFile2.validate())
        assertEquals(1, repositoryFile2.errors.getErrorCount())
        assertEquals("maxSize", repositoryFile2.errors["description"])

        //main submission entries cannot be hidden from the user
        def mainFile = createFile("target/vcs/aaa/model1/m1b.xml")
        def rf = new RepositoryFile(path: mainFile.absolutePath, description: "Silly model",
                mainFile: true, mimeType: "", hidden: true, revision: new Revision())
        assertFalse(rf.validate())
        assertEquals(1, rf.errors.getErrorCount())
        assertEquals("validator", rf.errors["mainFile"])
        rf.mainFile = false
        assertTrue(rf.validate())
        rf.mainFile = true
        rf.hidden = false
        assertTrue(rf.validate())
    }

    private File createFile(String path) {
        if (path == null) {
            path = "error.xml"
        }
        def f = new File(path)
        f.text = '''<?xml version="1.0"?>
    <foo>
        <bar>baz</bar>
    </foo>
'''
        return f.getCanonicalFile()
    }
}
