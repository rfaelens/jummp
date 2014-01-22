package net.biomodels.jummp.plugins.webapp
import net.biomodels.jummp.core.model.ModelFormatTransportCommand
import net.biomodels.jummp.core.model.ModelTransportCommand
import net.biomodels.jummp.core.model.RepositoryFileTransportCommand
import net.biomodels.jummp.core.model.RevisionTransportCommand as RTC
import net.biomodels.jummp.model.Model
import net.biomodels.jummp.model.Revision
import grails.util.Holders

    /* Base class for the classes testing upload pipeline. Navigates to the 
     * upload files page, then allows implementing classes to define further
     * behaviour. Includes a method for clicking through the pipeline with
     * supplied files
     * */
    abstract class TestUploadFiles extends CreateBase {
        int modelId=0;
        def modelService=Holders.applicationContext.getBean("modelService")
        
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
