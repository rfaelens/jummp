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

    /* Called by model controller for adding/removing files from the workingmemory */
    void handleFileUpload(Map<String, Object> workingMemory, Map<String, Object> modifications) {
        /*  The second parameter needs to contain the following:
         *   a - The files to be *modified* - imported or removed
         *   b - Possibly a map from filename to properties including:
         *   whether this file is being added or removed, whether it is
         *   the main file, and an optional description parameter */
    }
    
    void inferModelFormatType(Map<String, Object> workingMemory) {
        /* infers the model type, adds it to the workingmemory */
    }
    
    
    void performValidation(Map<String, Object> workingMemory) {
        /* throws an exception if files are not valid, or do not
         * comprise a valid model */
    }
    
    void inferModelInfo(Map<String, Object> workingMemory) {
        /* create RevisionTC, ModelTC, populate fields */
    }
    
    void refineModelInfo(Map<String, Object> workingMemory, Map<String, Object> modifications) {
        /* update the working memory with user specified modifications
         * creating separate objects where necessary to ensure that
         * the modifications are performed as separate commits/revisions */
    }
    
    void updateRevisionComments(Map<String, Object> workingMemory, Map<String, String> inputs) {
        /* update the working memory with revision specific comments
         * parameter left as a map<string,string> for forward-compatibility */
    }
    
    void handleSubmission(Map<String,Object> params)
    {
        /*Create or update DOM objects as necessary*/
    }
}
