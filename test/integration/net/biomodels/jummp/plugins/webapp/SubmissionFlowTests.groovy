package net.biomodels.jummp.plugins.webapp

import static org.junit.Assert.*
import org.junit.*
import grails.test.WebFlowTestCase
import net.biomodels.jummp.webapp.ModelController

class SubmissionFlowTests {
    
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
    
    /* WebFlowTestCase seems unable to cope with branching/different routes
    in the same test class. Therefore theres multiple classes implementing
    different routes we want to be able to test*/
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
    
    
    
}
