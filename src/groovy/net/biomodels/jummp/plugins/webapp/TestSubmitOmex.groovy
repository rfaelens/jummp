package net.biomodels.jummp.plugins.webapp


class TestSubmitOmex extends TestUploadFiles {
        void performRemainingTest() {
            final File MODEL_FILE = new File("jummp-plugins/jummp-plugin-combine-archive/test/files/sample archive.omex")
            fileUploadPipeline(MODEL_FILE, "OMEX", "sample archive", ["sample archive.omex"] as String[])
        }
    }

