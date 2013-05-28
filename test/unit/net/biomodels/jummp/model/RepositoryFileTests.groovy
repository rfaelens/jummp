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
        def test = new File("target/vcs/aaa/model1/test.txt")
        def fout
        try {
            fout = new BufferedWriter(new FileWriter(test))
            fout.append("This is a test\n")
        } catch (IOException ignored) {
        }
        finally {
            fout.flush()
            fout.close()
        }
    }

    @After
    public void tearDown() {
        FileUtils.deleteQuietly(new File("target/vcs/aaa"))
    }

    @Test
    void testConstraints() {
        def f = createFile("target/vcs/aaa/model1/m1.xml")
        def repositoryFile = new RepositoryFile(path: f.absolutePath, description: "Model1",
                hidden: false, revision: null)
        mockForConstraintsTests(RepositoryFile, [repositoryFile])

        //nulls are not accepted
        repositoryFile = new RepositoryFile()
        assertFalse(repositoryFile.validate())
        assertEquals(3, repositoryFile.errors.getErrorCount())
        assertEquals("nullable", repositoryFile.errors["path"])
        assertEquals("nullable", repositoryFile.errors["description"])
        assertEquals("nullable", repositoryFile.errors["revision"])

        // path cannot be blank
        def rev = new Revision(vcsId: "1", revisionNumber: 1, minorRevision: false, uploadDate: new Date(),
                comment: "", owner: null, model: null)
        repositoryFile = new RepositoryFile(path: "", description: "nothing", hidden: false, revision: rev)
        assertFalse(repositoryFile.validate())
        assertEquals("blank", repositoryFile.errors["path"])
        assertEquals(1, repositoryFile.errors.getErrorCount())

        // empty file descriptions are acceptable
        def newPath = createFile("target/vcs/aaa/model1/m1a.xml").absolutePath
        assertTrue(new File(newPath).exists())
        def repositoryFile2 = new RepositoryFile(path: newPath, description: "", hidden: false, revision: new Revision())
        assertTrue(repositoryFile2.validate())
        assertNull(repositoryFile2.errors["description"])

        //paths must be unique
        mockForConstraintsTests(RepositoryFile, [repositoryFile2])
        def duplicateFile = new RepositoryFile(path: newPath, description: "",
            hidden: false, revision: rev)
        assertFalse(duplicateFile.validate())
        assertEquals(1, duplicateFile.errors.getErrorCount())
        assertEquals("unique", duplicateFile.errors["path"])

        //descriptions can have at-most 100 characters
        repositoryFile2.description = "a"*100
        assertTrue(repositoryFile2.validate())
        assertNull(repositoryFile2.errors["description"])
        repositoryFile2.description += "b"
        assertFalse(repositoryFile2.validate())
        assertEquals(1, repositoryFile2.errors.getErrorCount())
        assertEquals("maxSize", repositoryFile2.errors["description"])
    }

    private File createFile(String path) {
        if (path == null) {
            path = ".${File.separator}error.txt".toString()
        }
        File f = new File(path)
        FileUtils.touch(f)
        return f.getCanonicalFile()
    }
}
