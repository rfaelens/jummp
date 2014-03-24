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
* Spring Framework, Spring Security (or a modified version of that library), containing parts
* covered by the terms of Apache License v2.0, the licensors of this
* Program grant you additional permission to convey the resulting work.
* {Corresponding Source for a non-source form of such a combination shall
* include the source code for the parts of Spring Framework, Spring Security used as well as
* that of the covered work.}
**/





package net.biomodels.jummp.webapp

import com.wordnik.swagger.annotations.*
import grails.plugins.springsecurity.Secured
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import net.biomodels.jummp.core.ModelException
import net.biomodels.jummp.core.model.PublicationTransportCommand
import net.biomodels.jummp.core.model.RepositoryFileTransportCommand as RFTC
import net.biomodels.jummp.core.model.RevisionTransportCommand
import net.biomodels.jummp.core.model.ModelTransportCommand
import net.biomodels.jummp.core.model.ModelState
import net.biomodels.jummp.plugins.security.PersonTransportCommand
import net.biomodels.jummp.webapp.UploadFilesCommand
import org.springframework.web.multipart.MultipartFile
import net.biomodels.jummp.core.model.PublicationTransportCommand
import org.apache.commons.lang.exception.ExceptionUtils
import grails.transaction.*
import static org.springframework.http.HttpStatus.*
import static org.springframework.http.HttpMethod.*
import org.apache.commons.io.FileUtils
import net.biomodels.jummp.core.model.ModelAuditTransportCommand
import net.biomodels.jummp.core.model.audit.*
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject
import org.codehaus.groovy.grails.web.json.JSONArray
import net.biomodels.jummp.core.model.PermissionTransportCommand

@Api(value = "/model", description = "Operations related to models")
class ModelController {
    /**
     * Flag that checks whether the dynamically-inserted logger is set to DEBUG or higher.
     */
    private final boolean IS_DEBUG_ENABLED = log.isDebugEnabled()
    
    def springSecurityService;
    /**
     * Dependency injection of modelDelegateService.
     **/
    def modelDelegateService
    /**
     * Dependency injection of modelFileFormatService
     **/
    def modelFileFormatService

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
    /**
    * Dependency injenction of pubMedService 
    */
    def pubMedService
    /*
    * Dependency injection of mailService
    */
    def mailService
    
    def beforeInterceptor = [action: this.&auditBefore, except: ['updateFlow', 'createFlow', 'uploadFlow', 'showWithMessage', 'share']]
    
    def afterInterceptor = [ action: this.&auditAfter, except: ['updateFlow', 'createFlow', 'uploadFlow', 'showWithMessage', 'share']] 
    
    
    private String getUsername() {
    	String username="anonymous"
		def principal = springSecurityService.principal
		if (principal instanceof String) {
			username=principal;
		}
		else if (principal) {
			username = principal.username
		}
		return username		
    }
    
    private void auditBefore() {
    	String model=params.id.split("\\.")[0]
    	long modelId=Long.parseLong(model)
    	String username=getUsername();
		String accessType=actionUri
		// publish uses revision ids, annoyingly enough.
		if (accessType.contains("publish")) {
			modelId=modelDelegateService.getRevisionDetails(new RevisionTransportCommand(id: modelId)).model.id
		}
		String formatType=params.format?:"html"
		String changesMade=null;
		int historyItem=updateHistory(modelId, username, accessType, formatType, changesMade)
		session.lastHistory=historyItem;
    }
    
    private void auditAfter(def model) {
    	modelDelegateService.updateAuditSuccess(session.lastHistory, true)
    	session.removeAttribute("lastHistory")
    }
    
    private int updateHistory(long model, String user, String accessType, 
    	String formatType, String changesMade, boolean success=false) {
    	accessType=accessType.replace("/model/","");
    	AccessFormat format=AccessFormat.HTML
    	try {
    		format=AccessFormat.valueOf(formatType.toUpperCase())
    	}
    	catch(Exception ignore) {
    	}
    	ModelAuditTransportCommand audit=new ModelAuditTransportCommand(model: new ModelTransportCommand(id: model),
    									username: user,
    									format: format,
    									type: AccessType.fromAction(accessType),
    									changesMade: changesMade,
    									success: success)
    	long returned=modelDelegateService.createAuditItem(audit)
    	return returned
    }
    
    def showWithMessage = {
        flash["giveMessage"]=params.flashMessage
        redirect(action: "show", id:params.id)
    }

    @ApiOperation(value = "Show a model.", httpMethod = "GET",
                response = net.biomodels.jummp.webapp.rest.model.show.Model.class,
                notes = "Pass the expected media type of the request as a parameter e.g. /model/id?format=json")
    @ApiImplicitParam(name = "modelId", value = "The model identifier", required = true, allowMultiple = false)
    def show() {
        if (!params.format || params.format=="html") {
        		RevisionTransportCommand rev=modelDelegateService.getRevision(params.id)
        		boolean showPublishOption = modelDelegateService.canPublish(rev.model.id)
        		boolean canUpdate = modelDelegateService.canAddRevision(rev.model.id)
        		boolean canDelete = modelDelegateService.canDelete(rev.model.id)
        		boolean canShare = modelDelegateService.canShare(rev.model.id)
        		String flashMessage=""
        		if (flash.now["giveMessage"]) {
        			flashMessage=flash.now["giveMessage"]
        		}
        		List<RevisionTransportCommand> revs=modelDelegateService.getAllRevisions(rev.model.id)
        		def model=[revision: rev, 
        				   authors: rev.model.creators,
        				   allRevs: revs,
        				   flashMessage: flashMessage,
        				   canUpdate: canUpdate,
        				   canDelete: canDelete,
        				   canShare: canShare,
        				   showPublishOption: showPublishOption,
        		]
        		if (rev.id == modelDelegateService.getLatestRevision(rev.model.id).id)
        		{
        			flash.genericModel=model
        			forward controller:modelFileFormatService.getPluginForFormat(rev.model.format),
                			action:"show",
                			id: params.id
                }
                else { //showing an old version, with the default page. Dont allow updates.
                	model["canUpdate"]=false
                	model["showPublishOption"]=false
                	model["oldVersion"]=true
                	model["canDelete"]=false
                	model["canShare"]=false
                	return model
                }
        }
        else {
        	RevisionTransportCommand rev=modelDelegateService.getRevision(params.id)
           	respond new net.biomodels.jummp.webapp.rest.model.show.Model(rev)
        }
    }

    def files = {
        def revisionFiles = modelDelegateService.getRevision(params.id).files
        def responseFiles = revisionFiles.findAll { !it.hidden }
        respond new net.biomodels.jummp.webapp.rest.model.show.ModelFiles(responseFiles)
    }

    def publish = {
       def rev=new RevisionTransportCommand(id: params.id as int)
       modelDelegateService.publishModelRevision(rev)
       redirect(action: "showWithMessage", id: modelDelegateService.getRevisionDetails(rev).model.id,
                params: [flashMessage:"Model has been published."])
    }

    def delete = {
       boolean deleted=modelDelegateService.deleteModel(params.id as int)
       redirect(action: "showWithMessage", id: params.id,
                params: [flashMessage: deleted?"Model has been deleted, and moved into archives.":"Model could not be deleted"])
    }
    
    def share = {
    	if (params.id) {
    		def rev=modelDelegateService.getRevisionDetails(new RevisionTransportCommand
    													(id: Long.parseLong(params.id)));
    		def perms=modelDelegateService.getPermissionsMap(rev.model.id);
    		return [revision: rev, permissions: perms as JSON]
    	}
    	else throw new Exception("Model version must be specified to share");
    }
    
    def shareUpdate = {
    	if (params.id && params.collabMap) {
    		def map=JSON.parse(params.collabMap);
    		List<PermissionTransportCommand> collabsNew=new LinkedList<PermissionTransportCommand>();
    		for (int i=0; i<map.length(); i++) {
    			JSONObject perm=map.getJSONObject(i);
    			PermissionTransportCommand ptc=new PermissionTransportCommand(
    														id: perm.getString("id"),
    														name: perm.getString("name"),
    														read: perm.getBoolean("read"),
    														write: perm.getBoolean("write"));
    			System.out.println("GOT :"+ptc.getProperties());
    			collabsNew.add(ptc);
    		}
    		modelDelegateService.setPermissions(params.id as Long, collabsNew);
    		render (['success': true, 'permissions': modelDelegateService.getPermissionsMap(params.id as Long)] as JSON)
    	}
    	else {
    		render (['success': false, 'message': "Couldnt update stuff"] as JSON)
    	}
    }
    
   @Secured(["isAuthenticated()"])
    def updateFlow = {
        start {
            action {
                if (!params.id) {
                    return error()
                }
                if (!modelDelegateService.canAddRevision(params.id as Long)) {
                	return accessDenied()
                }
                conversation.model_id=params.id
            }
            on("success").to "uploadPipeline"
            on("error"){
            	session.updateMissingId="True"
            }.to "displayErrorPage"
            on("accessDenied"){
            	Long id=-1;
            	try {
            		id=Long.parseLong(params.id)
            	}
            	catch(Exception e) {
            	}
            	if (params.id) {
            		updateHistory(id,
            				  "something wrong", 
            				  "update", 
            				  "html", 
            				   null,
            				   false)
            	}
            }.to "displayAccessDenied"
        }
        uploadPipeline {
            subflow(controller: "model", action: "upload", input: [isUpdate: true])
            on("abort").to "abort"
            on("displayConfirmationPage"){
            	String update=conversation.changesMade.join(". ")
            	String model=conversation.model_id
            	String user=getUsername()
            	updateHistory(session.result_submission,
            				  user, 
            				  "update", 
            				  "html", 
            				  update,
            				  true)
            }.to "displayConfirmationPage"
            on("displayErrorPage").to "displayErrorPage"
        }
        abort()
        displayConfirmationPage()
        displayErrorPage()
        displayAccessDenied()
    }

    @Secured(["isAuthenticated()"])
    def createFlow = {
        uploadPipeline {
            subflow(controller: "model", action: "upload", input: [isUpdate:false])
            on("abort").to "abort"
            on("displayConfirmationPage") {
            	updateHistory(session.result_submission,
            				  getUsername(), 
            				  "create", 
            				  "html", 
            				   null,
            				   true)
            }.to "displayConfirmationPage"
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
                flow.workingMemory=workingMemory
                flow.workingMemory.put("isUpdateOnExistingModel",flow.isUpdate)
                conversation.changesMade=new TreeSet<String>();
                if (flow.isUpdate) {
                    Long model_id=conversation.model_id as Long
                    flow.workingMemory.put("model_id", model_id)
                    flow.workingMemory.put("LastRevision", modelDelegateService.getLatestRevision(model_id))
                    /* Maintain reference to the previous revision in session 
                       memory to ensure it is not overwritten. Do it with a
                       random variable name to allow updating of multiple
                       models simultaneously by the same user
                    */
                    String alphabet=(('A'..'Z')+('a'..'z')).join()
                    String variableName=new Random().with {
                    	(1..10).collect { alphabet[ nextInt( alphabet.length() ) ] }.join()
    				}
                    flow.workingMemory.put("SafeReferenceVariable",variableName)
                    session."${variableName}"=flow.workingMemory.get("LastRevision")
                }
                submissionService.initialise(flow.workingMemory)
                if (flow.isUpdate) {
                	skipDisclaimer()
                }
                else {
                	goToDisclaimer()
                }
            }
            on("skipDisclaimer").to "uploadFiles"
            on("goToDisclaimer").to "displayDisclaimer"
            on(Exception).to "handleException"
        }
        displayDisclaimer {
            on("Continue").to "uploadFiles"
            on("Cancel").to "cleanUpAndTerminate"
        }
        uploadFiles {
            on("Upload") {
                def mainMultipartList = request.getMultiFileMap().mainFile
                def extraFileField = request.getMultiFileMap().extraFiles
                List<MultipartFile> extraMultipartList = []
                if (extraFileField instanceof MultipartFile) {
                    extraMultipartList = [extraFileField]
                }
                else {
                    extraMultipartList = extraFileField
                }
                def descriptionFields = params?.description ?: [""]
                if (descriptionFields instanceof String) {
                    descriptionFields = [descriptionFields]
                }
                def noMains = params.deletedMain
                List<String> mainsToBeDeleted = []
                if (noMains) {
                    if (!(noMains instanceof CharSequence)) {
                        mainsToBeDeleted.addAll(Arrays.asList(noMains))
                    } else {
                        mainsToBeDeleted.add(noMains)
                    }
                }
                def noAdditionals = params.deletedAdditional
                List<String> additionalsToBeDeleted = []
                if (noAdditionals) {
                    if (noAdditionals.getClass().isArray()) {
                        additionalsToBeDeleted.addAll(Arrays.asList(noAdditionals))
                    } else {
                        additionalsToBeDeleted.add(noAdditionals)
                    }
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
                cmd.mainFile = mainMultipartList
                cmd.extraFiles = extraMultipartList
                cmd.mainDeletes = mainsToBeDeleted
                cmd.extraDeletes = additionalsToBeDeleted
                cmd.description = descriptionFields
                if (IS_DEBUG_ENABLED) {
                    log.debug "Data binding done :${cmd.properties}"
                }
                flow.workingMemory.put("UploadCommand", cmd)
            }.to "transferFilesToService"
            on("ProceedWithoutValidation"){
            }.to "inferModelInfo"
            on("Cancel").to "cleanUpAndTerminate"
            on("Back"){}.to "displayDisclaimer"
        }
        transferFilesToService {
            action {
            	UploadFilesCommand cmd = flow.workingMemory.remove("UploadCommand") as UploadFilesCommand
            	boolean fileValidationError=false
            	boolean furtherProcessingRequired=true
                def deletedMains = cmd.mainDeletes
                List mainFiles=getMainFiles(flow.workingMemory)
                boolean isOK = !mainFileDeleted(mainFiles, cmd.mainFile, deletedMains)
                def multipartDebug = cmd.mainFile.collect { it.getOriginalFilename() }
                def mainFilesDebug = mainFiles.collect { new File(it.path).name }
                if (!isOK) {
                    return MainFileMissingError()
                }
            	if (!cmd.validate()) {
                    // No main file! This must be an error!
                    fileValidationError=true
                    // Unless there was one all along
                    
                    if (cmd.errors["mainFile"].codes.find{ it.contains("mainFile.blank") || it.contains("mainFile.nullable") }) {
                    	if (flow.workingMemory.containsKey("repository_files")) {
                            if (mainFiles && !mainFiles.isEmpty()) {
                            	if (!mainFileOverwritten(mainFiles, cmd.extraFiles)) {
                            	    fileValidationError=false
                            	    furtherProcessingRequired=true
                            	}
                            	else {
                            		return AdditionalReplacingMainError()
                            	}
                            } 
                            else {
                            	return MainFileMissingError()
                            }
                        }
                        else {
                           	return MainFileMissingError()
                        }
                    }
                    else {
                        throw new Exception("Error in uploading files. Cmd did not validate: ${cmd.getProperties()}")
                    }
                } 
                else {
                    if (IS_DEBUG_ENABLED) {
                        log.debug("The files are valid.")
                    }
                }
                if (!fileValidationError && furtherProcessingRequired) {
                    //should this be in a separate action state?
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
                        RFTC existing = flow.workingMemory.get("repository_files").get(0) as RFTC
                        submission_folder = (new File(existing.path)).getParentFile()
                    }
                    def parent = submission_folder.canonicalPath + sep
                    List<File> mainFileList;
                    if (cmd.mainFile) {
                        mainFileList = transferFiles(parent, cmd.mainFile)
                    }
                    else {
                        mainFileList = new LinkedList<File>()
                    }
                    List<File> extraFileList = transferFiles(parent, cmd.extraFiles)
                    List<String> descriptionList = cmd.description
                    def additionalsMap = [:]
                    extraFileList.eachWithIndex{ file, i ->
                        additionalsMap[file] = descriptionList[i]
                    }
                    if (IS_DEBUG_ENABLED) {
                        log.debug "About to submit ${mainFileList.inspect()} and ${additionalsMap.inspect()}."
                    }
                    flow.workingMemory["submitted_mains"] = mainFileList
                    flow.workingMemory["submitted_additionals"] = additionalsMap
                    List<String> deletedFileNames = []
                    deletedFileNames.addAll(deletedMains)
                    deletedFileNames.addAll(cmd.extraDeletes)
                    // ensure there are no lists within this list
                    flow.workingMemory["deleted_filenames"] = deletedFileNames.flatten()
                    submissionService.handleFileUpload(flow.workingMemory)
                }
            }
            on("MainFileMissingError") {
                flash.flashMessage="submission.upload.error.fileerror"
            }.to "uploadFiles"
            on("AdditionalReplacingMainError") {
                flash.flashMessage="submission.upload.error.additional_replacing_main"
            }.to "uploadFiles"
            on("success").to "performValidation"
            on(Exception).to "handleException"
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
            }.to "inferModelInfo"
            on("FilesNotValid") {
                flash.error="submission.upload.error.fileerror"
            }.to "uploadFiles"
            on(Exception).to "handleException"
        }
        inferModelInfo {
            action {
                submissionService.inferModelInfo(flow.workingMemory)
            }
            on("success").to "displayModelInfo"
            on(Exception).to "handleException"
        }
        displayModelInfo {
            on("Continue") {
                //populate modifications object with form data
                Map<String,Object> modifications = new HashMap<String,Object>()
                final String NAME = params.name
                final String DESC = params.description
                if (NAME && NAME.trim()) {
                    modifications.put("new_name", NAME.trim())
                }
                if (DESC && DESC.trim()) {
                    modifications.put("new_description", DESC.trim())
                }
                submissionService.refineModelInfo(flow.workingMemory, modifications)
            }.to "enterPublicationLink"
            on("Cancel").to "cleanUpAndTerminate"
            on("Back"){}.to "uploadFiles"
        }
        enterPublicationLink {
        	on("Continue") {
        		if (!params.PubLinkProvider && params.PublicationLink) {
        			flash.flashMessage="Please select a publication link type."
        			return error()
        		}
        		Map<String,String> modifications=new HashMap<String,String>()
                if (params.PubLinkProvider && params.PublicationLink) {
                	if (!pubMedService.verifyLink(params.PubLinkProvider,params.PublicationLink)) {
                		flash.flashMessage="The link is not a valid ${params.PubLinkProvider}"
                		return error()
        			}
                	ModelTransportCommand model=flow.workingMemory.get("ModelTC") as ModelTransportCommand
        			if (params.PubLinkProvider !=model.publication?.linkProvider 
        				|| params.PublicationLink !=model.publication?.link) {
        					modifications.put("PubLinkProvider",params.PubLinkProvider) 
        					modifications.put("PubLink",params.PublicationLink)
        					submissionService.updatePublicationLink(flow.workingMemory, modifications)
        			}
        			else {
        				flow.workingMemory.put("RetrievePubDetails", false)
        			}
                }
                else {
                	flow.workingMemory.put("RetrievePubDetails", false)
                }
        	}.to "getPublicationDataIfPossible"
        	on("Cancel").to "cleanUpAndTerminate"
            on("Back").to "displayModelInfo"
        }
        getPublicationDataIfPossible {
        	action {
        		if (flow.workingMemory.remove("RetrievePubDetails") as Boolean) {
        			ModelTransportCommand model=flow.workingMemory.get("ModelTC") as ModelTransportCommand
        			if (model.publication.link && model.publication.linkProvider) {
        				def retrieved
        				try {
        					retrieved=pubMedService.
        							getPublication(model.publication)
        				}
        				catch(Exception e) {
        					e.printStackTrace()
        				}
        				if (retrieved) {
        					model.publication = retrieved.toCommandObject() 
        					flow.workingMemory.put("Authors", model.publication.authors)
        				}
        			}
    				conversation.changesMade.add("Amended publication details");
    				publicationInfoPage()
        		}
        		else {
        			displaySummaryOfChanges()
        		}
        	}
        	on("publicationInfoPage").to "publicationInfoPage"
        	on("displaySummaryOfChanges").to "displaySummaryOfChanges"
        	on(Exception).to "handleException"
        }
        publicationInfoPage {
            on("Continue"){
            		ModelTransportCommand model=flow.workingMemory.get("ModelTC") as ModelTransportCommand
            	    bindData(model.publication, params, [exclude: ['authors']])
            	    String[] authorList=params.authorFieldTotal.split("!!author!!")
            	    List<PersonTransportCommand> validatedAuthors=new LinkedList<PersonTransportCommand>()
            	    authorList.each {
            	    	    if (it) {
            	    	    String[] parts=it.split("<init>")
            	    	    String name=parts[0];
            	    	    String orcid=parts[1];
            	    	    String institution=parts[2];
            	    	    System.out.println("NOW PROCESSING: "+it+"..."+name+"..."+orcid+".."+institution)
            	    	    def authorListSrc=model.publication.authors
            	    	    if (!authorListSrc) {
            	    	    	    authorListSrc=new LinkedList<PersonTransportCommand>();
            	    	    }
            	    	    def author=authorListSrc.find { auth ->
            	    	    	if (orcid != "no_orcid") {
            	    	    		return orcid == auth.orcid
            	    	    	}
            	    	    	else {
            	    	    		return name == auth.userRealName
            	    	    	}
            	    	    	return false
            	    	    }
            	    	    if (!author) {
            	    	    	System.out.println("COULDNT FIND AUTHOR "+name+" in ${authorListSrc}")
            	    	    	author=new PersonTransportCommand(userRealName:parts[0], 
            	    	    					  orcid: orcid!="no_orcid" ? orcid: null, 
            	    	    					  institution: institution!="no_institution_provided" ? institution:null)
            	    	    	if (!model.publication.authors) {
            	    	        	model.publication.authors=new LinkedList<PersonTransportCommand>()
            	    	        }
            	    	        model.publication.authors.add(author)
            	    	    }
            	    	    System.out.println("AUTHORS IN PUBINFO "+model.publication.authors.inspect())
        	    	        if (author.validate()) {
        	    	        	validatedAuthors.add(author)
        	    	        	System.out.println("AUTHORS IN VALIDATED "+validatedAuthors.inspect())
        	    	        }
        	    	        else {
        	    	        	log.error "Submission did not validate: ${author.properties}."
        	    	        	log.error "Errors: ${author.errors.allErrors.inspect()}."
        	    	        	flash.validationErrorOn=author
        	    	        	return error()
        	    	        }
        	    	 }
            	   }
            	   model.publication.authors=validatedAuthors
            	   System.out.println("AUTHOR SIZE" +model.publication.authors.size())
            	   if (!model.publication.validate()) {
            	   	   log.error "Submission did not validate: ${model.publication.properties}."
            	   	   log.error "Errors: ${model.publication.errors.allErrors.inspect()}."
            	   	   flash.validationErrorOn=model.publication
            	   	   return error()
            	   }
            }.to "displaySummaryOfChanges"
            on("Cancel").to "cleanUpAndTerminate"
            on("Back").to "enterPublicationLink"
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
                submissionService.updateFromSummary(flow.workingMemory, modifications)
            }.to "saveModel"
            on("Cancel").to "cleanUpAndTerminate"
            on("Back"){}.to "enterPublicationLink"
        }
        saveModel {
            action {
            	def changes=submissionService.handleSubmission(flow.workingMemory);
            	changes.each {
            		conversation.changesMade.add(it.toString());
            	}
                session.result_submission=flow.workingMemory.get("model_id")
                if (flow.isUpdate) {
                	flash.sendMessage="Model ${session.result_submission} has been updated."
                	session.removeAttribute(flow.workingMemory.get("SafeReferenceVariable") as String)
                    return redirectWithMessage()
                }
            }
            on("success").to "displayConfirmationPage"
            on("redirectWithMessage").to "redirectWithMessage"
            on(Exception).to "handleException"
        }
        cleanUpAndTerminate {
            action {
                submissionService.cleanup(flow.workingMemory)
                if (flow.isUpdate) {
                	flash.sendMessage="Model update was cancelled."
                	session.removeAttribute(flow.workingMemory.get("SafeReferenceVariable") as String)
                	return redirectWithMessage()
                }
            }
            on("success").to "abort"
            on("redirectWithMessage").to "redirectWithMessage"
        }
        redirectWithMessage {
        	action {
        		redirect(controller:'model', action:'showWithMessage', id:conversation.model_id, params: [flashMessage: flash.sendMessage])
        	}
        	on("success").to "displayConfirmationPage"
        	on(Exception).to "displayConfirmationPage"
        }
        handleException {
        	action {
        		String stackTrace=ExceptionUtils.getStackTrace(flash.flowExecutionException)
        		String ticket=UUID.randomUUID().toString()
        		if (flow.isUpdate) {
                	session.removeAttribute(flow.workingMemory.get("SafeReferenceVariable") as String)
                }
                if (flow.workingMemory.containsKey("repository_files")) {
                	def repFile=flow.workingMemory.get("repository_files").first()
                	if (repFile) {
                		File aSingleFile=new File(repFile.path)
                		File buggyFiles=new File(new File(grailsApplication.config.jummp.vcs.exchangeDirectory),"buggy")
                		File temporaryStorage=new File(buggyFiles, ticket)
                		temporaryStorage.mkdirs()
                		FileUtils.copyDirectory(new File(aSingleFile.getParent()), temporaryStorage)
                	}
                }
                submissionService.cleanup(flow.workingMemory)
        		mailService.sendMail {
        			to grailsApplication.config.jummp.security.registration.email.adminAddress
        			from grailsApplication.config.jummp.security.registration.email.sender
        			subject "Bug in submission: ${ticket}"
        			body stackTrace
                }
        		session.messageForError=ticket   
        	}
        	on("success").to "displayErrorPage"
        }
        abort()
        displayConfirmationPage()
        displayErrorPage()
    }

    private void serveModelAsZip(List<RFTC> files, def resp) {
    	    ByteArrayOutputStream byteBuffer=new ByteArrayOutputStream()
            ZipOutputStream zipFile = new ZipOutputStream(byteBuffer)
            files.each
            {
            	File file=new File(it.path)
                zipFile.putNextEntry(new ZipEntry(file.getName()))
                byte[] fileData=file.getBytes()
                zipFile.write(fileData,0,fileData.length)
                zipFile.closeEntry()
            }
            zipFile.close()
            resp.setContentType("application/zip")
            // TODO: set a proper name for the model
            resp.setHeader("Content-disposition", "attachment;filename=\"model.zip\"")
            resp.outputStream << new ByteArrayInputStream(byteBuffer.toByteArray())
    }
    
    private void serveModelAsFile(RFTC rf, def resp, boolean inline) {
            File file=new File(rf.path)
            resp.setContentType(rf.mimeType)
            resp.setHeader("Content-disposition", "${inline? "inline" : "attachment"};filename=\"${file.getName()}\"")
            resp.outputStream<< new ByteArrayInputStream(file.getBytes())
    }
    
    
    /**
     * File download of the model file for a model by id
     */
    def download = {
        try
        {
        	if (!params.filename) {	
        		List<RFTC> files = modelDelegateService.retrieveModelFiles(modelDelegateService.getRevision(params.id as String))
        		List<RFTC> mainFiles = files.findAll { it.mainFile }
        		if (files.size() == 1) {
            		   serveModelAsFile(files.first(), response, false)
            	}
            	else if (mainFiles.size() == 1) {
            		   serveModelAsFile(mainFiles.first(), response, false)
            	}
            	else {
            	       serveModelAsZip(files, response)
            	}
            }
            else {
            	List<RFTC> files = modelDelegateService.
            						retrieveModelFiles(modelDelegateService.getRevision(params.id as String))
            	RFTC requested=files.find {
                    if (it.hidden) {
                        return false
                    }
            	    File file=new File(it.path)
            	    file.getName()==params.filename
            	}
            	boolean inline=true
            	if (!params.inline) {
            		inline=false
            	}
            	if (requested) {
            	    serveModelAsFile(requested, response, inline)
            	}            	
            }
        }
        catch(Exception e)
        {
            e.printStackTrace()
        }
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
    
    private List getMainFiles(Map<String,Object> workingMemory) {
    	    List<RFTC> uploaded=workingMemory.get("repository_files") as List<RFTC>
            return uploaded.findAll {
            	    it.mainFile
            }
    }
    
    private boolean mainFileOverwritten(List mainFiles, List multipartFiles) {
    	boolean returnVal=false
    	mainFiles.each { mainFile ->
    		String name=(new File(mainFile.path)).getName()
    		multipartFiles.each { uploaded ->
    			if (uploaded.getOriginalFilename() == name) {
    				returnVal=true
    			}
    		}
	}
    	return returnVal
    }

    private boolean mainFileDeleted(List mainFiles, List cmdMains, List<String> mainsToBeDeleted) {
        def nonEmptyCmdMains = cmdMains?.find{!it.isEmpty()}
        if (nonEmptyCmdMains) {
            return false
        }
        def mainFileNames = mainFiles.collect { new File(it.path).name }
        def wtf =  (mainFileNames - mainsToBeDeleted)
        return wtf.isEmpty()
    }
}
