package net.biomodels.jummp.plugins.webapp

import static org.junit.Assert.*
import org.junit.*
import grails.test.WebFlowTestCase
import net.biomodels.jummp.webapp.ModelController
import net.biomodels.jummp.core.model.RepositoryFileTransportCommand as RFTC
import net.biomodels.jummp.core.model.RevisionTransportCommand as RTC
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
    the routes we want to be able to test
     *
     *Update: See SetupControllerIntegrationTests.groovy for a solution. Current
     *implementation left in to avoid recoding. The issue with the current version
     *is unnecessary instantiation of flows (just one class could be used). On
     *the other hand it is a little bit more modular than it might otherwise have
     *been. Raza Ali: 18/6/13
     */
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
            RTC revision=flowScope.workingMemory.get("RevisionTC") as RTC
            //test name
            assert "Becker2010_EpoR_AuxiliaryModel" == revision.name
            //test that the very long description contains known strings
            assert revision.description.contains("Verena Becker, Marcel Schilling, Julie Bachmann, Ute Baumann, Andreas Raue, Thomas Maiwald, Jens Timmer and Ursula Klingmüller")
            assert revision.description.contains("This relation solely depends on EpoR turnover independent of ligand binding, suggesting an essential role of large intracellular receptor pools. These receptor properties enable the system to cope with basal and acute demand in the hematopoietic system")
            assert revision.description.contains("%% Default sampling time points")
            assert revision.description.contains("BioModels Database: An enhanced, curated and annotated resource for published quantitative kinetic models")

            //good enough
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
        return createRFTCList(bigModel(), [getFileForTest("additionalFile.txt", "heres some randomText")])
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
