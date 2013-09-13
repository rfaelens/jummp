package net.biomodels.jummp.core

import net.biomodels.jummp.core.model.ModelFormatTransportCommand
import net.biomodels.jummp.core.model.ModelState
import net.biomodels.jummp.core.model.ModelTransportCommand
import net.biomodels.jummp.core.model.RepositoryFileTransportCommand
import net.biomodels.jummp.model.Model
import net.biomodels.jummp.plugins.git.GitManagerFactory
import net.biomodels.jummp.plugins.security.User
import org.apache.commons.io.FileUtils
import org.junit.*
import static org.junit.Assert.*

class SearchTests extends JummpIntegrationTest {
    def modelService
    def fileSystemService
    def grailsApplication

    @Before
    void setUp() {
        def container = new File("target/vcs/git/ggg/")
        container.mkdirs()
        new File("target/vcs/exchange/").mkdirs()
        fileSystemService.currentModelContainer = container.getCanonicalPath()
        fileSystemService.root = container.getParentFile()
        createUserAndRoles()
    }

    @After
     void tearDown() {
        try {
     	     FileUtils.deleteDirectory(new File("target/vcs/git"))
     	     FileUtils.deleteDirectory(new File("target/vcs/exchange"))
     	}
     	catch(Exception ignore) {
     	}
        modelService.vcsService.vcsManager = null
    }

    ModelTransportCommand searchForModel(String query) {
    	 Set<Model> mods=modelService.searchModels(query)
    	 if (mods.isEmpty()) {
    	 	 return null;
    	 }
    	 return mods.first()
    }
    
    @Test
    void testGetLatestRevision() {
        // create Model with one revision, without ACL
        String nameTag=UUID.randomUUID().toString()
        String descriptionTag=UUID.randomUUID().toString()
        authenticateAsTestUser()
        GitManagerFactory gitService = new GitManagerFactory()
        gitService.grailsApplication = grailsApplication
        grailsApplication.config.jummp.plugins.git.enabled = true
        grailsApplication.config.jummp.vcs.exchangeDirectory = "target/vcs/exchange"
        grailsApplication.config.jummp.vcs.workingDirectory = "target/vcs/git"
        grailsApplication.config.jummp.plugins.sbml.validation = false
        modelService.vcsService.vcsManager = gitService.getInstance()

        def rf = new RepositoryFileTransportCommand(path: smallModel("importModel.xml", nameTag, descriptionTag).absolutePath, mainFile:true, description: "")
        Model upped = modelService.uploadModelAsFile(rf, new ModelTransportCommand(format:
                new ModelFormatTransportCommand(identifier: "SBML"), comment: "test", name: "Test"))
        grailsApplication.mainContext.getBean("searchEngine").refreshIndex()
        
        
        assertSame(upped.id,searchForModel(nameTag).id)
        assertSame(upped.id, searchForModel(descriptionTag).id)
    }
    
    private File smallModel(String filename, String id, String desc) {
        return getFileForTest(filename, "<?xml version=\"1.0\" encoding=\"UTF-8\"?><sbml xmlns=\"http://www.sbml.org/sbml/level1\" level=\"1\" version=\"1\">  <model name=\"${id}\">"
+ "<notes>${desc}</notes>"+'''   <listOfCompartments>
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
    
    private File getFileForTest(String filename, String text)
    {
        def tempDir = FileUtils.getTempDirectory()
        def testFile = new File(tempDir.absolutePath + File.separator + filename)
        if (text) {
            testFile.setText(text)
        }
        return testFile
    }

}

