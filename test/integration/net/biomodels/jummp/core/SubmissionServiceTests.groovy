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
* JGit, Apache Commons, JUnit, Spring Security (or a modified version of that library), containing parts
* covered by the terms of Common Public License, Apache License v2.0, Eclipse Distribution License v1.0, the licensors of this
* Program grant you additional permission to convey the resulting work.
* {Corresponding Source for a non-source form of such a combination shall
* include the source code for the parts of JGit, Apache Commons, JUnit, Spring Security used as well as
* that of the covered work.}
**/





package net.biomodels.jummp.core

import grails.test.mixin.TestMixin
import grails.test.mixin.integration.IntegrationTestMixin
import net.biomodels.jummp.core.model.ModelFormatTransportCommand
import net.biomodels.jummp.core.model.ModelTransportCommand
import net.biomodels.jummp.core.model.RepositoryFileTransportCommand
import net.biomodels.jummp.model.Model
import net.biomodels.jummp.model.Revision
import net.biomodels.jummp.plugins.git.GitManagerFactory
import net.biomodels.jummp.plugins.security.Role
import net.biomodels.jummp.plugins.security.User
import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.junit.*
import static java.util.UUID.randomUUID
import org.springframework.security.acls.domain.BasePermission
import static org.junit.Assert.*

@TestMixin(IntegrationTestMixin)
class SubmissionServiceTests extends JummpIntegrationTest {
    /**
     * Dependency Injection of Grails Application
     */
    def grailsApplication
    /**
     * Dependency Injection of SubmissionService
     */
    def submissionService
    /**
    * Dependency injection of modelService
    */
    def modelService
    
    @Before
    void setUp() {
        // Setup logic here
        createUserAndRoles()
        setupVcs()
    }

    @After
    void tearDown() {
        // Tear down logic here
        FileUtils.deleteDirectory(new File("target/vcs/git"))
        FileUtils.deleteDirectory(new File("target/vcs/resource"))
        FileUtils.deleteDirectory(new File("target/vcs/repository"))
        FileUtils.deleteDirectory(new File("target/vcs/exchange"))
        modelService.vcsService.vcsManager = null
    }

    // TODO: remove this copy from JmsAdapterServiceTest
    private void setupVcs() {
        File clone = new File("target/vcs/git")
        assertTrue(clone.mkdirs())
        FileRepositoryBuilder builder = new FileRepositoryBuilder()
        Repository repository = builder.setWorkTree(clone)
        .readEnvironment() // scan environment GIT_* variables
        .findGitDir() // scan up the file system tree
        .build()
        Git git = new Git(repository)
        git.init().setDirectory(clone).call()
        GitManagerFactory gitService = new GitManagerFactory()
        gitService.grailsApplication = grailsApplication
        grailsApplication.config.jummp.plugins.git.enabled = true
        grailsApplication.config.jummp.vcs.workingDirectory = "target/vcs/git"
        grailsApplication.config.jummp.vcs.exchangeDirectory = "target/vcs/exchange"
        assertTrue(new File("target/vcs/exchange/").mkdirs())
        modelService.vcsService.vcsManager = gitService.getInstance()
        assertTrue(modelService.vcsService.isValid())
    }

    private String addTestFile(Map<String, Object> workingMemory) {
    	String guid=randomUUID() as String
    	def mains=[getFile("mainFile.txt", "this is a main file")]
    	File additional=getFile("addFile.txt", guid)
    	Map<File, String> adds=new HashMap<File, String>()
    	adds.put(additional, "this is a desc")
    	workingMemory.put("submitted_mains", mains)
    	workingMemory.put("submitted_additionals", adds)
    	submissionService.handleFileUpload(workingMemory)
    	assertTrue(workingMemory.containsKey("repository_files"))
    	assertTrue(workingMemory.get("repository_files").size() == 2)
    	submissionService.inferModelFormatType(workingMemory)
    	assertTrue(workingMemory.containsKey("model_type"))
    	assertEquals(workingMemory.get("model_type") as String, "UNKNOWN")
    	submissionService.performValidation(workingMemory)
    	assertTrue(workingMemory.containsKey("model_validation_result"))
    	assertEquals(workingMemory.get("model_validation_result") as Boolean, true)
    	workingMemory.put("Valid", true)
    	submissionService.inferModelInfo(workingMemory)
    	assertTrue(workingMemory.containsKey("RevisionTC"))
    	return guid
    }
    
    private String addAdditionalFile(Map<String, Object> workingMemory, String directory=null) {
    	String guid=randomUUID() as String
    	File additional
    	if (!directory) {
    		additional=getFile("addFile.txt", guid)
    	}
    	else {
    		additional=getFile("addFile.txt", guid, directory)
    	}
    	Map<File, String> adds=new HashMap<File, String>()
    	adds.put(additional, "this is a desc")
    	workingMemory.put("submitted_mains", [])
    	workingMemory.put("submitted_additionals", adds)
    	submissionService.handleFileUpload(workingMemory)
    	assertTrue(workingMemory.containsKey("repository_files"))
    	System.out.println(workingMemory.inspect())
    	System.out.println(workingMemory.get("repository_files").inspect())
    	(workingMemory.get("repository_files") as List).each {
    		System.out.println(it.path)
    	}
    	assertTrue(workingMemory.get("repository_files").size() == 2)
    	submissionService.inferModelFormatType(workingMemory)
    	assertTrue(workingMemory.containsKey("model_type"))
    	assertEquals(workingMemory.get("model_type") as String, "UNKNOWN")
    	submissionService.performValidation(workingMemory)
    	assertTrue(workingMemory.containsKey("model_validation_result"))
    	assertEquals(workingMemory.get("model_validation_result") as Boolean, true)
    	workingMemory.put("Valid", true)
    	submissionService.inferModelInfo(workingMemory)
    	assertTrue(workingMemory.containsKey("RevisionTC"))
    	return guid
    }

   
    private void deleteAdditionalFile(Map<String, Object> workingMemory) {
    	def deleteThese=["addFile.txt"]
    	workingMemory.put("deleted_filenames", deleteThese)
    	submissionService.handleFileUpload(workingMemory)
    	assertTrue(workingMemory.containsKey("repository_files"))
    	assertTrue(workingMemory.get("repository_files").size() == 1)
    	submissionService.inferModelInfo(workingMemory)
    	assertTrue(workingMemory.containsKey("RevisionTC"))
    }
   
    
    private void confirmFile(String modelID, String confirmationText) {
    	List<File> files=modelService
    						.retrieveModelRepFiles(
    									modelService.getLatestRevision(
    											modelService.getModel(Long.parseLong(modelID))))
    	if (!confirmationText) { //no confirmation text means the file shouldnt exist
    		assertNull(files.find { it.getName() == "addFile.txt"})
    	}
    	else {
    		File testFile=files.find { it.getName() == "addFile.txt" }
    		String fileText=testFile.getText()
    		assertEquals(fileText, confirmationText)
    		assertFalse(fileText == "not this text")
    	}
    }


    @Test
    void testModelSubmission() {
    	assertNotNull(authenticateAsTestUser())
        Map<String, Object> workingMemory=new HashMap<String, Object>()
    	workingMemory.put("isUpdateOnExistingModel", false)
    	submissionService.initialise(workingMemory)
    	String testThis=addTestFile(workingMemory)
    	def revSummary=['RevisionComments':'Model revised without commit message']
    	submissionService.updateFromSummary(workingMemory, revSummary)
    	submissionService.handleSubmission(workingMemory)
    	assertTrue(workingMemory.containsKey("model_id"))
		confirmFile(workingMemory.get("model_id") as String, testThis) 	
    }
    
    @Test
    void testModelSubmissionWithDelete() {
    	assertNotNull(authenticateAsTestUser())
        Map<String, Object> workingMemory=new HashMap<String, Object>()
    	workingMemory.put("isUpdateOnExistingModel", false)
    	submissionService.initialise(workingMemory)
    	String testThis=addTestFile(workingMemory)
    	deleteAdditionalFile(workingMemory)
    	def revSummary=['RevisionComments':'Model revised without commit message']
    	submissionService.updateFromSummary(workingMemory, revSummary)
    	submissionService.handleSubmission(workingMemory)
    	assertTrue(workingMemory.containsKey("model_id"))
		confirmFile(workingMemory.get("model_id") as String, null) 	
    }

    @Test
    void testModelUpdate() {
    	assertNotNull(authenticateAsTestUser())
        Map<String, Object> workingMemory=new HashMap<String, Object>()
    	workingMemory.put("isUpdateOnExistingModel", false)
    	submissionService.initialise(workingMemory)
    	String testThis=addTestFile(workingMemory)
    	def revSummary=['RevisionComments':'Model revised without commit message']
    	submissionService.updateFromSummary(workingMemory, revSummary)
    	submissionService.handleSubmission(workingMemory)
    	assertTrue(workingMemory.containsKey("model_id"))
    	long model_id=Long.parseLong(workingMemory.get("model_id") as String)
		confirmFile(""+model_id, testThis)
		workingMemory=new HashMap<String, Object>()
    	workingMemory.put("isUpdateOnExistingModel", true)
    	workingMemory.put("model_id", model_id)
    	workingMemory.put("LastRevision", modelService.getLatestRevision(modelService.getModel(model_id)).toCommandObject())
    	submissionService.initialise(workingMemory)
    	File revisionFile=new File(workingMemory.get("LastRevision").getFiles()[0].path)
		testThis=addAdditionalFile(workingMemory, revisionFile.getParent())
		submissionService.updateFromSummary(workingMemory, revSummary)
    	submissionService.handleSubmission(workingMemory)
    	confirmFile(workingMemory.get("model_id") as String, testThis)
    }
    
    @Test
    void testModelDelete() {
    	assertNotNull(authenticateAsTestUser())
        Map<String, Object> workingMemory=new HashMap<String, Object>()
    	workingMemory.put("isUpdateOnExistingModel", false)
    	submissionService.initialise(workingMemory)
    	String testThis=addTestFile(workingMemory)
    	def revSummary=['RevisionComments':'Model revised without commit message']
    	submissionService.updateFromSummary(workingMemory, revSummary)
    	submissionService.handleSubmission(workingMemory)
    	assertTrue(workingMemory.containsKey("model_id"))
    	long model_id=Long.parseLong(workingMemory.get("model_id") as String)
		confirmFile(""+model_id, testThis)
		workingMemory=new HashMap<String, Object>()
    	workingMemory.put("isUpdateOnExistingModel", true)
    	workingMemory.put("model_id", model_id)
    	workingMemory.put("LastRevision", modelService.getLatestRevision(modelService.getModel(model_id)).toCommandObject())
    	submissionService.initialise(workingMemory)
    	deleteAdditionalFile(workingMemory)
		submissionService.updateFromSummary(workingMemory, revSummary)
    	submissionService.handleSubmission(workingMemory)
    	confirmFile(workingMemory.get("model_id") as String, null)
    }
    

    protected File getFile(String filename, String text, String directory=grailsApplication.config.jummp.vcs.exchangeDirectory)
    {
        File testFile=new File(new File(directory),
        					   filename)
        testFile.setText(text)
        return testFile
    }
    
    
}
