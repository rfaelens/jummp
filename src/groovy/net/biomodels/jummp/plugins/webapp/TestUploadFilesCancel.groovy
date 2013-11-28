package net.biomodels.jummp.plugins.webapp


    /* Clicks cancel as soon as you get to the upload files page */
    class TestUploadFilesCancel extends TestUploadFiles {
        void performRemainingTest() {
            clickCancelEndFlow()
        }
    }
    
