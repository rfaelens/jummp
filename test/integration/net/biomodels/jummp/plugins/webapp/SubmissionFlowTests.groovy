package net.biomodels.jummp.plugins.webapp

import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.mock.web.MockMultipartHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import static org.junit.Assert.*
import org.junit.*
import grails.test.WebFlowTestCase
import net.biomodels.jummp.webapp.ModelController
import net.biomodels.jummp.core.model.RevisionTransportCommand as RTC
import net.biomodels.jummp.core.JummpIntegrationTest
import org.codehaus.groovy.grails.plugins.testing.GrailsMockMultipartFile
import org.apache.commons.io.FileUtils
import net.biomodels.jummp.model.Model
import net.biomodels.jummp.model.Revision

class SubmissionFlowTests extends JummpIntegrationTest {
    
    def grailsApplication
    def fileSystemService
    def modelService
    
    @Before
    void setUp() {
        super.createUserAndRoles()
        grailsApplication.config.jummp.vcs.exchangeDirectory = "target/vcs/exchange"
        grailsApplication.config.jummp.vcs.workingDirectory = "target/vcs/git"
        File parentLocation = new File(grailsApplication.config.jummp.vcs.workingDirectory )
        parentLocation.mkdir()
        fileSystemService.root = parentLocation
        fileSystemService.currentModelContainer = parentLocation.absolutePath+File.separator+"ttt"
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
        new TestDisclaimerAbort().runTest()
    }

    /* Continues after the disclaimer */
    @Test
    void testDisclaimerContinue() {
        assertNotNull(authenticateAsTestUser())
        new TestDisclaimerContinue().runTest()
    }

    /* Tests upload page, then clicks abort
     */ 
    @Test
    void testUploadFilesAbort() {
        assertNotNull(authenticateAsTestUser())
        new TestUploadFilesCancel().runTest()
    }

    /* Tests upload pipeline, first with an empty list, 
     * then with an unknown model */
    @Test
    void testUploadFilesContinue() {
        assertNotNull(authenticateAsTestUser())
        new testUploadFilesContinue().runTest()
    }

    /* Tests upload pipeline, first with an empty list, 
     * then with a known SBML model */
    @Test
    void testSubmitSBML() {
        assertNotNull(authenticateAsTestUser())
        new TestSubmitSBML().runTest()
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
     */
    abstract class FlowUnitTest extends WebFlowTestCase {
        def getFlow() { 
            new ModelController().createFlow 
        }
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
        void runTest()
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

        /* Adds the supplied file with parameters as a mock multipart file */
        protected void addFileToRequest(File modelFile, String formID, String contentType) {
            final file = new GrailsMockMultipartFile(formID, 
                                                     modelFile.getName(),
                                                     contentType,
                                                     modelFile.getBytes())
            (mockRequest as MockMultipartHttpServletRequest).addFile(file)
        }

    }

    class TestDisclaimerContinue extends FlowUnitTest {
        void performTest() {
            def viewSelection = startFlow()
            // Click continue on disclaimer, test memory variables
            signalEvent("Continue")
            assertFlowState("uploadFiles")
        }
    }

    class TestDisclaimerAbort extends FlowUnitTest {
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
    abstract class TestUploadFiles extends FlowUnitTest {
        void performTest() {
            def viewSelection = startFlow()
            signalEvent("Continue")
            assert false == (Boolean) flowScope.
                                        workingMemory.
                                        get("isUpdateOnExistingModel")
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
            addFileToRequest(file, "mainFile", "application/xml")
            signalEvent("Upload")
            assertFlowState("displayModelInfo")
            assert format == flowScope.workingMemory.get("model_type") as String
            RTC revision=flowScope.workingMemory.get("RevisionTC") as RTC
            //test name
            assert mname == revision.name
            //test that the description contains known strings
            checkDescription(revision.description, descriptionStrings)
            //add tests for when displayModelInfo does something interesting
            signalEvent("Continue")
            assertFlowState("displaySummaryOfChanges")
            
            //add tests for when displayModelInfo does something interesting
            signalEvent("Continue")
            
            assert flowExecutionOutcome.id == "displayConfirmationPage"
            
            //test that the model is infact saved in the database
            int modelId=Integer.parseInt(mockRequest.session.result_submission as String)
            Model model=Model.findById(modelId)
            assert model
            Revision rev=modelService.getLatestRevision(model)
            assert rev
            assert mname == rev.name
            checkDescription(rev.description, descriptionStrings)
            
            //test that the model is saved in the repository
            Map<String, byte[]> files=modelService.retrieveModelFiles(Model.findById(modelId))
            assert files
            byte[] savedFile=files.get(file.getName())
            assert savedFile
            assert savedFile == file.getBytes()
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
    
    class testUploadFilesContinue extends TestUploadFiles {
        void performRemainingTest() {
            // empty files list shouldnt validate!
            signalEvent("Upload")
            assertFlowState("uploadFiles")
            //random files should validate as unknown
            fileUploadPipeline(getFileForTest("modelfile.xml","hello world"), 
                               "UNKNOWN", 
                               "",
                               null)
        }
    }

    /* Tests the SBML functionality with the known SBML file */
    class TestSubmitSBML extends TestUploadFiles {
        void performRemainingTest() {
            String[] descriptionTests = new String[4];
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

    private File getFileForTest(String filename, String text)
    {
        File tempFile=File.createTempFile("nothing",null)
        def testFile=new File(tempFile.getParent()+File.separator+filename)
        if (text) testFile.setText(text)
        return testFile
    }

    private File bigModel() {
        return new File("test/files/BIOMD0000000272.xml")
    }
}
