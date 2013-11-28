package net.biomodels.jummp.plugins.webapp

    
    class TestDisclaimerAbort extends CreateBase {
        void performTest() {
            def viewSelection = startFlow()
            // Click cancel on the first step
            clickCancelEndFlow()
        }
    }

