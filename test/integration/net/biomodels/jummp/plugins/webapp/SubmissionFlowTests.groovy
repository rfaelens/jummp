/**
* Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI), Deutsches Krebsforschungszentrum (DKFZ)
*
* This file is part of Jummp.
*
* Jummp is free software; you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as
* published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
*
* Jummp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License along with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
*
* Additional permission under GNU Affero GPL version 3 section 7
*
* If you modify Jummp, or any covered work, by linking or combining it with [name of library] (or a modified version of that
* library), containing parts covered by the terms of [name of library's license], the licensors of this Program grant you additional
* permission to convey the resulting work. {Corresponding Source for a non-source form of such a combination shall include the source
* code for the parts of [name of library] used as well as that of the covered work.}
**/


package net.biomodels.jummp.plugins.webapp

import grails.test.WebFlowTestCase
import net.biomodels.jummp.core.JummpIntegrationTest
import net.biomodels.jummp.core.model.ModelFormatTransportCommand
import net.biomodels.jummp.core.model.ModelTransportCommand
import net.biomodels.jummp.core.model.RepositoryFileTransportCommand
import net.biomodels.jummp.core.model.RevisionTransportCommand as RTC
import net.biomodels.jummp.model.Model
import net.biomodels.jummp.model.Revision
import net.biomodels.jummp.webapp.ModelController
import org.apache.commons.io.FileUtils
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import org.junit.*
import org.springframework.mock.web.MockMultipartFile
import org.springframework.mock.web.MockMultipartHttpServletRequest
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import static org.junit.Assert.*

class SubmissionFlowTests extends JummpIntegrationTest {
    
    def grailsApplication
    def fileSystemService
    def modelService
    
    @Before
    void setUp() {
        super.createUserAndRoles()
        File exchangeDirectory = new File("target/vcs/exchange/")
        exchangeDirectory.mkdirs()
        grailsApplication.config.jummp.vcs.exchangeDirectory = "target/vcs/exchange/"
        grailsApplication.config.jummp.vcs.workingDirectory = "target/vcs/git/"
        File parentLocation = new File(grailsApplication.config.jummp.vcs.workingDirectory )
        parentLocation.mkdirs()
        fileSystemService.root = parentLocation
        fileSystemService.currentModelContainer = parentLocation.absolutePath + File.separator + "ttt"
    }

    @After
    void tearDown() {
        FileUtils.deleteDirectory(new File("target/vcs/git"))
        FileUtils.deleteDirectory(new File("target/vcs/exchange"))
    }

    /* Aborts at the first step */
    @Test
    void testDisclaimerAbort() {
        assertNotNull(authenticateAsTestUser())
        new TestDisclaimerAbort().testrun()
    }

    /* Continues after the disclaimer */
    @Test
    void testDisclaimerContinue() {
        assertNotNull(authenticateAsTestUser())
        new TestDisclaimerContinue().testrun()
    }

    /* Tests upload page, then clicks abort
     */ 
    @Test
    void testUploadFilesAbort() {
        assertNotNull(authenticateAsTestUser())
        new TestUploadFilesCancel().testrun()
    }

    /* Tests upload pipeline, first with an empty list, 
     * then with an unknown model */
    @Test
    void testUploadFilesContinue() {
        assertNotNull(authenticateAsTestUser())
        TestUploadFilesContinue continued=new TestUploadFilesContinue()
        continued.testrun()
    }

    @Test
    void testUpdateUploadedModel() {
        assertNotNull(authenticateAsAdmin())
        ModelTransportCommand meta = new ModelTransportCommand(comment: "Test Comment", name: "test", format: new ModelFormatTransportCommand(identifier: "UNKNOWN"))
        File importFile = new File("target/vcs/exchange/import.xml")
        FileUtils.touch(importFile)
        importFile.append("Test\n")
        def rf = new RepositoryFileTransportCommand(path: importFile.absolutePath, description: "")
        Model model = modelService.uploadModelAsFile(rf, meta)
        assertTrue(model.validate())
        new TestUpdateSbml(model.id, importFile).testrun()
    }

    /* Tests upload pipeline, first with an empty list, 
     * then with a known SBML model */
    @Test
    void testSubmitSBML() {
        assertNotNull(authenticateAsTestUser())
        new TestSubmitSBML().testrun()
    }

    @Test
    void testSubmitOmex() {
        authenticateAsTestUser()
        new TestSubmitOmex().testrun()
    }

    /**
     * WebFlowTestCase seems unable to cope with branching(different routes)
     * in the same test class. Therefore theres multiple classes implementing
     * the routes we want to be able to test
     *
     * Update: See SetupControllerIntegrationTests.groovy for a solution. Current
     * implementation left in to avoid recoding. The issue with the current version
     * is unnecessary instantiation of flows (just one class could be used). On
     * the other hand it is a little bit more modular than it might otherwise have
     * been. Raza Ali: 18/6/13
     * 
     * Base class for the different webflow tests for both create and update
     */
    
    abstract class FlowBase extends WebFlowTestCase {
        abstract def getFlow(); 
        abstract void performTest()

        /*
         * WebFlowTestCase doesnt support file uploads. Modify request
         * property to change that.  
        **/
        protected void setUp() {
           super.setUp()
           mockRequest = new MockMultipartHttpServletRequest()
           RequestContextHolder.setRequestAttributes(new GrailsWebRequest(mockRequest,mockResponse,mockServletContext,applicationContext))
           registerFlow("model/upload", new ModelController().uploadFlow)
        }
        
        /* Main Template method for testing. */
        public void testrun()
        {
            setUp()
            performTest()
            tearDown()
        }
        
        /* Clicks cancel, checks that flow is aborted. */
        protected void clickCancelEndFlow() {
            signalEvent("Cancel")
            assert "abort" == flowExecutionOutcome.id
        }
        
        /* Checks the current state against the supplied state id */
        protected void assertFlowState(String state) {
            assert state == flowExecution.activeSession.state.id
        }
        
        /* 
         * Convenience function to add the supplied main and additional files
         * to submission
         * */
        protected void addSubmissionFiles(List<File> mainFiles, Map<File, String> additionalFiles) {
            mainFiles.each {
                addFileToRequest(it, "mainFile", "application/xml")
            }
            additionalFiles.keySet().each {
                addFileToRequest(it, "extraFiles", "application/xml")
                (mockRequest as MockHttpServletRequest).addParameter("description",
                                                                     additionalFiles.get(it))
            }
        }

        /* 
         * Adds the supplied file with parameters as a mock multipart file 
         * 
         **/
        private void addFileToRequest(File modelFile, String formID, String contentType) {
            final file = new MockMultipartFile(formID, 
                                                     modelFile.getName(),
                                                     contentType,
                                                     modelFile.getBytes())
            (mockRequest as MockMultipartHttpServletRequest).addFile(file)
        }
        
        /* 
         * Convenience function to create arbitrary additional files with corresponding
         * descriptions. 
         **/
        protected Map<File,String> getRandomAdditionalFiles(int num) {
            Map<File,String> returnMe=new HashMap<File,String>()
            for (int i=0; i<num; i++) {
                returnMe.put(getFileForTest("add_file_"+i+".xml", "my text is "+num),
                             "this is a description for file "+i)
            }
            return returnMe
        }

        /* 
         * Convenience function to compare a map of String->byte[] retrieved from
         * the repository with the supplied list of files
         * */
        protected void validateFiles(List<RepositoryFileTransportCommand> retrieved, List<File> testFiles) {
            assert retrieved
            Map<String,byte[]> files=new HashMap<String,byte[]>()
            retrieved.each {
            	    File file=new File(it.path)
            	    files.put(file.getName(), file.getBytes())
            }
            testFiles.each {
                assert files.containsKey(it.getName())
                byte[] savedFile=files.get(it.getName())
                assert savedFile == it.getBytes()
            }
        }

    }
    
    /* Base class for Update flow tests. Sets the model id in the request
     * params as supplied in the constructor*/
    abstract class UpdateBase extends FlowBase {
        long modelid
        UpdateBase(long m) {
            modelid=m
        }
        protected void setUp() {
           super.setUp()
           mockRequest.setParameter("id",""+modelid)
        }
        def getFlow() {
            new ModelController().updateFlow
        }
    }

    /* Base class for the create flows*/
    abstract class CreateBase extends FlowBase {
        def getFlow() { 
            new ModelController().createFlow 
        }
    }

    /* Class for testing out the update mechanism. Creates a model with an
     * unknown file format. Then updates the model with an sbml file thereby
     * changing the model type. Ensures that the revision reflects the new
     * model's name and description (from the sbml file), and contains both
     * the original and the new model files*/
    class TestUpdateSbml extends UpdateBase {
        File existing
        TestUpdateSbml(long m, File uploaded) {
            super(m)
            existing=uploaded
        }
        void performTest() {
            def viewSelection = startFlow()
            //signalEvent("Continue")
            assertFlowState("uploadFiles")
            File newFile=bigModel()
            Map<File,String> additionalFiles=getRandomAdditionalFiles(10)
            addSubmissionFiles([newFile], additionalFiles)
            signalEvent("Upload")
            
            //assertFlowState("displayModelInfo") currently disabled
            assert true == (Boolean) flowScope.
                                        workingMemory.
                                        get("isUpdateOnExistingModel")
            assert "SBML" == flowScope.workingMemory.get("model_type") as String
            RTC revision=flowScope.workingMemory.get("RevisionTC") as RTC
            //test name
            assert "Becker2010_EpoR_AuxiliaryModel" == revision.name
            assert revision.description.contains("This relation solely depends on EpoR turnover independent of ligand binding, suggesting an essential role of large intracellular receptor pools. These receptor properties enable the system to cope with basal and acute demand in the hematopoietic system")
            
            //add tests for when displayModelInfo does something interesting
            //signalEvent("Continue") display model info disabled
            
            //Dont add publication info
            signalEvent("Continue")
            
            assertFlowState("displaySummaryOfChanges")
            Model model=modelService.getModel(modelid)
            Revision prev=modelService.getLatestRevision(model)
            assert prev
            signalEvent("Continue")
            //assert flowExecutionOutcome.id == "displayConfirmationPage"
            
            
            //test that the model is infact saved in the database
            Revision rev=modelService.getLatestRevision(model)
            //test that revision is saved correctly
            assert rev
            assert rev.comment.contains("Model revised without commit message")
            assert rev.revisionNumber==prev.revisionNumber+1
            
            //test that files are updated in the repository correctly
            List<RepositoryFileTransportCommand> files=modelService.retrieveModelFiles(model)
            validateFiles(files, [existing, newFile]+additionalFiles.keySet())
        }
    }

    class TestDisclaimerContinue extends CreateBase {
        void performTest() {
            def viewSelection = startFlow()
            // Click continue on disclaimer, test memory variables
            signalEvent("Continue")
            assertFlowState("uploadFiles")
        }
    }

    class TestDisclaimerAbort extends CreateBase {
        void performTest() {
            def viewSelection = startFlow()
            // Click cancel on the first step
            clickCancelEndFlow()
        }
    }

    /* Base class for the classes testing upload pipeline. Navigates to the 
     * upload files page, then allows implementing classes to define further
     * behaviour. Includes a method for clicking through the pipeline with
     * supplied files
     * */
    abstract class TestUploadFiles extends CreateBase {
        int modelId=0;

        int getModel() {
            return modelId
        }

        void performTest() {
            def viewSelection = startFlow()
            assertFlowState("displayDisclaimer")
            signalEvent("Continue")
            assertFlowState("uploadFiles")
            performRemainingTest()
        }
        // What the concrete class wants to test
        abstract void performRemainingTest();
        // Click through the upload pipeline with the supplied file
        // and test name/description strings
        void fileUploadPipeline(File file,
                                String format,
                                String mname,
                                String[] descriptionStrings) {
            Map<File,String> additionalFiles=getRandomAdditionalFiles(10)
            addSubmissionFiles([file], additionalFiles)
            signalEvent("Upload")
           /* 	Temporarily disabled states as editing model info is not implemented
           	assertFlowState("displayModelInfo") 
           	signalEvent("Continue")
            */
           
            //Dont add publication info
            assertFlowState("enterPublicationLink")
            //(mockRequest as MockHttpServletRequest).setParameter("PubLinkProvider","PUBMED") Doesnt seem to work :/
            //(mockRequest as MockHttpServletRequest).setParameter("PublicationLink","9486845")
            
            signalEvent("Continue")
            
            assert false == (Boolean) flowScope.
                                        workingMemory.
                                        get("isUpdateOnExistingModel")
            assert format == flowScope.workingMemory.get("model_type") as String
            RTC revision=flowScope.workingMemory.get("RevisionTC") as RTC
            //test name
            assert mname == revision.name
            //test that the description contains known strings
            checkDescription(revision.description, descriptionStrings)
            //add tests for when displayModelInfo does something interesting
            assertFlowState("displaySummaryOfChanges")

            //add tests for when displayModelInfo does something interesting
            signalEvent("Continue")

            assert flowExecutionOutcome.id == "displayConfirmationPage"

            //test that the model is infact saved in the database
            modelId=Integer.parseInt(mockRequest.session.result_submission as String)
            Model model=Model.findById(modelId)
            assert model
            Revision rev=modelService.getLatestRevision(model)
            assert rev
            assert mname == rev.name
            checkDescription(rev.description, descriptionStrings)
            
            //test that the model is saved in the repository
            List<RepositoryFileTransportCommand> files=modelService.retrieveModelFiles(Model.findById(modelId))
            validateFiles(files, [file]+additionalFiles.keySet())
        }
        
        private void checkDescription(String description, String[] descriptionStrings) {
            if (descriptionStrings) {
                descriptionStrings.each {
                    assert description.contains(it)
                }
            }
        }
        
    }

    /* Clicks cancel as soon as you get to the upload files page */
    class TestUploadFilesCancel extends TestUploadFiles {
        void performRemainingTest() {
            clickCancelEndFlow()
        }
    }
    
    class TestUploadFilesContinue extends TestUploadFiles {
        void performRemainingTest() {
            // empty files list shouldnt validate!
            signalEvent("Upload")
            assertFlowState("uploadFiles")
            //random files should validate as unknown
            fileUploadPipeline(getFileForTest("modelfile.xml","hello world"), 
                               "UNKNOWN", 
                               "modelfile",
                               null)
        }
    }

    /* Tests the SBML functionality with the known SBML file */
    class TestSubmitSBML extends TestUploadFiles {
        void performRemainingTest() {
            String[] descriptionTests = new String[4]
            descriptionTests[0]="Verena Becker, Marcel Schilling, Julie Bachmann, Ute Baumann, Andreas Raue, Thomas Maiwald, Jens Timmer and Ursula Klingmüller"
            descriptionTests[1]="This relation solely depends on EpoR turnover independent of ligand binding, suggesting an essential role of large intracellular receptor pools. These receptor properties enable the system to cope with basal and acute demand in the hematopoietic system"
            descriptionTests[2]="%% Default sampling time points"
            descriptionTests[3]="BioModels Database: An enhanced, curated and annotated resource for published quantitative kinetic models"
            fileUploadPipeline(bigModel(), 
                               "SBML", 
                               "Becker2010_EpoR_AuxiliaryModel",
                               descriptionTests)
        }
    }

    class TestSubmitOmex extends TestUploadFiles {
        void performRemainingTest() {
            final File MODEL_FILE = new File("jummp-plugins/jummp-plugin-combine-archive/test/files/sample.omex")
            fileUploadPipeline(MODEL_FILE, "OMEX", "sample", ["sample.omex"] as String[])
        }
    }

    private File getFileForTest(String filename, String text)
    {
        def tmp = System.getProperty("java.io.tmpdir")
        def testFile=new File(tmp + File.separator + filename)
        testFile.setText(text?: "")
        return testFile
    }

    private File bigModel() {
        return new File("test/files/BIOMD0000000272.xml")
    }
}
