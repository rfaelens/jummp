package net.biomodels.jummp.plugins

import net.biomodels.jummp.core.JummpIntegrationTestCase
import net.biomodels.jummp.model.Model
import net.biomodels.jummp.core.model.ModelTransportCommand
import net.biomodels.jummp.core.model.ModelFormatTransportCommand
import net.biomodels.jummp.model.Revision
import net.biomodels.jummp.core.model.RevisionTransportCommand
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.lib.Repository
import net.biomodels.jummp.plugins.git.GitManagerFactory
import org.apache.commons.io.FileUtils
import net.biomodels.jummp.model.ModelFormat

/**
 * Test for SbmlService parts which require a running core to retrieve Models.
 */
class SbmlServiceTests extends JummpIntegrationTestCase {
    /**
     * Dependency injection of SbmlService
     */
    def sbmlService
    def modelService

    protected void setUp() {
        super.setUp()
        createUserAndRoles()
        setupVcs()
        mockLogging(net.biomodels.jummp.plugins.sbml.SbmlService, true)
    }

    protected void tearDown() {
        super.tearDown()
        FileUtils.deleteDirectory(new File("target/sbml/git"))
        FileUtils.deleteDirectory(new File("target/sbml/exchange"))
    }

    void testLevelAndVersion() {
        authenticateAsTestUser()
        File modelFile = File.createTempFile("jummp", null)
        modelFile.append('''<?xml version="1.0" encoding="UTF-8"?>
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
        Model model = modelService.uploadModel(modelFile, new ModelTransportCommand(format: new ModelFormatTransportCommand(identifier: "SBML"), comment: "test", name: "Test"))
        RevisionTransportCommand rev = modelService.getLatestRevision(model).toCommandObject()
        assertEquals(1, sbmlService.getLevel(rev))
        assertEquals(1, sbmlService.getVersion(rev))
        RevisionTransportCommand rev2 = modelService.addRevision(model, new File("test/files/BIOMD0000000272.xml"), ModelFormat.findByIdentifier("SBML"), "test").toCommandObject()
        assertEquals(2, sbmlService.getLevel(rev2))
        assertEquals(4, sbmlService.getVersion(rev2))
    }

    void testModelMetaId() {
        authenticateAsTestUser()
        File modelFile = File.createTempFile("jummp", null)
        modelFile.append('''<?xml version="1.0" encoding="UTF-8"?>
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
        Model model = modelService.uploadModel(modelFile, new ModelTransportCommand(format: new ModelFormatTransportCommand(identifier: "SBML"), comment: "test", name: "Test"))
        RevisionTransportCommand rev = modelService.getLatestRevision(model).toCommandObject()
        assertEquals("", sbmlService.getMetaId(rev))
        RevisionTransportCommand rev2 = modelService.addRevision(model, new File("test/files/BIOMD0000000272.xml"), ModelFormat.findByIdentifier("SBML"), "test").toCommandObject()
        assertEquals("_688624", sbmlService.getMetaId(rev2))
    }

    private void setupVcs() {
        // setup VCS
        File clone = new File("target/sbml/git")
        clone.mkdirs()
        FileRepositoryBuilder builder = new FileRepositoryBuilder()
        Repository repository = builder.setWorkTree(clone)
        .readEnvironment() // scan environment GIT_* variables
        .findGitDir() // scan up the file system tree
        .build()
        Git git = new Git(repository)
        git.init().setDirectory(clone).call()
        GitManagerFactory gitService = new GitManagerFactory()
        mockConfig('''
            jummp.plugins.git.enabled=true
            jummp.vcs.workingDirectory="target/sbml/git"
            jummp.vcs.exchangeDirectory="target/sbml/exchange"
            ''')
        modelService.vcsService.vcsManager = gitService.getInstance()
    }
}
