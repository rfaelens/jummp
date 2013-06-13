package net.biomodels.jummp.core
/* Service that provides model building functionality to
a wizard-style model import/update implemented in the web
app. It is currently kept in core as we may wish to reuse
some of it when we build the curation pipeline. If it is
found to be unsuitable for reuse, please move to the web-app
plugin. 

Raza Ali - 12/6/13
*/
class SubmissionService {

    private NewModelStateMachine newmodel=new NewModelStateMachine();
    private NewRevisionStateMachine newrevision=new NewRevisionStateMachine();
    
    abstract class StateMachineStrategy {
        abstract void handleFileUpload(Map<String, Object> workingMemory, Map<String, Object> modifications);
        abstract void inferModelFormatType(Map<String, Object> workingMemory);
        abstract void performValidation(Map<String,Object> workingMemory);
        abstract void inferModelInfo(Map<String,Object> workingMemory);
        abstract void refineModelInfo(Map<String,Object> workingMemory, Map<String,Object> modifications);
        abstract void updateRevisionComments(Map<String,Object> workingMemory, Map<String,String> modifications);
        abstract void handleSubmission(Map<String,Object> params);
    }

    class NewModelStateMachine extends StateMachineStrategy {
        void handleFileUpload(Map<String, Object> workingMemory, Map<String, Object> modifications) {}
        void inferModelFormatType(Map<String, Object> workingMemory) {}
        void performValidation(Map<String,Object> workingMemory) {}
        void inferModelInfo(Map<String,Object> workingMemory) {}
        void refineModelInfo(Map<String,Object> workingMemory, Map<String,Object> modifications) {}
        void updateRevisionComments(Map<String,Object> workingMemory, Map<String,String> modifications) {}
        void handleSubmission(Map<String,Object> params) {}
    }
    
    class NewRevisionStateMachine extends StateMachineStrategy {
        void handleFileUpload(Map<String, Object> workingMemory, Map<String, Object> modifications) {}
        void inferModelFormatType(Map<String, Object> workingMemory) {}
        void performValidation(Map<String,Object> workingMemory) {}
        void inferModelInfo(Map<String,Object> workingMemory) {}
        void refineModelInfo(Map<String,Object> workingMemory, Map<String,Object> modifications) {}
        void updateRevisionComments(Map<String,Object> workingMemory, Map<String,String> modifications) {}
        void handleSubmission(Map<String,Object> params) {}
    }

    private StateMachineStrategy getStrategyFromContext(Map<String,Object> workingMemory) {
        Boolean isUpdateOnExistingModel=(Boolean)workingMemory.get("isUpdateOnExistingModel");
        if (isUpdateOnExistingModel) return newrevision
        return newmodel
    }
    
    
    /* Called by model controller for adding/removing files from the workingmemory */
    void handleFileUpload(Map<String, Object> workingMemory, Map<String, Object> modifications) {
        /*  The second parameter needs to contain the following:
         *   a - The files to be *modified* - imported or removed
         *   b - Possibly a map from filename to properties including:
         *   whether this file is being added or removed, whether it is
         *   the main file, and an optional description parameter */
        getStrategyFromContext(workingMemory).handleFileUpload(workingMemory, modifications)
    }
    
    void inferModelFormatType(Map<String, Object> workingMemory) {
        /* infers the model type, adds it to the workingmemory
         * needs to store the model type as 'model_type' in the
         * working memory */
        getStrategyFromContext(workingMemory).inferModelFormatType(workingMemory)
    }
    
    
    void performValidation(Map<String, Object> workingMemory) {
        /* throws an exception if files are not valid, or do not
         * comprise a valid model */
        getStrategyFromContext(workingMemory).performValidation(workingMemory)
    }
    
    void inferModelInfo(Map<String, Object> workingMemory) {
        /* create RevisionTC, ModelTC, populate fields */
        getStrategyFromContext(workingMemory).inferModelInfo(workingMemory)
    }
    
    void refineModelInfo(Map<String, Object> workingMemory, Map<String, Object> modifications) {
        /* update the working memory with user specified modifications
         * creating separate objects where necessary to ensure that
         * the modifications are performed as separate commits/revisions */
        getStrategyFromContext(workingMemory).refineModelInfo(workingMemory, modifications)
    }
    
    void updateRevisionComments(Map<String, Object> workingMemory, Map<String, String> modifications) {
        /* update the working memory with revision specific comments
         * parameter left as a map<string,string> for forward-compatibility */
        getStrategyFromContext(workingMemory).updateRevisionComments(workingMemory, modifications)
    }
    
    void handleSubmission(Map<String,Object> workingMemory)
    {
        /*Create or update DOM objects as necessary*/
        getStrategyFromContext(workingMemory).handleSubmission(workingMemory)
    }
}
