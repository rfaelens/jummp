package net.biomodels.jummp.webapp

import grails.plugins.springsecurity.Secured
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import net.biomodels.jummp.core.ModelException
import net.biomodels.jummp.core.model.PublicationTransportCommand
import net.biomodels.jummp.core.model.RepositoryFileTransportCommand as RFTC
import net.biomodels.jummp.core.model.RevisionTransportCommand
import net.biomodels.jummp.webapp.UploadFilesCommand
import org.springframework.web.multipart.MultipartFile

class ModelController {
    /**
     * Flag that checks whether the dynamically-inserted logger is set to DEBUG or higher.
     */
    private final boolean IS_DEBUG_ENABLED = log.isDebugEnabled()
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

    @Secured(["isAuthenticated()"])
    def updateFlow = {
        start {
            action {
                conversation.model_id=params.id
            }
            on("success").to "displayDisclaimer"
        }
        displayDisclaimer {
            on("Continue").to "uploadPipeline"
            on("Cancel").to "abort"
        }
        uploadPipeline {
            subflow(controller: "model", action: "upload", input: [isUpdate: true])
            on("abort").to "abort"
            on("displayConfirmationPage").to "displayConfirmationPage"
            on("displayErrorPage").to "displayErrorPage"
        }
        abort()
        displayConfirmationPage()
        displayErrorPage()
    }
    
    
    @Secured(["isAuthenticated()"])
    def createFlow = {
        displayDisclaimer {
            on("Continue").to "uploadPipeline"
            on("Cancel").to "abort"
        }
        uploadPipeline {
            subflow(controller: "model", action: "upload", input: [isUpdate:false])
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
            isUpdate(required: true)
        }
        start {
            action {
                Map<String, Object> workingMemory=new HashMap<String,Object>()
                workingMemory.put("isUpdateOnExistingModel",flow.isUpdate) //use subflow for updating models, todo
                if (flow.isUpdate) {
                    Long model_id=conversation.model_id as Long
                    workingMemory.put("model_id", model_id)
                    workingMemory.put("LastRevision", modelDelegateService.getLatestRevision(model_id))
                }
                flow.workingMemory=workingMemory
                submissionService.initialise(flow.workingMemory)
            }
            on("success").to "uploadFiles"
        }
        uploadFiles {
            on("Upload") {

                def mainMultipartList = request.getMultiFileMap().mainFile
                def extraFileField = request.getMultiFileMap().extraFile
                List<MultipartFile> extraMultipartList = []
                if (extraFileField instanceof MultipartFile) {
                    extraMultipartList = [extraFileField]
                }else {
                    extraMultipartList = extraFileField
                }
                def descriptionFields = params["description"]
                if (descriptionFields instanceof String) {
                    descriptionFields = [descriptionFields]
                }

                if (IS_DEBUG_ENABLED) {
                    if (mainMultipartList?.size() == 1) {
                        log.debug("New submission started.The main file supplied is ${mainMultipartList.properties}.")
                    } else {
                        log.debug("New submission started. Main files: ${mainMultipartList.inspect()}.")
                    }
                    log.debug("Additional files supplied: ${extraMultipartList.inspect()}.\n")
                }

                def cmd = new UploadFilesCommand()
                bindData(cmd, mainMultipartList, [include: ['mainFile']])
                bindData(cmd, extraMultipartList, [include: ['extraFiles']])
                bindData(cmd, descriptionFields)
                if (IS_DEBUG_ENABLED) {
                    log.debug "Data binding done :${cmd.properties}"
                }
                flow.workingMemory.put("UploadCommand", cmd)
                if (!cmd.validate()) {
                    log.error "Submission did not validate: ${cmd.properties}."
                    log.error "Errors: ${cmd.errors.allErrors.inspect()}."
                    // No main file! This must be an error!
                    flow.workingMemory.put("file_validation_error",true)
                    // Unless there was one all along
                    if (cmd.errors["mainFile"].codes.contains("mainFile.blank")) {
                        if (flow.workingMemory.containsKey("repository_files")) {
                            List<RFTC> uploaded=flow.workingMemory.get("repository_files") as List<RFTC>
                            if (uploaded.find { it.mainFile }) {
                                flow.workingMemory.put("file_validation_error",false)

                                //if there are no main or additional files, remove upload command
                                if (!cmd.extraFiles || cmd.extraFiles.isEmpty()) {
                                    flow.workingMemory.remove("UploadCommand")
                                }
                            }
                        }
                    }
                } else {
                    flow.workingMemory.put("file_validation_error",false)
                    if (IS_DEBUG_ENABLED) {
                        log.debug("The files are valid.")
                    }
                }
                //}
            }.to "transferFilesToService"
            on("ProceedWithoutValidation"){
            }.to "inferModelInfo"
            on("Cancel").to "abort"
            on("Back"){}.to "displayDisclaimer"
        }
        transferFilesToService {
            action {
                    if (flow.workingMemory.remove("file_validation_error") as Boolean) {
                        return GoBackToUploader()
                    }
                    if (flow.workingMemory.containsKey("UploadCommand")) {
                        //should this be in a separate action state?
                        UploadFilesCommand cmd = flow.workingMemory.remove("UploadCommand") as UploadFilesCommand
                        def uuid = UUID.randomUUID().toString()
                        if (IS_DEBUG_ENABLED) {
                            log.debug "Generated submission UUID: ${uuid}"
                        }
                        //pray that exchangeDirectory has been defined
                        File submission_folder=null
                        def sep = File.separator
                        if (!flow.workingMemory.containsKey("repository_files")) {
                          def exchangeDir = grailsApplication.config.jummp.vcs.exchangeDirectory
                          submission_folder = new File(exchangeDir, uuid)
                          submission_folder.mkdirs()
                        }
                        else {
                            RFTC existing=flow.workingMemory.get("repository_files").get(0) as RFTC
                            submission_folder=(new File(existing.path)).getParentFile()
                        }
                        def parent = submission_folder.canonicalPath + sep
                        List<File> mainFileList;
                        if (cmd.mainFile) {
                            mainFileList=transferFiles(parent, cmd.mainFile)
                        }
                        else {
                            mainFileList=new LinkedList<File>()
                        }
                        List<File> extraFileList = transferFiles(parent, cmd.extraFiles)
                        List<String> descriptionList  = cmd.description
                        def additionalsMap = [:]
                        extraFileList.eachWithIndex{ file, i ->
                        additionalsMap[file] = descriptionList[i]
                        }
                        if (IS_DEBUG_ENABLED) {
                            log.debug "About to submit ${mainFileList.inspect()} and ${additionalsMap.inspect()}."
                        }
                        flow.workingMemory["submitted_mains"] = mainFileList
                        flow.workingMemory["submitted_additionals"] = additionalsMap
                        def inputs = new HashMap<String, Object>()
                        submissionService.handleFileUpload(flow.workingMemory,inputs)
                    }
                    PerformValidation()
            }
            on("GoBackToUploader").to "uploadFiles"
            on("PerformValidation").to "performValidation"
        }
        
        performValidation {
            action {
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
            on("Cancel").to "cleanUpAndTerminate"
            on("Back"){}.to "uploadFiles"
        }
        displaySummaryOfChanges {
            on("Continue")
            {
                Map<String,String> modifications=new HashMap<String,String>()
                if (params.RevisionComments) {
                    modifications.put("RevisionComments", params.RevisionComments)
                }
                else {
                    modifications.put("RevisionComments", "Model revised without commit message")
                }
                submissionService.updateRevisionComments(flow.workingMemory, modifications)
            }.to "saveModel"
            on("Cancel").to "cleanUpAndTerminate"
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
        cleanUpAndTerminate {
            action {
                submissionService.cleanup(flow.workingMemory)
            }
            on("success").to "abort"
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

    private List<File> transferFiles(String parent, List multipartFiles) {
        List<File> outcome = []
        multipartFiles.each { f ->
            final String originalFilename = f.getOriginalFilename()
            if (!originalFilename.isEmpty()) {
                final def transferredFile = new File(parent + originalFilename)
                if (IS_DEBUG_ENABLED) {
                    log.debug "Transferring file ${transferredFile}"
                }
                f.transferTo(transferredFile)
                outcome << transferredFile
            }
        }
        outcome
    }
}
