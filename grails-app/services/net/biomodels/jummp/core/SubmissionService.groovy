package net.biomodels.jummp.core

import net.biomodels.jummp.core.ModelException
import net.biomodels.jummp.core.model.ModelFormatTransportCommand as MFTC //rude?
import net.biomodels.jummp.core.model.ModelTransportCommand as MTC
import net.biomodels.jummp.core.model.RepositoryFileTransportCommand as RFTC
import net.biomodels.jummp.core.model.RevisionTransportCommand as RTC
import net.biomodels.jummp.model.Model
import net.biomodels.jummp.model.ModelFormat

/**
 * Service that provides model building functionality to a wizard-style model
 * import or update implemented in the web app. It is currently kept in core as
 * we may wish to reuse some of it when we build the curation pipeline. If it is
 * found to be unsuitable for reuse, please move to the web-app plugin.
 *
 * @author Raza Ali <raza.ali@ebi.ac.uk>
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 * @author 20130625
*/
class SubmissionService {
    // concrete strategies for the submission state machine
    private final NewModelStateMachine newModel = new NewModelStateMachine()
    private final NewRevisionStateMachine newrevision = new NewRevisionStateMachine()
    /**
     * Dependency Injection of ModelFileFormatService
     */
    def modelFileFormatService
    /**
     * Dependency Injection of ModelService
     */
    def modelService

    /*
     * Abstract state machine strategy, to be extended by the two concrete
     * strategy implementations
     */
    abstract class StateMachineStrategy {

        /**
         * Purpose
         *
         * @param workingMemory     a Map containing all objects exchanged throughout the flow.
         * @param modifications
         */
        abstract void handleFileUpload(Map<String, Object> workingMemory, Map<String, Object> modifications);

        /**
         * Detects the format of the model and stores this information in the working memory
         * using the key <tt>model_type</tt>
         *
         * @param workingMemory     a Map containing all objects exchanged throughout the flow.
         */
        void inferModelFormatType(Map<String, Object> workingMemory) {
            MFTC format=modelFileFormatService.inferModelFormat(getFilesFromMemory(workingMemory, true))
            if (format) {
                workingMemory.put("model_type",format.identifier)
            }
         }

        /**
         * Purpose
         *
         * @param workingMemory     a Map containing all objects exchanged throughout the flow.
         */
        abstract void performValidation(Map<String,Object> workingMemory);

        /**
         * Related functions for inferModelInfo, following template method pattern
         *
         * @param workingMemory     a Map containing all objects exchanged throughout the flow.
         * @param model             a @link{net.biomodels.jummp.core.model.ModelTransportCommand} 
         *                          representing the model.
         * @param revision          a @link{net.biomodels.jummp.core.model.RevisionTransportCommand}
         *                          representing the revision.
         */
        protected void storeTCs(Map<String,Object> workingMemory, MTC model, RTC revision) {
             workingMemory.put("ModelTC", model)
             workingMemory.put("RevisionTC", revision)
        }

        /**
         * Purpose
         *
         * @param workingMemory     a Map containing all objects exchanged throughout the flow.
         */
        protected abstract void createTransportObjects(Map<String,Object> workingMemory);

        /**
         * Purpose
         *
         * @param workingMemory     a Map containing all objects exchanged throughout the flow.
         */
        protected void updateRevisionFromFiles(Map<String,Object> workingMemory) {
            RTC revision = workingMemory.get("RevisionTC") as RTC
            List<File> files = getFilesFromMemory(workingMemory, true)
            revision.name = modelFileFormatService.extractName(files,
                    ModelFormat.findByIdentifier(workingMemory.get("model_type") as String))
            revision.description=modelFileFormatService.extractDescription(files,
                    ModelFormat.findByIdentifier(workingMemory.get("model_type") as String))
        }

        /**
         * Purpose
         *
         * @param workingMemory     a Map containing all objects exchanged throughout the flow.
         */
        void inferModelInfo(Map<String,Object> workingMemory) {
            if (!workingMemory.containsKey("RevisionTC")) {
                createTransportObjects(workingMemory)
            }
            updateRevisionFromFiles(workingMemory)
        }

        /**
         * Purpose
         *
         * @param workingMemory     a Map containing all objects exchanged throughout the flow.
         * @param modifications     a Map containing the user's modifications to the model information we extracted.
         */
        void refineModelInfo(Map<String,Object> workingMemory, Map<String,Object> modifications) {
            RTC revision=workingMemory.get("RevisionTC") as RTC
            if (revision.name == modifications.get("new_name") as String) {
                if (revision.description == modifications.get("new_description") as String) {
                    return
                }
            }
            handleModificationsToSubmissionInfo(workingMemory, modifications)
        }

        /**
         * Purpose
         *
         * @param workingMemory     a Map containing all objects exchanged throughout the flow.
         * @param modifications     a Map containing the user's modifications to the model information we extracted.
         */
        protected abstract void handleModificationsToSubmissionInfo(Map<String, Object> workingMemory,
                Map<String,Object> modifications);

        /**
         * Purpose
         *
         * @param workingMemory     a Map containing all objects exchanged throughout the flow.
         * @param modifications     a Map containing the user's modifications to the model information we extracted.
         */
        abstract void updateRevisionComments(Map<String,Object> workingMemory, Map<String,String> modifications);

        /**
         * Purpose
         *
         * @param workingMemory     a Map containing all objects exchanged throughout the flow.
         */
        abstract void handleSubmission(Map<String,Object> workingMemory);

        /**
         * Purpose
         *
         * @param mainFiles         a List of all the main files associated with the model.
         * @param additionalFiles   a Map comprising any supplementary files and corresponding descriptions
         *                          that are also part of the model that is submitted.
         */
        protected List<RFTC> createRFTCList(List<File> mainFiles, Map<File,String> additionalFiles) {
            List<RFTC> returnMe=new LinkedList<RFTC>()
            mainFiles.each {
                returnMe.add(createRFTC(it, true,""))
            }
            additionalFiles.keySet().each {
                returnMe.add(createRFTC(it, false, additionalFiles.get(it)))
            }
            returnMe
        }

        /*
         * Convenience method for creating 
         * @link{net.biomodels.jummp.core.model.RepositoryFileTransportCommand} objects
         */
        private RFTC createRFTC(File file, boolean isMain, String description) {
            new RFTC(path: file.getCanonicalPath(), mainFile: isMain, userSubmitted: true,
                    hidden: false, description:description)
        }
    }

    /**
     * Provides a concrete implementation of the @link{StateMachineStrategy} that is responsible 
     * for handling the submission of new models to JUMMP.
     */
    class NewModelStateMachine extends StateMachineStrategy {

        /**
         * Purpose
         *
         * @param workingMemory     a Map containing all objects exchanged throughout the flow.
         * @param modifications     a Map describing the list of existing files that should be added or removed.
         */
        void handleFileUpload(Map<String, Object> workingMemory, Map<String, Object> modifications) {
            if (workingMemory.containsKey("submitted_mains"))
            {
                List<File> mainFiles=workingMemory.remove("submitted_mains") as List<File>
                Map<File,String> additionals=null;
                if (workingMemory.containsKey("submitted_additionals")) {
                    additionals=workingMemory.remove("submitted_additionals") as Map<File, String>
                }
                else {
                    additionals=new HashMap<File,String>()
                }
                workingMemory.put("repository_files", createRFTCList(mainFiles, additionals))
            }
        }

        /**
         * Purpose
         *
         * @param workingMemory     a Map containing all objects exchanged throughout the flow.
         */
        void performValidation(Map<String,Object> workingMemory) {
            List<File> modelFiles=getFilesFromMemory(workingMemory, false)
            modelFiles.each {
                if (!it) {
                    workingMemory.put("validation_error", "Null file passed!")
                }
                if (!it.exists()) {
                    workingMemory.put("validation_error", "File does not exist")
                }
                if (it.isDirectory()) {
                    workingMemory.put("validation_error", "Directory passed as input")
                }
            }
            boolean modelsAreValid = modelFileFormatService.validate(
                        getFilesFromMemory(workingMemory, true),
                        workingMemory.get("model_type") as String)
            if (!workingMemory.containsKey("model_type")) {
                workingMemory.put("validation_error",
                    "Missing Format Error: Validation could not be performed, format unknown")
            }
            else if (!modelsAreValid) {
                //TODO be more specific to the user about what went wrong.
                workingMemory.put("validation_error", "ModelValidationError")
            }
        }

        /**
         * Purpose
         *
         * @param workingMemory     a Map containing all objects exchanged throughout the flow.
         */
        protected void createTransportObjects(Map<String,Object> workingMemory) {
            MTC model=new MTC() //no need for it currently, later on, store publication details
            RTC revision=new RTC(files: getRepFiles(workingMemory), model: model) 
            storeTCs(workingMemory, model, revision)
        }

        /**
         * Purpose
         *
         * @param workingMemory     a Map containing all objects exchanged throughout the flow.
         * @param modifications     a Map containing the user's modifications to the model information we extracted.
         */
        void handleModificationsToSubmissionInfo(Map<String, Object> workingMemory, Map<String,Object> modifications) {
            // todo
        }

        /**
         * Implementation of @link{StateMachineStrategy#updateRevisionComments(Map workingMemory, Map modifications)}
         * This method is final so that subclasses can not alter its behaviour.
         *
         * @param workingMemory     a Map containing all objects exchanged throughout the flow.
         * @param modifications     a Map containing the user's modifications to the model information we extracted.
         */
        final void updateRevisionComments(Map<String,Object> workingMemory, Map<String,String> modifications) {
            // does nothing
        }

        /**
         * Purpose
         *
         * @param workingMemory     a Map containing all objects exchanged throughout the flow.
         */
        void handleSubmission(Map<String,Object> workingMemory) {
            List<RFTC> repoFiles = getRepFiles(workingMemory)
            MTC model=(MTC) workingMemory.get("ModelTC") as MTC
            RTC revision=workingMemory.get("RevisionTC") as RTC

            model.name=revision.name
            model.format=ModelFormat.findByIdentifier(workingMemory.get("model_type") as String).toCommandObject()
            model.comment="Import of ${revision.name}".toString()
            workingMemory.put("model_id",
                modelService.uploadModelAsList(repoFiles, model).id)
        }
    }

    /**
     * Provides a concrete implementation of the @link{StateMachineStrategy} that is responsible 
     * for handling the submission of updated versions of existing models.
     */
    class NewRevisionStateMachine extends StateMachineStrategy {
        void handleFileUpload(Map<String, Object> workingMemory, Map<String, Object> modifications) {
        /*Include check to remove 'model_type' from memory if main file has been changed*/

        }

        /**
         * Detects the format of the model and stores this information in the working memory
         * using the key <tt>model_type</tt>
         *
         * @param workingMemory     a Map containing all objects exchanged throughout the flow.
         */
        void inferModelFormatType(Map<String, Object> workingMemory) {
            if (workingMemory.containsKey("reprocess_files")) {
                super.inferModelFormatType(workingMemory)
            }
        }
        void performValidation(Map<String,Object> workingMemory) {
            if (workingMemory.containsKey("reprocess_files")) {
                newModel.performValidation(workingMemory)
            }
        }

        protected void createTransportObjects(Map<String,Object> workingMemory) {
            Model modelDom=Model.get(workingMemory.get("model_id") as Long)
            MTC model=modelDom.toCommandObject()
            RTC revision=modelService.getLatestRevision(model)
            storeTCs(workingMemory, model, revision)
            //ensure that a new revision tc is used for submission, use 
            //this one for copying info!
        }

        void handleModificationsToSubmissionInfo(Map<String, Object> workingMemory, Map<String,Object> modifications) {
            // todo
        }

        void updateRevisionComments(Map<String,Object> workingMemory, Map<String,String> modifications) {
            RTC revision=workingMemory.get("RevisionTC") as RTC
            revision.comment=workingMemory.get("RevisionComments") as String
        }

        void handleSubmission(Map<String,Object> workingMemory) {
            //todo
        }
    }


    /**
     * Called by ModelController for adding or removing files from the working memory
     *
     * @param workingMemory     a Map containing all objects exchanged throughout the flow.
     * @param modifications
     */
    void handleFileUpload(Map<String, Object> workingMemory, Map<String, Object> modifications) {
        /*
         * The second parameter needs to contain the following:
         *   a - The files to be *modified* - imported or removed
         *   b - Possibly a map from filename to properties including:
         *   whether this file is being added or removed, whether it is
         *   the main file, and an optional description parameter
         */
        getStrategyFromContext(workingMemory).handleFileUpload(workingMemory, modifications)
    }

    /**
     * Detects the format of the model and stores this information in the working memory
     * using the key <tt>model_type</tt>
     *
     * @param workingMemory     a Map containing all objects exchanged throughout the flow.
     */
    void inferModelFormatType(Map<String, Object> workingMemory) {
        getStrategyFromContext(workingMemory).inferModelFormatType(workingMemory)
    }

    void performValidation(Map<String, Object> workingMemory) throws Exception {
        /*
         * Throws an exception if files are not valid, or do not comprise a valid model
         */
         getStrategyFromContext(workingMemory).performValidation(workingMemory)
    }

    void inferModelInfo(Map<String, Object> workingMemory) {
        /* create RevisionTC, ModelTC, populate fields */
          getStrategyFromContext(workingMemory).inferModelInfo(workingMemory)
    }

    void refineModelInfo(Map<String, Object> workingMemory, Map<String, Object> modifications) {
        /* 
         * update the working memory with user specified modifications
         * creating separate objects where necessary to ensure that
         * the modifications are performed as separate commits or revisions
         */
        getStrategyFromContext(workingMemory).refineModelInfo(workingMemory, modifications)
    }

    void updateRevisionComments(Map<String, Object> workingMemory, Map<String, String> modifications) {
        /*
         * update the working memory with revision specific comments
         * parameter left as a map<string,string> for forward-compatibility
         */
        getStrategyFromContext(workingMemory).updateRevisionComments(workingMemory, modifications)
    }

    /**
     * Purpose
     *
     * @param workingMemory     a Map containing all objects exchanged throughout the flow.
     */
    void handleSubmission(Map<String,Object> workingMemory) {
        /*Create or update DOM objects as necessary*/
        getStrategyFromContext(workingMemory).handleSubmission(workingMemory)
    }

    private StateMachineStrategy getStrategyFromContext(Map<String,Object> workingMemory) {
        Boolean isUpdateOnExistingModel=(Boolean)workingMemory.get("isUpdateOnExistingModel");
        if (isUpdateOnExistingModel) {
            return newrevision
        }
        return newModel
    }

    private List<File> getFilesFromMemory(Map<String, Object> workingMemory, boolean filterMain) {
        List<RFTC> repFiles=getRepFiles(workingMemory)
        if (!repFiles) {
            repFiles=new LinkedList<RFTC>(); //only for testing, remove and throw exception perhaps!
        }
        if (filterMain) {
            repFiles = repFiles.findAll { it.mainFile } //filter out non-main files
        }
        return getFilesFromRepFiles(repFiles)
    }

    private List<File> getFilesFromRepFiles(List<RFTC> repFiles) {
        //would be nice to do this in a groovier way
        List<File> list=new LinkedList<File>()
        repFiles.each {
            list.add(new File(it.path))
        }
        return list
    }

    private List<RFTC> getRepFiles(Map<String, Object> workingMemory) {
        return (List<RFTC>)workingMemory.get("repository_files")
    }
}
