package net.biomodels.jummp.webapp
import net.biomodels.jummp.core.model.RevisionTransportCommand
import net.biomodels.jummp.core.model.PublicationTransportCommand
import net.biomodels.jummp.core.ModelException
import java.util.zip.ZipOutputStream  
import java.util.zip.ZipEntry  
import java.io.ByteArrayOutputStream

class ModelController {
    /**
     * Dependency injection of modelDelegateService.
     **/
    def modelDelegateService
    /**
     * Dependency injection of sbmlService.
     */
    def sbmlService
    /* 
     * Dependency injection of submissionService
     */
    def submissionService

    def show = {
        [id: params.id]
    }
    /* The flow maintains the 'params' as flow.workingMemory (just to distinguish
     * between request.params and our params. Flow scope does require all objects
     * to be serializable. We need to ensure that our objects fit the bill, else
     * store in session scope! */
    def uploadFlow = {
        displayDisclaimer {
            on("Continue") {
                Map<String, Object> workingMemory=new HashMap<String,Object>()
                flow.workingMemory=workingMemory
                flow.workingMemory.put("isUpdateOnExistingModel",false) //use subflow for updating models, todo
                }.to "uploadFiles"
            on("Cancel").to "abort"
        }
        uploadFiles {
            on("Upload") {
                Map<String,Object> inputs = new HashMap<String, Object>()
                // add files to inputs here as appropriate
                submissionService.handleFileUpload(flow.workingMemory,inputs)
            }.to "performValidation"
            on("ProceedWithoutValidation"){
            }.to "inferModelInfo"
            on("Cancel").to "abort"
        }
        performValidation {
            action {
                try {
                    if (!flow.workingMemory.containsKey("model_type")) {
                        submissionService.inferModelFormatType(flow.workingMemory)
                    }
                    submissionService.performValidation(flow.workingMemory)
                    Valid()
                }
                catch(ModelException giveOption) {
                    ModelNotValid()
                }
                catch(Exception forceCorrection) {
                    FilesNotValid()
                }
            }
            on("Valid"){
                flow.workingMemory.put("Valid", true)
            }.to "inferModelInfo"
            on("ModelNotValid") {
                flow.workingMemory.put("Valid", false)
                // read this parameter to display option to upload without 
                // validation in upload files view
                flash.showProceedWithoutValidationDialog=true 
            }.to "uploadFiles"
            on("FilesNotValid") {
                flash.showFileInvalidError=true;
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
                Map<String,Object> modifications=new HashMap<String,Object>()
                //populate modifications object with form data
                submissionService.refineModelInfo(flow.workingMemory, modifications)
            }.to "displaySummaryOfChanges"
            on("Cancel").to "abort"
        }
        displaySummaryOfChanges {
            on("Continue")
            {
                Map<String,String> modifications=new HashMap<String,String>();
                //populate modifications
                submissionService.updateRevisionComments(flow.workingMemory, modifications)
            }.to "saveModel"
            on("Cancel").to "abort"
        }
        saveModel {
            action {
                submissionService.handleSubmission(flow.workingMemory)
            }
            on("success").to "displayConfirmationPage"
            on("error").to "displayErrorPage"
        }
        displayConfirmationPage()
        displayErrorPage()
        abort()
    }

    
    /**
     * File download of the model file for a model by id
     */
    def download = {
        Map<String, byte[]> bytes = modelDelegateService.retrieveModelFiles(new RevisionTransportCommand(id: params.id as int))
        ByteArrayOutputStream byteBuffer=new ByteArrayOutputStream()
        ZipOutputStream zipFile = new ZipOutputStream()  
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