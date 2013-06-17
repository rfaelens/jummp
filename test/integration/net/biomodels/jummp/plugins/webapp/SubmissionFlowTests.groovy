package net.biomodels.jummp.plugins.webapp

import static org.junit.Assert.*
import org.junit.*
import grails.test.WebFlowTestCase
import net.biomodels.jummp.webapp.ModelController
import net.biomodels.jummp.core.model.RepositoryFileTransportCommand as RFTC
import net.biomodels.jummp.core.JummpIntegrationTest

class SubmissionFlowTests extends JummpIntegrationTest {
    
    @Test
    void testDisclaimerAbort() {
        new TestDisclaimerAbort().runTest()
    }
    
    @Test
    void testDisclaimerContinue() {
        new TestDisclaimerContinue().runTest()
    }
    
    @Test
    void testUploadFilesAbort() {
        new TestUploadFilesCancel().runTest()
    }
    
    @Test
    void testUploadFilesContinue() {
        new testUploadFilesContinue().runTest()
    }
    
    @Test
    void testSubmitSBML() {
        new TestSubmitSBML().runTest()
    }
    
    
    /* WebFlowTestCase seems unable to cope with branching/different routes
    in the same test class. Therefore theres multiple classes implementing
    the routes we want to be able to test*/
    abstract class FlowUnitTest extends WebFlowTestCase {
        def getFlow() { new ModelController().uploadFlow }
        abstract void performTest()
        void runTest()
        {
            setUp()
            performTest()
            tearDown()
        }
        protected void clickCancelEndFlow() {
            signalEvent("Cancel")
            try {
                def state=flowExecution.activeSession.state.id
                fail("Shouldnt be able to access session state after aborting!")
            }
            catch(Exception happy) {}
        }
    }
    
    class TestDisclaimerContinue extends FlowUnitTest {
        void performTest() {
            def viewSelection = startFlow()
            signalEvent("Continue")
            assert "uploadFiles" == flowExecution.activeSession.state.id
            assert false == (Boolean) flowScope.workingMemory.get("isUpdateOnExistingModel")
        }
    }

    class TestDisclaimerAbort extends FlowUnitTest {
        void performTest() {
            def viewSelection = startFlow()
            clickCancelEndFlow()
        }
    }
    
    abstract class TestUploadFiles extends FlowUnitTest {
        void performTest() {
            def viewSelection = startFlow()
            signalEvent("Continue")
            performRemainingTest()
        }
        abstract void performRemainingTest();
    }

    class TestUploadFilesCancel extends TestUploadFiles {
        void performRemainingTest() {
            clickCancelEndFlow()
        }
    }
    
    class testUploadFilesContinue extends TestUploadFiles {
        void performRemainingTest() {
            // empty files list shouldnt validate!
            signalEvent("Upload")
            assert "uploadFiles" == flowExecution.activeSession.state.id
            //random files should validate as unknown
            flowScope.workingMemory.put("repository_files", getRandomModel())
            signalEvent("Upload")
            assert "displayModelInfo" == flowExecution.activeSession.state.id
            assert "UNKNOWN" == flowScope.workingMemory.get("model_type") as String
        }
    }
    
    class TestSubmitSBML extends TestUploadFiles {
        void performRemainingTest() {
        // valid sbml should proceed
            flowScope.workingMemory.put("repository_files", getSbmlModel())
            signalEvent("Upload")
            assert "displayModelInfo" == flowExecution.activeSession.state.id
            assert "SBML" == flowScope.workingMemory.get("model_type") as String
        }
    }
    
    RFTC createRFTC(File file, boolean isMain) {
        new RFTC(path: file.getCanonicalPath(), mainFile: isMain, userSubmitted: true, hidden: isMain, description:file.getName())
    }
    
    List<RFTC> createRFTCList(File mainFile, List<File> additionalFiles) {
        List<RFTC> returnMe=new LinkedList<RFTC>()
        returnMe.add(createRFTC(mainFile, true))
        additionalFiles.each {
            returnMe.add(createRFTC(it, false))
        }
        returnMe
    }
    
    private List<RFTC> getRandomModel() {
        return createRFTCList(getFileForTest("modelfile.txt","hello world"),[getFileForTest("additional.txt","hello world")])
    }
    
    private List<RFTC> getSbmlModel() {
        return createRFTCList(smallModel("sbmlModel.xml"), [getFileForTest("additionalFile.txt", "heres some randomText")])
    }
    
    private File getFileForTest(String filename, String text)
    {
        File tempFile=File.createTempFile("nothing",null)
        def testFile=new File(tempFile.getParent()+File.separator+filename)
        if (text) testFile.setText(text)
        return testFile
    }
    
    
    private File smallModel(String filename) {

        return getFileForTest(filename, '''<?xml version="1.0" encoding="UTF-8"?>
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
