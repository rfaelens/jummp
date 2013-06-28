package net.biomodels.jummp.webapp

import grails.plugins.springsecurity.Secured
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import net.biomodels.jummp.core.ModelException
import net.biomodels.jummp.core.model.PublicationTransportCommand
import net.biomodels.jummp.core.model.RepositoryFileTransportCommand as RFTC
import net.biomodels.jummp.core.model.RevisionTransportCommand

class ModelController {
    /**
     * Dependency injection of modelDelegateService.
     **/
    def modelDelegateService
    /**
     * Dependency injection of sbmlService.
     */
    def sbmlService
    /**
     * Dependency injection of submissionService
     */
    def submissionService
    /**
     * Dependency Injection of grailsApplication
     */
    def grailsApplication

    def show = {
        [id: params.id]
    }

    def updateFlow = {
        displayDisclaimer {
            on("Continue").to "uploadPipeline"
            on("Cancel").to "abort"
        }
        uploadPipeline {
            Map<String, Object> workingMemory=new HashMap<String,Object>()
            workingMemory.put("isUpdateOnExistingModel",true) //use subflow for updating models, todo
            workingMemory.put("model_revised", params.id)
            
            subflow(controller: "model", action: "upload", input: [workingMemory: workingMemory])
            on("abort").to "abort"
            on("displayConfirmationPage").to "displayConfirmationPage"
            on("displayErrorPage").to "displayErrorPage"
        }
        abort()
        displayConfirmationPage()
        displayErrorPage()
    }
    
    
    def createFlow = {
        displayDisclaimer {
            on("Continue").to "uploadPipeline"
            on("Cancel").to "abort"
        }
        uploadPipeline {
            Map<String, Object> workingMemory=new HashMap<String,Object>()
            workingMemory=workingMemory
            workingMemory.put("isUpdateOnExistingModel",false) //use subflow for updating models, todo
          
            subflow(controller: "model", action: "upload", input: [workingMemory: workingMemory])
            on("abort").to "abort"
            on("displayConfirmationPage").to "displayConfirmationPage"
            on("displayErrorPage").to "displayErrorPage"
        }
        abort()
        displayConfirmationPage()
        displayErrorPage()
    }
    
    
    /*
     * The flow maintains the 'params' as flow.workingMemory (just to distinguish
     * between request.params and our params. Flow scope requires all objects
     * to be serializable. If this is not possible, there are two solutions:
     *   1) store in session scope 
     *   2) evict the objects in question from the Hibernate session before the
     * end of the session using <tt>flow.persistenceContext.evict(it)</tt>.
     * See http://grails.org/grails/latest/doc/guide/theWebLayer.html#flowScopes
     */
    @Secured(["isAuthenticated()"])
    def uploadFlow = {
        input {
            workingMemory(required: true)
        }
        uploadFiles {
            on("Upload") {
                //withForm {
                def inputs = new HashMap<String, Object>()

                def mainFile = request.getFile('mainFile')
                if (!mainFile || mainFile.empty) {
                    flash.message = "Please select a main file"
                    return error()
                }

                def uuid = UUID.randomUUID().toString()
                //pray that exchangeDirectory has been defined
                def exchangeDir =
                        grailsApplication.config.jummp.vcs.exchangeDirectory
                def sep = File.separator
                def submission_folder = new File(exchangeDir + sep + uuid)
                submission_folder.mkdirs()
                def filePath = submission_folder.canonicalPath + 
                                sep + mainFile.getOriginalFilename()
                def transferredFile = new File(filePath)
                transferredFile.append(mainFile.bytes)
                //mainFile.transferTo(transferredFile)
                //do something with request.getFileMap(), but what?
                def mains = [transferredFile]
                def additionals = [:]
                flow.workingMemory["submitted_mains"] = mains
                flow.workingMemory["submitted_additionals"] = additionals
                // add files to inputs here as appropriate
                submissionService.handleFileUpload(flow.workingMemory,inputs)
                //}
            }.to "performValidation"
            on("ProceedWithoutValidation"){
            }.to "inferModelInfo"
            on("Cancel").to "abort"
            on("Back"){}.to "displayDisclaimer"
        }

        performValidation {
            action {
                //temporarily add an sbml model to allow execution to proceed
                //flow.workingMemory.put("repository_files", getSbmlModel())
                if (!flow.workingMemory.containsKey("model_type")) {
                    submissionService.inferModelFormatType(flow.workingMemory)
                }
                submissionService.performValidation(flow.workingMemory)
                if (!flow.workingMemory.containsKey("validation_error")) {
                    Valid()
                }
                else
                {
                    String errorAsString=flow.workingMemory.remove("validation_error") as String
                    if (errorAsString.contains("ModelValidationError")) {
                        ModelNotValid()
                    }
                    else {
                        FilesNotValid()
                    }
                }
            }
            on("Valid"){
                flow.workingMemory.put("Valid", true)
            }.to "inferModelInfo"
            on("ModelNotValid") {
                flow.workingMemory.put("Valid", false)
                // read this parameter to display option to upload without
                // validation in upload files view
                flash.showProceedWithoutValidationDialog = true
            }.to "uploadFiles"
            on("FilesNotValid") {
                flash.showFileInvalidError = true
            }.to "uploadFiles"
        }
        inferModelInfo {
            action {
                submissionService.inferModelInfo(flow.workingMemory)
            }
            on("success").to "displayModelInfo"
        }
        displayModelInfo {
            on("Continue") {
                //populate modifications object with form data
                Map<String,Object> modifications = new HashMap<String,Object>()
                modifications.put("new_name", params.name)
                modifications.put("new_description", params.description)
                submissionService.refineModelInfo(flow.workingMemory, modifications)
            }.to "displaySummaryOfChanges"
            on("Cancel").to "abort"
            on("Back"){}.to "uploadFiles"
        }
        displaySummaryOfChanges {
            on("Continue")
            {
                Map<String,String> modifications=new HashMap<String,String>()
                modifications.put("RevisionComments", params.RevisionComments)
                //populate modifications
                submissionService.updateRevisionComments(flow.workingMemory, modifications)
            }.to "saveModel"
            on("Cancel").to "abort"
            on("Back"){}.to "displayModelInfo"
        }
        saveModel {
            action {
                try {
                    submissionService.handleSubmission(flow.workingMemory)
                    session.result_submission=flow.workingMemory.get("model_id")
                }
                catch(Exception ignore) {
                    error()
                }
            }
            on("success").to "displayConfirmationPage"
            on("error").to "displayErrorPage"
        }
        abort()
        displayConfirmationPage()
        displayErrorPage()
    }

    /**
     * File download of the model file for a model by id
     */
    def download = {
        try
        {
            Map<String, byte[]> bytes = modelDelegateService.retrieveModelFiles(new RevisionTransportCommand(id: params.id as int))
            ByteArrayOutputStream byteBuffer=new ByteArrayOutputStream()
            ZipOutputStream zipFile = new ZipOutputStream(byteBuffer)
            for (Map.Entry<String, byte[]> entry : bytes.entrySet())
            {
                zipFile.putNextEntry(new ZipEntry(entry.getKey()))
                zipFile.write(entry.getValue(),0,entry.getValue().length)
                zipFile.closeEntry()
            }
            zipFile.close()

            response.setContentType("application/zip")
            // TODO: set a proper name for the model
            response.setHeader("Content-disposition", "attachment;filename=\"model.zip\"")
            response.outputStream << new ByteArrayInputStream(byteBuffer.toByteArray())
        }
        catch(Exception e)
        {
            e.printStackTrace()
        }
    }

    def model = {
        RevisionTransportCommand rev = modelDelegateService.getLatestRevision(params.id as Long)
        List<String> authors = []
        rev.model.publication?.authors.each {
            authors.add("${it.firstName} ${it.lastName}, ")
        }
        if(!authors.empty) {
            String auth = authors.get(authors.size() - 1)
            authors.remove(authors.get(authors.size() - 1))
            authors.add(authors.size(), auth.substring(0, auth.length() - 2))
        }
        [revision: rev, authors: authors]
    }

    /**
     * Display basic information about the model
     */
    def summary = {
        RevisionTransportCommand rev = modelDelegateService.getLatestRevision(params.id as Long)
        [publication: modelDelegateService.getPublication(params.id as Long), revision: rev, notes: sbmlService.getNotes(rev), annotations: sbmlService.getAnnotations(rev)]
    }

    def overview = {
        RevisionTransportCommand rev = modelDelegateService.getLatestRevision(params.id as Long)
        [
            reactions: sbmlService.getReactions(rev),
            rules: sbmlService.getRules(rev),
            parameters: sbmlService.getParameters(rev),
            compartments: sbmlService.getCompartments(rev)
        ]
    }

    /**
     * Renders html snippet with Publication information for the current Model identified by the id.
     */
    def publication = {
        PublicationTransportCommand publication = modelDelegateService.getPublication(params.id as Long)
        [publication: publication]
    }

    def notes = {
        RevisionTransportCommand rev = modelDelegateService.getLatestRevision(params.id as Long)
        [notes: sbmlService.getNotes(rev)]
    }

    /**
     * Retrieve annotations and hand them over to the view
     */
    def annotations = {
        RevisionTransportCommand rev = modelDelegateService.getLatestRevision(params.id as Long)
        [annotations: sbmlService.getAnnotations(rev)]
    }

    /**
     * File download of the model file for a model by id
     */
    def downloadModelRevision = {
        RevisionTransportCommand rev = modelDelegateService.getLatestRevision(params.id as Long)
        byte[] bytes = modelDelegateService.retrieveModelFiles(rev)
        response.setContentType("application/xml")
        // TODO: set a proper name for the model
        response.setHeader("Content-disposition", "attachment;filename=\"model.xml\"")
        response.outputStream << new ByteArrayInputStream(bytes)
    }
}
