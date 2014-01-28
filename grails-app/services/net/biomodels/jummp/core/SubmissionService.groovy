/**
* Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI),
* Deutsches Krebsforschungszentrum (DKFZ)
*
* This file is part of Jummp.
*
* Jummp is free software; you can redistribute it and/or modify it under the
* terms of the GNU Affero General Public License as published by the Free
* Software Foundation; either version 3 of the License, or (at your option) any
* later version.
*
* Jummp is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
* details.
*
* You should have received a copy of the GNU Affero General Public License along
* with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
*
* Additional permission under GNU Affero GPL version 3 section 7
*
* If you modify Jummp, or any covered work, by linking or combining it with
* Apache Commons, Perf4j (or a modified version of that library), containing parts
* covered by the terms of Apache License v2.0, the licensors of this
* Program grant you additional permission to convey the resulting work.
* {Corresponding Source for a non-source form of such a combination shall
* include the source code for the parts of Apache Commons, Perf4j used as well as
* that of the covered work.}
**/





package net.biomodels.jummp.core

import net.biomodels.jummp.core.ModelException
import net.biomodels.jummp.core.model.ModelFormatTransportCommand as MFTC //rude?
import net.biomodels.jummp.core.model.ModelTransportCommand as MTC
import net.biomodels.jummp.core.model.RepositoryFileTransportCommand as RFTC
import net.biomodels.jummp.core.model.RevisionTransportCommand as RTC
import net.biomodels.jummp.core.model.PublicationTransportCommand
import net.biomodels.jummp.model.PublicationLinkProvider
import net.biomodels.jummp.model.Model
import net.biomodels.jummp.model.ModelFormat
import net.biomodels.jummp.model.Revision
import net.biomodels.jummp.model.RepositoryFile
import org.perf4j.aop.Profiled
import org.apache.commons.io.FilenameUtils
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
    
    /**
     * Dependency Injection of session factory to prevent serialisation of revision
     * domain object. 
     */
    def transient sessionFactory

    /*
     * Abstract state machine strategy, to be extended by the two concrete
     * strategy implementations
     */
    abstract class StateMachineStrategy {

        /**
         * Purpose
         *
         * @param workingMemory     a Map containing all objects exchanged throughout the flow.
         */
        abstract void initialise(Map<String, Object> workingMemory);
        /**
         * Purpose
         *
         * @param workingMemory     a Map containing all objects exchanged throughout the flow.
         * @param modifications     a Map containing the existing files in the model, to be modified
         */
        @Profiled(tag = "submissionService.handleFileUpload")
        void handleFileUpload(Map<String, Object> workingMemory) {
            List<RFTC> tobeAdded;
            List<String> filesToDelete;
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
                tobeAdded=createRFTCList(mainFiles, additionals)
            }
            if (workingMemory.containsKey("deleted_filenames"))
            {
                filesToDelete=workingMemory.remove("deleted_filenames") as List<String>
            }
            storeRFTC(workingMemory, tobeAdded, filesToDelete) 
  
        }
        
        /**
         * Removes deleted files from memory
         *
         * @param workingMemory     a Map containing all objects exchanged throughout the flow.
         */
        @Profiled(tag = "submissionService.NewModelStateMachine.performValidation")
        protected void handleDeletes(Map<String,Object> workingMemory, List<String> filesToDelete) {
        	if (workingMemory.containsKey("repository_files")) {
                List<RFTC> existing=(workingMemory.get("repository_files") as List<RFTC>)
                def toDelete=[]
                existing.each {
                	File file=new File(it.path)
                	filesToDelete.each { deleteMe ->
                		if (file.getName() == deleteMe) {
                			toDelete.add(it)
                		}
                	}
                }
                existing.deleteAll(toDelete)
            }
         }
        

        
        /**
         * Purpose Append supplied RFTC list to those in workingMemory (if any, otherwise create)
         *
         * @param workingMemory     a Map containing all objects exchanged throughout the flow.
         * @param modifications     a Map containing the existing files in the model, to be modified
         */
        @Profiled(tag = "submissionService.storeRFTC")
        protected void storeRFTC(Map<String,Object> workingMemory, 
        						 List<RFTC> tobeAdded,
        						 List<String> filesToDelete) {
            if (workingMemory.containsKey("repository_files")) {
                List<RFTC> existing=(workingMemory.get("repository_files") as List<RFTC>)
                existing.each { oldfile ->
                	String oldname=(new File(oldfile.path)).getName()
                	tobeAdded.each { newfile ->
                		String newname=(new File(newfile.path)).getName()
                		if (newname==oldname) {
                			toDelete.add(oldfile)
                		}
                	}
                	if (filesToDelete) {
                		filesToDelete.each { deleteFile ->
                			String testname=(new File(deleteFile.path)).getName()
                			if (oldname == testname) {
                				toDelete.add(oldfile)
                			}
                		}
                	}
                }
                if (filesToDelete) {
                	handleDeletes(workingMemory, filesToDelete)
                }
                existing.addAll(tobeAdded)
            }
            else {
                workingMemory.put("repository_files", tobeAdded)
            }
        }
        
        /**
         * Detects the format of the model and stores this information in the working memory
         * using the key <tt>model_type</tt>
         *
         * @param workingMemory     a Map containing all objects exchanged throughout the flow.
         */
        @Profiled(tag = "submissionService.inferModelFormatType")
        void inferModelFormatType(Map<String, Object> workingMemory) {
            MFTC format=modelFileFormatService.inferModelFormat(getFilesFromMemory(workingMemory, true))
            if (format) {
                workingMemory.put("model_type",format.identifier)
            }
         }

        /**
         * Perform validation on the model
         *
         * @param workingMemory     a Map containing all objects exchanged throughout the flow.
         */
        abstract void performValidation(Map<String,Object> workingMemory);

        /**
         * Convenience function to store the supplied DOMs in working memory
         *
         * @param workingMemory     a Map containing all objects exchanged throughout the flow.
         */
        @Profiled( tag = "submissionService.storeTCs")
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
        
        protected boolean updatePubs(MTC model, String publinkType, String publink) {
        	boolean refreshPublication=false
        	def linkType=PublicationLinkProvider.LinkType.valueOf(publinkType)
        	if (!model.publication || model.publication.link!=publink 
        		|| model.publication.linkProvider?.linkType!=publinkType) {
        		model.publication=new PublicationTransportCommand()
        		refreshPublication=true;
        	}
        	model.publication.link=publink
        	model.publication.linkProvider=PublicationLinkProvider.createCriteria().get() {
        			eq("linkType",linkType)
        	}.toCommandObject()
        	return refreshPublication
        }

        protected String getModelNameFromFiles(List<File> mainFiles) {
        	StringBuilder name=new StringBuilder()
        	boolean first=true
        	mainFiles.each {
        		if (!first) {
        			name.append(", ")
        		}        		
        		name.append(FilenameUtils.getBaseName(it.name))
        		first=false
        	}
        	return name.toString()
        }

        protected String getModelDescriptionFromFiles(List<File> allFiles) {
        	StringBuilder desc=new StringBuilder("Model comprised of files: ")
        	boolean first=true
        	allFiles.each {
        		if (!first) {
        			desc.append(", ")
        		}        		
        		desc.append(it.name)
        		first=false
        	}
        	return desc.toString()
        }
        

        /**
         * Purpose Convenience function to update the revision dom from the files
         *
         * @param workingMemory     a Map containing all objects exchanged throughout the flow.
         */
        @Profiled(tag = "submissionService.updateRevisionComments")
        protected void updateRevisionFromFiles(Map<String,Object> workingMemory) {
            RTC revision = workingMemory.get("RevisionTC") as RTC
            List<File> files = getFilesFromMemory(workingMemory, true)
            final String formatVersion = revision.format.formatVersion ? revision.format.formatVersion : ""
            ModelFormat modelFormat=ModelFormat.findByIdentifierAndFormatVersion(revision.format.identifier, formatVersion)
            revision.name = modelFileFormatService.extractName(files,modelFormat)
            if (!revision.name) {
            	    revision.name=getModelNameFromFiles(files)
            }
            revision.description=modelFileFormatService.extractDescription(files, modelFormat)
            if (!revision.description) {
            	    revision.description=getModelDescriptionFromFiles(getFilesFromMemory(workingMemory, false))
            }
            revision.validated=workingMemory.get("model_validation_result") as Boolean
        }

        /**
         * Purpose Extract information about the model.
         *
         * @param workingMemory     a Map containing all objects exchanged throughout the flow.
         */
        @Profiled(tag = "submissionService.inferModelInfo")
        void inferModelInfo(Map<String,Object> workingMemory) {
            if (!workingMemory.containsKey("RevisionTC")) {
                createTransportObjects(workingMemory)
            }
            updateRevisionFromFiles(workingMemory)
        }

        /**
         * Purpose Update the name/description in the model datastructures and files
         *
         * @param workingMemory     a Map containing all objects exchanged throughout the flow.
         * @param modifications     a Map containing the user's modifications to the model information we extracted.
         */
        @Profiled(tag = "submissionService.refineModelInfo")
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
         * Purpose Handle changes made at the submission summary. Basically the commit message
         *
         * @param workingMemory     a Map containing all objects exchanged throughout the flow.
         * @param modifications     a Map containing the user's modifications to the model information we extracted.
         */
        protected abstract void handleModificationsToSubmissionInfo(Map<String, Object> workingMemory,
                Map<String,Object> modifications);

                
        void updatePublicationLink(Map<String,Object> workingMemory, Map<String,String> modifications) {
        	if (modifications.containsKey("PubLinkProvider")) {
        		workingMemory.put("RetrievePubDetails", 
        							updatePubs(workingMemory.get("ModelTC") as MTC, 
        									   modifications.get("PubLinkProvider"), 
        									   modifications.get("PubLink")))
        	}
        }
                
        /**
         * Purpose
         *
         * @param workingMemory     a Map containing all objects exchanged throughout the flow.
         * @param modifications     a Map containing the user's modifications to the model information we extracted.
         */
        void updateFromSummary(Map<String,Object> workingMemory, Map<String,String> modifications) {
        	//does nothing in the base class. 
        }

        /**
         * Purpose submit files, remove intermediate files from disk
         *
         * @param workingMemory     a Map containing all objects exchanged throughout the flow.
         */
        @Profiled(tag = "submissionService.handleSubmission")
        void handleSubmission(Map<String,Object> workingMemory) {
            try {
            	completeSubmission(workingMemory)
            	cleanup(workingMemory)
            }
            catch(Exception e) {
                e.printStackTrace()
                throw e
            }
        }
        /*
         * Concrete implementations perform the actual submission
         **/
        protected abstract void completeSubmission(Map<String, Object> workingMemory);

        /**
         * Purpose Remove intermediate files from disk
         *
         * @param workingMemory     a Map containing all objects exchanged throughout the flow.
         */
        @Profiled(tag = "submissionService.cleanup")
        void cleanup(Map<String,Object> workingMemory) {
            try
            {
                List<RFTC> repoFiles = getRepFiles(workingMemory)
                File parent=null
                repoFiles.each {
                    File deleteMe=new File(it.path)
                    if (!parent) {
                         parent=deleteMe.getParentFile()
                    }
                    deleteMe.delete()
                }
                if (parent) {
                	parent.delete()
                }
            }
            catch(Exception e) {
                e.printStackTrace()
            }
        }
        
        /**
         * Purpose
         *
         * @param mainFiles         a List of all the main files associated with the model.
         * @param additionalFiles   a Map comprising any supplementary files and corresponding descriptions
         *                          that are also part of the model that is submitted.
         */
        @Profiled(tag = "submissionService.createRFTCList")
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
         * Purpose Dont need to do anything to initialise
         *
         * @param workingMemory     a Map containing all objects exchanged throughout the flow.
         */
        void initialise(Map<String, Object> workingMemory) {
            //need to do nothing
        }
        
        
        /**
         * Purpose Perform file and model validation, and throw the appropriate exception
         * when necessary
         *
         * @param workingMemory     a Map containing all objects exchanged throughout the flow.
         */
        @Profiled(tag = "submissionService.NewModelStateMachine.performValidation")
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
            workingMemory.put("model_validation_result", modelsAreValid)
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
         * Purpose Create new model and revision transport objects, store in working memory
         *
         * @param workingMemory     a Map containing all objects exchanged throughout the flow.
         */
        @Profiled(tag = "submissionService.NewModelStateMachine.createTransportObjects")
        protected void createTransportObjects(Map<String,Object> workingMemory) {
            MTC model=new MTC() //no need for it currently, later on, store publication details
            RTC revision=new RTC(files: getRepFiles(workingMemory), 
                                model: model,
                                format: ModelFormat.
                                            findByIdentifier(workingMemory.get("model_type") as String).
                                            toCommandObject()) 
            storeTCs(workingMemory, model, revision)
        }

        /**
         * Purpose Handles changes made on the summary screen. Dont need to do anything
         * as currently implemented as there is no option for users to do anything.
         *
         * @param workingMemory     a Map containing all objects exchanged throughout the flow.
         * @param modifications     a Map containing the user's modifications to the model information we extracted.
         */
        void handleModificationsToSubmissionInfo(Map<String, Object> workingMemory, Map<String,Object> modifications) {
            // todo
        }

        /**
         * Purpose Saves the model in the database and repository
         *
         * @param workingMemory     a Map containing all objects exchanged throughout the flow.
         */
        @Profiled(tag = "submissionService.NewModelStateMachine.completeSubmission")
        void completeSubmission(Map<String,Object> workingMemory) {
            List<RFTC> repoFiles = getRepFiles(workingMemory)
            RTC revision=workingMemory.get("RevisionTC") as RTC
            MTC model=revision.model
            model.format=revision.format
            revision.comment="Import of ${revision.name}".toString()
            workingMemory.put("model_id",
                    modelService.uploadValidatedModel(repoFiles, revision).id)
        }
    }

    /**
     * Provides a concrete implementation of the @link{StateMachineStrategy} that is responsible 
     * for handling the submission of updated versions of existing models.
     */
    class NewRevisionStateMachine extends StateMachineStrategy {

        /**
         * Initialises the revision transport command object and the currently
         * existing files associated with the revision in working memory.
         *
         * @param workingMemory     a Map containing all objects exchanged throughout the flow.
         */
        @Profiled(tag = "submissionService.NewRevisionStateMachine.initialise")
        void initialise(Map<String, Object> workingMemory) {
            //fetch files from repository, make RFTCs out of them
            RTC rev=workingMemory.get("LastRevision") as RTC
            List<RFTC> repFiles=rev.getFiles()
            storeRFTC(workingMemory, repFiles, null)
            workingMemory.put("existing_files", repFiles)
            sessionFactory.currentSession.clear()
        }

        
        /**
         * Removes deleted files from memory, checking against previous revision
         * files, marking any existing files to be removed from Vcs
         *
         * @param workingMemory     a Map containing all objects exchanged throughout the flow.
         */
        @Profiled(tag = "submissionService.NewModelStateMachine.performValidation")
        void handleDeletes(Map<String,Object> workingMemory, List<String> filesToDelete) {
        	super.handleDeletes(workingMemory, filesToDelete)
        	if (!workingMemory.containsKey("removeFromVCS")) {
        		workingMemory.put("removeFromVCS", new LinkedList<String>())
        	}
        	def removeFromVcs=workingMemory.get("removeFromVCS")
        	
        	(workingMemory.get("existing_files") as List<RFTC>).each {
        		File file=new File(it.path)
        		filesToDelete.each { deleteMe ->
        			if (file.getName() == deleteMe) {
        				removeFromVcs.add(deleteMe)
        			}
        		}
        	}
         }

        
        /* 
         * If the files include additional files, set parameter in the working memory
         * to ensure that they are reprocessed (validation etc)
         * */
        @Profiled(tag = "submissionService.NewRevisionStateMachine.handleFileUpload")
        void handleFileUpload(Map<String, Object> workingMemory) {
            if (workingMemory.containsKey("submitted_mains")) {
                workingMemory.put("reprocess_files", true)
            }
            super.handleFileUpload(workingMemory)
        }

        /**
         * Detects the format of the model and stores this information in the working memory
         * using the key <tt>model_type</tt>. Only does it if the flag is set to reprocess 
         * the files
         *
         * @param workingMemory     a Map containing all objects exchanged throughout the flow.
         */
        @Profiled(tag = "submissionService.NewRevisionStateMachine.inferModelFormatType")
        void inferModelFormatType(Map<String, Object> workingMemory) {
            if (workingMemory.containsKey("reprocess_files")) {
                super.inferModelFormatType(workingMemory)
            }
        }
        
        /**
         * Performs validation using the key <tt>model_type</tt> to select the
         * file format service. Only does it if the flag is set to reprocess 
         * the files
         *
         * @param workingMemory     a Map containing all objects exchanged throughout the flow.
         */
        @Profiled(tag = "submissionService.NewRevisionStateMachine.performValidation")
        void performValidation(Map<String,Object> workingMemory) {
            if (workingMemory.containsKey("reprocess_files")) {
                newModel.performValidation(workingMemory)
            }
        }

        /**
         * Initialises the Revision object based on the object stored
         * for the last revision and the <tt>model_type</tt> from working memory
         *
         * @param workingMemory     a Map containing all objects exchanged throughout the flow.
         */
        @Profiled(tag = "submissionService.NewRevisionStateMachine.createTransportObjects")
        protected void createTransportObjects(Map<String,Object> workingMemory) {
            RTC revision=workingMemory.get("LastRevision") as RTC
            if (workingMemory.containsKey("reprocess_files")) {
                String formatId = workingMemory["model_type"] as String
                final String formatVersion
                if (formatId != revision.format.identifier) {
                	formatVersion="*"
                }
                else {
                	formatVersion = revision.format.formatVersion ? revision.format.formatVersion : "*"
                }
                revision.format =
                        ModelFormat.findByIdentifierAndFormatVersion(formatId, formatVersion).toCommandObject()
            }
            else {
               workingMemory.put("model_type",revision.format.identifier)
               workingMemory.put("model_validation_result",revision.validated)
            }
            storeTCs(workingMemory, revision.model, revision)
            //ensure that a new revision tc is used for submission, use 
            //this one for copying info!
        }

        /* Handles removal of files from the model. Is called from 
         * handleFileUploads. The revision implementation should remove files from
         * both the working memory and the repository
         * @param workingMemory     a Map containing all objects exchanged throughout the flow.
         * @param modifications     the files to be modified/removed
         */
        void handleModificationsToSubmissionInfo(Map<String, Object> workingMemory, Map<String,Object> modifications) {
            // todo
        }

        /* Updates the revision's comments. New comment is passed through the 
         * modifications map. Kept as map to allow passing other info
         * @param workingMemory     a Map containing all objects exchanged throughout the flow.
         * @param modifications     the revision comments (and any other info to be updated)
         */
        @Profiled(tag = "submissionService.NewRevisionStateMachine.updateRevisionComments")
        void updateFromSummary(Map<String,Object> workingMemory, Map<String,String> modifications) {
            super.updateFromSummary(workingMemory, modifications)
        	RTC revision=workingMemory.get("RevisionTC") as RTC
            revision.comment=modifications.get("RevisionComments")
        }

        /* Submits the revision to modelservice 
         * @param workingMemory     a Map containing all objects exchanged throughout the flow.
         */
        @Profiled(tag = "submissionService.NewRevisionStateMachine.completeSubmission")
        void completeSubmission(Map<String,Object> workingMemory) {
            RTC revision=workingMemory.get("RevisionTC") as RTC
            List<RFTC> repoFiles = getRepFiles(workingMemory)
            
            def newlyCreated= modelService.addValidatedRevision(repoFiles, revision)
            workingMemory.put("model_id", newlyCreated.model.id)
        }
    }

    /**
     * Called by ModelController to initialise working memory
     *
     * @param workingMemory     a Map containing all objects exchanged throughout the flow.
     * @param modifications
     */
    @Profiled(tag = "submissionService.initialise")
    void initialise(Map<String, Object> workingMemory) {
        getStrategyFromContext(workingMemory).initialise(workingMemory)
    }

    
    
    /**
     * Called by ModelController for adding or removing files from the working memory
     *
     * @param workingMemory     a Map containing all objects exchanged throughout the flow.
     * @param modifications
     */
    @Profiled(tag = "submissionService.handleFileUpload")
    void handleFileUpload(Map<String, Object> workingMemory) {
        getStrategyFromContext(workingMemory).handleFileUpload(workingMemory)
    }

    /**
     * Detects the format of the model and stores this information in the working memory
     * using the key <tt>model_type</tt>
     *
     * @param workingMemory     a Map containing all objects exchanged throughout the flow.
     */
    @Profiled(tag = "submissionService.inferModelFormatType")
    void inferModelFormatType(Map<String, Object> workingMemory) {
        getStrategyFromContext(workingMemory).inferModelFormatType(workingMemory)
    }

    /**
     * Performs validation on the supplied model and stores the result in 
     * <tt>model_validation_result</tt>
     *
     * @param workingMemory     a Map containing all objects exchanged throughout the flow.
     */
    @Profiled(tag = "submissionService.performValidation")
    void performValidation(Map<String, Object> workingMemory) throws Exception {
        /*
         * Throws an exception if files are not valid, or do not comprise a valid model
         */
         getStrategyFromContext(workingMemory).performValidation(workingMemory)
    }

    /**
     * Extracts the model's information and creates transport command
     * objects for Revision, stored in <tt>RevisionTC</tt>
     *
     * @param workingMemory     a Map containing all objects exchanged throughout the flow.
     */
    @Profiled(tag = "submissionService.inferModelInfo")
    void inferModelInfo(Map<String, Object> workingMemory) {
        /* create RevisionTC, ModelTC, populate fields */
          getStrategyFromContext(workingMemory).inferModelInfo(workingMemory)
    }

    /**
     * update the working memory with user specified modifications
     * creating separate objects where necessary to ensure that
     * the modifications are performed as separate commits or revisions
     *
     * @param workingMemory     a Map containing all objects exchanged throughout the flow.
     */
    @Profiled(tag = "submissionService.refineModelInfo")
    void refineModelInfo(Map<String, Object> workingMemory, Map<String, Object> modifications) {
        /* 
         */
        getStrategyFromContext(workingMemory).refineModelInfo(workingMemory, modifications)
    }

    /**
     * update the working memory with revision specific comments
     * parameter left as a map<string,string> for forward-compatibility
     *
     * @param workingMemory     a Map containing all objects exchanged throughout the flow.
     */
    @Profiled(tag = "submissionService.updateFromSummary")
    void updateFromSummary(Map<String, Object> workingMemory, Map<String, String> modifications) {
        getStrategyFromContext(workingMemory).updateFromSummary(workingMemory, modifications)
    }
    
    
    /**
     * update the working memory with publication data.
     * parameter left as a map<string,string> for forward-compatibility
     *
     * @param workingMemory     a Map containing all objects exchanged throughout the flow.
     */
    @Profiled(tag = "submissionService.updateFromSummary")
    void updatePublicationLink(Map<String, Object> workingMemory, Map<String, String> modifications) {
        getStrategyFromContext(workingMemory).updatePublicationLink(workingMemory, modifications)
    }
    
    
    /**
     * Purpose Create or update DOM objects as necessary
     *
     * @param workingMemory     a Map containing all objects exchanged througho6ut the flow.
     */
    @Profiled(tag = "submissionService.handleSubmission")
    void handleSubmission(Map<String,Object> workingMemory) {
        getStrategyFromContext(workingMemory).handleSubmission(workingMemory)
    }

    
    /**
     * Purpose: Remove the intermediate files from the disk
     *
     * @param workingMemory     a Map containing all objects exchanged throughout the flow.
     */
    @Profiled(tag = "submissionService.cleanup")
    void cleanup(Map<String,Object> workingMemory) {
        getStrategyFromContext(workingMemory).cleanup(workingMemory)
    }

    
    /**
     * Purpose: Get the appropriate strategy for the flow (update or create)
     *
     * @param workingMemory     a Map containing all objects exchanged throughout the flow.
     */
    private StateMachineStrategy getStrategyFromContext(Map<String,Object> workingMemory) {
        Boolean isUpdateOnExistingModel=(Boolean)workingMemory.get("isUpdateOnExistingModel");
        if (isUpdateOnExistingModel) {
            return newrevision
        }
        return newModel
    }

    /**
     * Purpose: Convenience function to extract files from memory. 
     *
     * @param workingMemory     a Map containing all objects exchanged throughout the flow.
     * @param filterMain  a boolean parameter specifying whether or not to exclude additional files
     */
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

    /**
     * Purpose: Convenience function to convert a list of RFTC to a list of files
     *
     * @param workingMemory     a Map containing all objects exchanged throughout the flow.
     */
    private List<File> getFilesFromRepFiles(List<RFTC> repFiles) {
        //would be nice to do this in a groovier way
        List<File> list=new LinkedList<File>()
        repFiles.each {
            list.add(new File(it.path))
        }
        return list
    }

    /**
     * Purpose: Convenience function to extract repository files from working memory
     *
     * @param workingMemory     a Map containing all objects exchanged throughout the flow.
     */
    private List<RFTC> getRepFiles(Map<String, Object> workingMemory) {
        return (List<RFTC>)workingMemory.get("repository_files")
    }
}
