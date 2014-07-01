package net.biomodels.jummp.plugins.webapp

class TestUploadFilesContinue extends TestUploadFiles {
    void performRemainingTest() {
        // empty files list shouldnt validate!
        signalEvent("Upload")
        assertFlowState("uploadFiles")
        //random files should validate as unknown
        fileUploadPipeline(getFileForTest("modelfile.xml","hello world"), "UNKNOWN",
                    "modelfile", null)
    }
}
