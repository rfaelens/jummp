/**
* Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI),
* Deutsches Krebsforschungszentrum (DKFZ)
*
* This file is part of Jummp.
*
* Jummp is free software; you can redistribute it and/or modify it under the
* terms of the GNU Affero General Public License as published by the Free
* Software Foundation; either version 3 of the License, or (at your option) any
* later version.
*
* Jummp is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
* details.
*
* You should have received a copy of the GNU Affero General Public License along
* with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
*
* Additional permission under GNU Affero GPL version 3 section 7
*
* If you modify Jummp, or any covered work, by linking or combining it with
* Apache Commons, JUnit (or a modified version of that library), containing parts
* covered by the terms of Common Public License, Apache License v2.0, the licensors of this
* Program grant you additional permission to convey the resulting work.
* {Corresponding Source for a non-source form of such a combination shall
* include the source code for the parts of Apache Commons, JUnit used as well as
* that of the covered work.}
**/





package net.biomodels.jummp.core

import grails.test.mixin.TestMixin
import grails.test.mixin.integration.IntegrationTestMixin
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

@TestMixin(IntegrationTestMixin)
class SearchTests extends JummpIntegrationTest {
    def modelService
    def fileSystemService
    def grailsApplication

    @Test
    void testGetLatestRevision() {
    	    
    	// generate unique ids for the name and description
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

    	// upload the model
        def rf = new RepositoryFileTransportCommand(path: smallModel("importModel.xml", nameTag, descriptionTag).absolutePath, mainFile:true, description: "")
        Model upped = modelService.uploadModelAsFile(rf, new ModelTransportCommand(format:
                new ModelFormatTransportCommand(identifier: "SBML"), comment: "test", name: "Test"))
        // refresh the search index (we dont want to wait 5 mins) (once we switch to 4.4)       
        //grailsApplication.mainContext.getBean("searchEngine").refreshIndex()
        
        // Search for the model using the unique name and description, and ensure its the same we uploaded       
        assertSame(upped.id,searchForModel(nameTag).id)
        assertSame(upped.id, searchForModel(descriptionTag).id)
    }

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

    
    // Convenience functions follow..
    
    
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

