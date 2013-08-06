package net.biomodels.jummp.plugins

import net.biomodels.jummp.core.JummpIntegrationTest
import net.biomodels.jummp.core.model.ModelFormatTransportCommand
import net.biomodels.jummp.core.model.ModelTransportCommand
import net.biomodels.jummp.core.model.RepositoryFileTransportCommand
import net.biomodels.jummp.core.model.RevisionTransportCommand
import net.biomodels.jummp.model.Model
import net.biomodels.jummp.model.ModelFormat
import net.biomodels.jummp.model.Revision
import net.biomodels.jummp.plugins.git.GitManagerFactory
import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.junit.*
import static org.junit.Assert.*

/**
 * Test for SbmlService parts which require a running core to retrieve Models.
 */
class SbmlServiceTests extends JummpIntegrationTest {
    /**
     * Dependency injection of SbmlService
     */
    def sbmlService
    def modelService
    def fileSystemService
    def grailsApplication

    @Before
    void setUp() {
        createUserAndRoles()
        //setupVcs()
        fileSystemService.root = new File("target/sbml/git/").getCanonicalFile()
        fileSystemService.currentModelContainer = fileSystemService.root.absolutePath + "/ttt/"
        // disable validation as it is broken
        grailsApplication.config.jummp.plugins.sbml.validation = false
    }

    @After
    void tearDown() {
        FileUtils.deleteDirectory(new File("target/sbml"))
    }

    @Test
    void testExtractName() {
        File myModel = null
        assertEquals("", sbmlService.extractName([myModel]))
        myModel = new File("target/sbml/myModel")
        FileUtils.deleteQuietly(myModel)
        assertEquals("", sbmlService.extractName([myModel]))
        assertEquals("", sbmlService.extractName([myModel, myModel]))
        myModel = new File("test/files/BIOMD0000000272.xml")
        assertEquals("Becker2010_EpoR_AuxiliaryModel", sbmlService.extractName([myModel]))
    }

    @Test
    void testAreFilesThisFormat() {
        File file = new File("target/sbml/test")
        FileUtils.deleteQuietly(file)
        FileUtils.touch(file)
        assertFalse(sbmlService.areFilesThisFormat([file]))

        // unknown sbml
        File unknown = new File("target/sbml/unknown")
        FileUtils.deleteQuietly(unknown)
        FileUtils.touch(unknown)
        unknown.append('''\
<?xml version='1.0' encoding='UTF-8'?>
<model/>''')
        assertFalse(sbmlService.areFilesThisFormat([unknown]))

        File properSbml = new File("test/files/BIOMD0000000272.xml")
        assertTrue(sbmlService.areFilesThisFormat([properSbml]))

        assertTrue(sbmlService.areFilesThisFormat([properSbml,smallModel("validSbml.xml")]))
        assertFalse(sbmlService.areFilesThisFormat([properSbml, unknown]))
    }


    @Test
    void testLevelAndVersion() {
        authenticateAsTestUser()
        def rf = new RepositoryFileTransportCommand(path: smallModel("BIOMD0000000272.xml").absolutePath,
                    description: "", mainFile: true)
        Model model = modelService.uploadModelAsFile(rf, new ModelTransportCommand(format:
                new ModelFormatTransportCommand(identifier: "SBML"), comment: "test", name: "Test"))
        RevisionTransportCommand rev = modelService.getLatestRevision(model).toCommandObject()
        assertEquals(1, sbmlService.getLevel(rev))
        assertEquals(1, sbmlService.getVersion(rev))
        rf.path = "test/files/BIOMD0000000272.xml"
        RevisionTransportCommand rev2 = modelService.addRevisionAsFile(model, rf,
                ModelFormat.findByIdentifier("SBML"), "test").toCommandObject()
        assertEquals(2, sbmlService.getLevel(rev2))
        assertEquals(4, sbmlService.getVersion(rev2))
    }

    @Test
    void testModelMetaId() {
        authenticateAsTestUser()
        def rf = new RepositoryFileTransportCommand(path: smallModel("BIOMD0000000272.xml"), description: "")
        Model model = modelService.uploadModelAsFile(rf, new ModelTransportCommand(format:
                new ModelFormatTransportCommand(identifier: "SBML"), comment: "test", name: "Test"))
        RevisionTransportCommand rev = modelService.getLatestRevision(model).toCommandObject()
        assertEquals("", sbmlService.getMetaId(rev))
        rf.path = "test/files/BIOMD0000000272.xml"
        RevisionTransportCommand rev2 = modelService.addRevisionAsFile(model, rf,
                ModelFormat.findByIdentifier("SBML"), "test").toCommandObject()
        assertEquals("_688624", sbmlService.getMetaId(rev2))
    }

    private File getFileForTest(String filename, String text)
    {
        def tempDir = FileUtils.getTempDirectory()
        def testFile = new File(tempDir.absolutePath + File.separator + filename)
        if (text) {
            testFile.setText(text)
        }
        return testFile
    }

    @Test
    void testModelNotes() {
        authenticateAsTestUser()
        def rf = new RepositoryFileTransportCommand(path: smallModel("testModelNotes.xml").absolutePath, description: "")
        Model model = modelService.uploadModelAsFile(rf, new ModelTransportCommand(format: 
                new ModelFormatTransportCommand(identifier: "SBML"), comment: "test", name: "Test"))
        RevisionTransportCommand rev = modelService.getLatestRevision(model).toCommandObject()
        assertEquals("", sbmlService.getNotes(rev))

        File modelWithNotes = getFileForTest("testModelNotes.xml",'''<?xml version="1.0" encoding="UTF-8"?>
<sbml xmlns="http://www.sbml.org/sbml/level1" level="1" version="1">
  <model>
    <notes><body xmlns="http://www.w3.org/1999/xhtml"><p>Test</p></body></notes>
    <listOfCompartments>
      <compartment name="x"/>
    </listOfCompartments>
    <listOfSpecies>
      <specie name="y" compartment="x" initialAmount="1"/>
    </listOfSpecies>
    <listOfReactions>
      <reaction name="r">
        <listOfReactants>
          <specieReference specie="y"/>
        </listOfReactants>
        <listOfProducts>
          <specieReference specie="y"/>
        </listOfProducts>
      </reaction>
    </listOfReactions>
  </model>
</sbml>''')
        rf.path = modelWithNotes.absolutePath
        RevisionTransportCommand rev2 = modelService.addRevisionAsFile(model, rf, ModelFormat.findByIdentifier("SBML"), "test").toCommandObject()
        assertEquals('''<notes>\n  <body xmlns="http://www.w3.org/1999/xhtml">\n<p>Test</p>\n    </body>\n  \n</notes>''', sbmlService.getNotes(rev2))
    }

    private void setupVcs() {
        // setup VCS
        File clone = new File("target/sbml/git/")
        clone.mkdirs()
        FileRepositoryBuilder builder = new FileRepositoryBuilder()
        Repository repository = builder.setWorkTree(clone)
        .readEnvironment() // scan environment GIT_* variables
        .findGitDir(clone) // scan up the file system tree
        .build()
        Git git = new Git(repository)
        git.init().setDirectory(clone).call()
        GitManagerFactory gitService = new GitManagerFactory()
        gitService.grailsApplication = grailsApplication
        grailsApplication.config.jummp.plugins.git.enabled = true
        grailsApplication.config.jummp.vcs.workingDirectory = "target/sbml/git/"
        File exchangeDir = new File("target/sbml/exchange/")
        exchangeDir.mkdirs()
        grailsApplication.config.jummp.vcs.exchangeDirectory = exchangeDir.path
        modelService.vcsService.vcsManager = gitService.getInstance()
    }

    private File smallModel(String filename) {
        return getFileForTest(filename, '''\
<?xml version="1.0" encoding="UTF-8"?>
<sbml xmlns="http://www.sbml.org/sbml/level1" level="1" version="1">
  <model>
    <listOfCompartments>
      <compartment name="x"/>
    </listOfCompartments>
    <listOfSpecies>
      <specie name="y" compartment="x" initialAmount="1"/>
    </listOfSpecies>
    <listOfReactions>
      <reaction name="r">
        <listOfReactants>
          <specieReference specie="y"/>
        </listOfReactants>
        <listOfProducts>
          <specieReference specie="y"/>
        </listOfProducts>
      </reaction>
    </listOfReactions>
  </model>
</sbml>''')
    }
}
