package net.biomodels.jummp.core
import net.biomodels.jummp.core.model.RepositoryFileTransportCommand as RFTC
import net.biomodels.jummp.core.model.ModelFormatTransportCommand as MFTC //rude?
import net.biomodels.jummp.core.model.RevisionTransportCommand
import net.biomodels.jummp.core.ModelException

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
    
    def modelFileFormatService
    def transient sessionFactory

    
    def fileSystemService
    
    abstract class StateMachineStrategy {
        abstract void handleFileUpload(Map<String, Object> workingMemory, Map<String, Object> modifications);
        MFTC inferModelFormatType(Map<String, Object> workingMemory) {
            MFTC format=modelFileFormatService.inferModelFormat(getFilesFromMemory(workingMemory, true))
            if (format) workingMemory.put("model_type",format.identifier)
            else workingMemory.put("model_type", "UNKNOWN")
        }
        abstract void performValidation(Map<String,Object> workingMemory);
        abstract void inferModelInfo(Map<String,Object> workingMemory);
        abstract void refineModelInfo(Map<String,Object> workingMemory, Map<String,Object> modifications);
        abstract void updateRevisionComments(Map<String,Object> workingMemory, Map<String,String> modifications);
        abstract void handleSubmission(Map<String,Object> params);
    }

    class NewModelStateMachine extends StateMachineStrategy {
        void handleFileUpload(Map<String, Object> workingMemory, Map<String, Object> modifications) {}
        void performValidation(Map<String,Object> workingMemory) {
            List<File> modelFiles=getFilesFromMemory(workingMemory, false)
            modelFiles.each {
                if (!it) throw new IOException("Null file passed!")
                if (!it.exists()) throw new IOException("File does not exist!")
                if (it.isDirectory()) throw new IOException("Cannot import directory..yet")
            }
            if (!modelFileFormatService.validate(getFilesFromMemory(workingMemory, true), workingMemory.get("model_type") as String)) {
                throw new ModelException("Model files do not validate!")
            }
        }
        void inferModelInfo(Map<String,Object> workingMemory) {}
        void refineModelInfo(Map<String,Object> workingMemory, Map<String,Object> modifications) {}
        void updateRevisionComments(Map<String,Object> workingMemory, Map<String,String> modifications) {}
        void handleSubmission(Map<String,Object> params) {}
    }
    
    class NewRevisionStateMachine extends StateMachineStrategy {
        /*Include check to remove 'model_type' from memory if main file has been changed*/
        void handleFileUpload(Map<String, Object> workingMemory, Map<String, Object> modifications) {}
        MFTC inferModelFormatType(Map<String, Object> workingMemory) {
            if (workingMemory.containsKey("reprocess_files"))
            {
                MFTC format=super.inferModelFormatType(workingMemory)
                if (workingMemory.containsKey("revisionTC")) {
                    RevisionTransportCommand revision=workingMemory.get("revisionTC") as RevisionTransportCommand
                    revision.format=format
                }
            }
        }
        void performValidation(Map<String,Object> workingMemory) {
            if (workingMemory.containsKey("reprocess_files")) {
                newmodel.performValidation(workingMemory)
            }
        }
        void inferModelInfo(Map<String,Object> workingMemory) {}
        void refineModelInfo(Map<String,Object> workingMemory, Map<String,Object> modifications) {}
        void updateRevisionComments(Map<String,Object> workingMemory, Map<String,String> modifications) {}
        void handleSubmission(Map<String,Object> params) {}
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
        try
        {
        getStrategyFromContext(workingMemory).inferModelFormatType(workingMemory)
        sessionFactory.currentSession.clear() // http://jira.grails.org/browse/GRAILS-3133
        System.out.println(workingMemory)
        }
        catch(Exception e)
        {
            e.printStackTrace()
        }
    }
    
    
    void performValidation(Map<String, Object> workingMemory) {
        /* throws an exception if files are not valid, or do not
         * comprise a valid model */
        try
        {
            System.out.println(workingMemory)
            getStrategyFromContext(workingMemory).performValidation(workingMemory)
            sessionFactory.currentSession.clear() // http://jira.grails.org/browse/GRAILS-3133
            System.out.println(workingMemory)
        }
        catch(Exception e)
        {
            e.printStackTrace()
        }
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

        private StateMachineStrategy getStrategyFromContext(Map<String,Object> workingMemory) {
        Boolean isUpdateOnExistingModel=(Boolean)workingMemory.get("isUpdateOnExistingModel");
        if (isUpdateOnExistingModel) return newrevision
        return newmodel
    }
    
    private List<File> getFilesFromMemory(Map<String, Object> workingMemory, boolean filterMain) {
        List<RFTC> repFiles=getRepFiles(workingMemory)
        if (!repFiles) repFiles=new LinkedList<RFTC>(); //only for testing, remove and throw exception perhaps!
        if (filterMain) repFiles = repFiles.findAll { it.mainFile } //filter out non-main files
        return getFilesFromRepFiles(repFiles)
    }
    
    private List<File> getFilesFromRepFiles(List<RFTC> repFiles) {
        //would be nice to do this in a groovier way
        List<File> list=new LinkedList<File>()
        repFiles.each {
            list.add(new File(fileSystemService.root,it.path))
        }
        return list
    }
    
    private List<RFTC> getRepFiles(Map<String, Object> workingMemory) {
        return (List<RFTC>)workingMemory.get("repository_files")
    }                                                                                                                                                                                                                                                                                                                                                                       


}
