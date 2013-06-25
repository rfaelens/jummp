package net.biomodels.jummp.webapp

import grails.plugins.springsecurity.Secured
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import net.biomodels.jummp.core.ModelException
import net.biomodels.jummp.core.model.PublicationTransportCommand
import net.biomodels.jummp.core.model.RepositoryFileTransportCommand as RFTC
import net.biomodels.jummp.core.model.RevisionTransportCommand
import net.biomodels.jummp.webapp.UploadFilesCommand

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

    def show = {
        [id: params.id]
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
        displayDisclaimer {
            on("Continue") {
                Map<String, Object> workingMemory=new HashMap<String,Object>()
                flow.workingMemory=workingMemory
                flow.workingMemory.put("isUpdateOnExistingModel",false) //use subflow for updating models, todo
            }.to "uploadFiles"
            on("Cancel").to "abort"
        }
        uploadFiles {
            on("Upload") { UploadFilesCommand cmd ->
                if (cmd.hasErrors()) {
                    return error()
                }
                else {
                    Map<String, Object> inputs = new HashMap<String, Object>()
                    def mainFile = request.getFile('mainFile')
                    // add files to inputs here as appropriate
                    submissionService.handleFileUpload(flow.workingMemory,inputs)
                }
            }.to "performValidation"
            on("ProceedWithoutValidation"){
            }.to "inferModelInfo"
            on("Cancel").to "abort"
            on("Back"){}.to "displayDisclaimer"
        }
        performValidation {
            action {
                //temporarily add an sbml model to allow execution to proceed
                flow.workingMemory.put("repository_files", getSbmlModel())
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
                submissionService.handleSubmission(flow.workingMemory)
            }
            on("success").to "displayConfirmationPage"
            on("error").to "displayErrorPage"
        }
        displayConfirmationPage()
        displayErrorPage()
        abort()
    }

    private RFTC createRFTC(File file, boolean isMain) {
        new RFTC(path: file.getCanonicalPath(), mainFile: isMain, userSubmitted: true, hidden: false, description:file.getName())
    }

    private File getFileForTest(String filename, String text)
    {
        File tempFile=File.createTempFile("nothing",null)
        def testFile=new File(tempFile.getParent()+File.separator+filename)
        if (text) testFile.setText(text)
        return testFile
    }

    private List<RFTC> createRFTCList(File mainFile, List<File> additionalFiles) {
        List<RFTC> returnMe=new LinkedList<RFTC>()
        returnMe.add(createRFTC(mainFile, true))
        additionalFiles.each {
            returnMe.add(createRFTC(it, false))
        }
        returnMe
    }

    private File bigModel() {
        return new File("test/files/BIOMD0000000272.xml")
    }

    private List<RFTC> getSbmlModel() {
        return createRFTCList(bigModel(), [getFileForTest("additionalFile.txt", "heres some randomText")])
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
