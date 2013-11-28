package net.biomodels.jummp.plugins.webapp

    class TestDisclaimerContinue extends CreateBase {
        void performTest() {
            def viewSelection = startFlow()
            // Click continue on disclaimer, test memory variables
            signalEvent("Continue")
            assertFlowState("uploadFiles")
        }
    }

