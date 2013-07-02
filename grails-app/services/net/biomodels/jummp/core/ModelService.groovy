package net.biomodels.jummp.core

import net.biomodels.jummp.core.events.LoggingEventType
import net.biomodels.jummp.core.events.ModelCreatedEvent
import net.biomodels.jummp.core.events.PostLogging
import net.biomodels.jummp.core.events.RevisionCreatedEvent
import net.biomodels.jummp.core.model.ModelListSorting
import net.biomodels.jummp.core.model.ModelState
import net.biomodels.jummp.core.model.ModelTransportCommand
import net.biomodels.jummp.core.model.RevisionTransportCommand
import net.biomodels.jummp.core.model.PublicationLinkProvider
import net.biomodels.jummp.core.model.RepositoryFileTransportCommand
import net.biomodels.jummp.core.vcs.VcsException
import net.biomodels.jummp.model.Model
import net.biomodels.jummp.model.ModelFormat
import net.biomodels.jummp.model.Publication
import net.biomodels.jummp.model.RepositoryFile
import net.biomodels.jummp.model.Revision
import net.biomodels.jummp.plugins.security.User
import org.apache.commons.io.FileUtils
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.perf4j.aop.Profiled
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.access.prepost.PostAuthorize
import org.springframework.security.access.prepost.PostFilter
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.acls.domain.BasePermission
import org.springframework.security.acls.domain.PrincipalSid
import org.springframework.security.acls.model.Acl
import org.springframework.security.core.userdetails.UserDetails

/**
 * @short Service class for managing Models
 *
 * This service provides the high-level API to access models and their revisions.
 * It is recommended to use this service instead of accessing Models and Revisions
 * directly through GORM. The service methods respect the ACL on the objects and by
 * that ensures that no user can perform actions he is not allowed to.
 * @see Model
 * @see Revision
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 * @date 20130625
 */
@SuppressWarnings("GroovyUnusedCatchParameter")
class ModelService {
    /**
     * Dependency Injection of Spring Security Service
     */
    def springSecurityService
    /**
     * Dependency Injection of AclUtilService
     */
    def aclUtilService
    /**
     * Dependency Injection of VcsService
     */
    def vcsService
    /**
     * Dependency Injection of ModelFileFormatService
     */
    def modelFileFormatService
    /**
     * Dependency Injection for GrailsApplication
     */
    @SuppressWarnings("GrailsStatelessService")
    def grailsApplication
    /**
     * Dependency Injection for PubMedService
     */
    def pubMedService
    /**
     * Dependency injection for ExecutorService to run threads
     */
    def executorService
    /**
     * Dependency injection of ModelHistoryService
     */
    def modelHistoryService
    /**
     * Dependency Injection of FileSystemService
     */
    def fileSystemService

    static transactional = true

    /**
    * Returns list of Models the user has access to.
    *
    * Searches for all Models the current user has access to, that is @ref getLatestRevision
    * does not return @c null for any Model in the returned list.
    * This method provides pagination.
    * @param offset Offset in the list
    * @param count Number of models to return
    * @param sortOrder @c true for ascending, @c false for descending
    * @param sortColumn the column which should be sorted
    * @param filter Optional filter for search
    * @return List of Models
    **/
    @PostLogging(LoggingEventType.RETRIEVAL)
    @Profiled(tag="modelService.getAllModels")
    public List<Model> getAllModels(int offset, int count, boolean sortOrder, ModelListSorting sortColumn, String filter = null) {
        if (offset < 0 || count <= 0) {
            // safety check
            return []
        }
        String sorting = sortOrder ? 'asc' : 'desc'
        // for Admin - sees all (not deleted) models
        if (SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")) {
            def criteria = Model.createCriteria()
            return criteria.list {
                ne("state", ModelState.DELETED)
                if (filter && filter.length() >= 3) {
                    or {
                        ilike("name", "%${filter}%")
                        publication {
                            or {
                                ilike("journal", "%${filter}%")
                                ilike("title", "%${filter}%")
                                ilike("affiliation", "%${filter}%")
                            }
                        }
                    }
                }
                maxResults(count)
                firstResult(offset)
                switch (sortColumn) {
                case ModelListSorting.NAME:
                    order("name", sorting)
                    break
                case ModelListSorting.LAST_MODIFIED:
                    revisions {
                        order("uploadDate", sorting)
                    }
                    break
                case ModelListSorting.FORMAT:
                    revisions {
                        order("format", sorting)
                    }
                    break
                case ModelListSorting.PUBLICATION:
                    // TODO: implement, fall through to default
                case ModelListSorting.ID: // Id is the default
                default:
                    order("id", sorting)
                    break
                }
            }
        }

        Set<String> roles = SpringSecurityUtils.authoritiesToRoles(SpringSecurityUtils.getPrincipalAuthorities())
        if (springSecurityService.isLoggedIn()) {
            // anonymous users do not have a principal
            roles.add((springSecurityService.getPrincipal() as UserDetails).getUsername())
        }
        String query = '''
SELECT DISTINCT m FROM Revision AS r, AclEntry AS ace
JOIN r.model AS m
JOIN ace.aclObjectIdentity AS aoi
JOIN aoi.aclClass AS ac
JOIN ace.sid AS sid
WHERE
aoi.objectId = r.id
AND ac.className = :className
AND sid.sid IN (:roles)
AND ace.mask IN (:permissions)
AND ace.granting = true
AND m.state != :deleted
AND r.deleted = false
'''
        if (filter && filter.length() >= 3) {
            query += '''
AND (
lower(m.name) like :filter
OR lower(m.publication.journal) like :filter
OR lower(m.publication.title) like :filter
OR lower(m.publication.affiliation) like :filter
)
'''
        }
        query += '''
ORDER BY
'''
        switch (sortColumn) {
        case ModelListSorting.NAME:
            query += "m.name"
            break
        case ModelListSorting.LAST_MODIFIED:
            query += "r.uploadDate"
            break
        case ModelListSorting.FORMAT:
            query += "r.format"
            break
        case ModelListSorting.PUBLICATION:
            // TODO: implement, fall through to default
        case ModelListSorting.ID: // Id is the default
        default:
            query += "m.id"
            break
        }
        query += " " + sorting
        Map params = [
            className: Revision.class.getName(),
            permissions: [BasePermission.READ.getMask(), BasePermission.ADMINISTRATION.getMask()],
            deleted: ModelState.DELETED,
            roles: roles, max: count, offset: offset]
        if (filter && filter.length() >= 3) {
            params.put("filter", "%${filter.toLowerCase()}%");
        }

        return Model.executeQuery(query, params)
    }

    /**
    * Convenient method for sorting by the id column.
    *
    * @return List of Models sorted ascending
    * @see ModelService#getAllModels(int offset, int count, boolean sortOrder)
    **/
    @PostLogging(LoggingEventType.RETRIEVAL)
    @Profiled(tag="modelService.getAllModels")
    public List<Model> getAllModels(int offset, int count, boolean sortOrder) {
        getAllModels(offset, count, sortOrder, ModelListSorting.ID)
    }

    /**
    * Convenient method for ascending sorting.
    *
    * @return List of Models sorted ascending by @p sortColumn
    * @see ModelService#getAllModels(int offset, int count, boolean sortOrder)
    **/
    @PostLogging(LoggingEventType.RETRIEVAL)
    @Profiled(tag="modelService.getAllModels")
    public List<Model> getAllModels(int offset, int count, ModelListSorting sortColumn) {
        return getAllModels(offset, count, true, sortColumn)
    }

    /**
    * Convenient method for ascending sorting by id.
    *
    * @return List of Models sorted ascending by id
    * @see ModelService#getAllModels(int offset, int count, boolean sortOrder)
    **/
    @PostLogging(LoggingEventType.RETRIEVAL)
    @Profiled(tag="modelService.getAllModels")
    public List<Model> getAllModels(int offset, int count) {
        return getAllModels(offset, count, ModelListSorting.ID)
    }

    /**
    * Convenient method for ascending sorting of first ten models.
    *
    * @param sortColumn the column which should be sorted
    * @return List of first 10 Models sorted ascending by @p sortColumn
    * @see ModelService#getAllModels(int offset, int count, boolean sortOrder)
    **/
    @PostLogging(LoggingEventType.RETRIEVAL)
    @Profiled(tag="modelService.getAllModels")
    public List<Model> getAllModels(ModelListSorting sortColumn) {
        return getAllModels(0, 10, true, sortColumn)
    }

    /**
    * Convenient method for ascending sorting of first ten models by id.
    *
    * @return List of first 10 Models sorted ascending by id
    * @see ModelService#getAllModels(int offset, int count, boolean sortOrder)
    **/
    @PostLogging(LoggingEventType.RETRIEVAL)
    @Profiled(tag="modelService.getAllModels")
    public List<Model> getAllModels() {
        return getAllModels(ModelListSorting.ID)
    }

    /**
    * Returns the number of Models the user has access to.
    *
    * @param filter Optional filter for search
    * @see ModelService#getAllModels()
    **/
    @PostLogging(LoggingEventType.RETRIEVAL)
    @Profiled(tag="modelService.getModelCount")
    public Integer getModelCount(String filter = null) {
        if (SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")) {
            // special handling for Admin - is allowed to see all (not deleted) Models
            def criteria = Model.createCriteria()
            return criteria.get {
                ne("state", ModelState.DELETED)
                if (filter && filter.length() >= 3) {
                    or {
                        ilike("name", "%${filter}%")
                        publication {
                            or {
                                ilike("journal", "%${filter}%")
                                ilike("title", "%${filter}%")
                                ilike("affiliation", "%${filter}%")
                            }
                        }
                    }
                }
                projections {
                    count("id")
                }
            } as Integer
        }

        Set<String> roles = SpringSecurityUtils.authoritiesToRoles(SpringSecurityUtils.getPrincipalAuthorities())
        if (springSecurityService.isLoggedIn()) {
            // anonymous users do not have a principal
            roles.add((springSecurityService.getPrincipal() as UserDetails).getUsername())
        }

        String query = '''
SELECT COUNT(DISTINCT m.id) FROM Revision AS r, AclEntry AS ace
JOIN r.model AS m
JOIN ace.aclObjectIdentity AS aoi
JOIN aoi.aclClass AS ac
JOIN ace.sid AS sid
WHERE
aoi.objectId = r.id
AND ac.className = :className
AND sid.sid IN (:roles)
AND ace.mask IN (:permissions)
AND ace.granting = true
AND m.state != :deleted
AND r.deleted = false
'''
        if (filter && filter.length() >= 3) {
            query += '''
AND (
lower(m.name) like :filter
OR lower(m.publication.journal) like :filter
OR lower(m.publication.title) like :filter
OR lower(m.publication.affiliation) like :filter
)
'''
        }
        Map params = [
            className: Revision.class.getName(),
            permissions: [BasePermission.READ.getMask(), BasePermission.ADMINISTRATION.getMask()],
            deleted: ModelState.DELETED,
            roles: roles]
        if (filter && filter.length() >= 3) {
            params.put("filter", "%${filter.toLowerCase()}%");
        }

        return Model.executeQuery(query, params)[0] as Integer
    }

    /**
     * Returns the Model identified by @p id.
     * @param id The id of the model.
     * @return The Model
     */
    @PostLogging(LoggingEventType.RETRIEVAL)
    @Profiled(tag="modelService.getModel")
    public Model getModel(long id) {
        Model model = Model.get(id)
        if (model) {
            if (!getLatestRevision(model)) {
                throw new AccessDeniedException("No access to Model with Id ${id}")
            }
        } else {
            throw new AccessDeniedException("No access to Model with Id ${id}")
        }
        return model
    }

    /**
    * Queries the @p model for the latest available revision the user has read access to.
    * @param model The Model for which the latest revision should be retrieved.
    * @return Latest Revision the current user has read access to. If there is no such revision null is returned
    **/
    @PostLogging(LoggingEventType.RETRIEVAL)
    @Profiled(tag="modelService.getLatestRevision")
    public Revision getLatestRevision(Model model) {
        if (!model) {
            return null
        }
        if (model.state == ModelState.DELETED) {
            // exclude deleted models
            return null
        }
        // admin gets max (non deleted) revision
        if (SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")) {
            List<Long> result = Revision.executeQuery('''
                SELECT rev.id
                FROM Revision AS rev
                JOIN rev.model.revisions AS revisions
                WHERE
                rev.model = :model
                AND revisions.deleted = false
                GROUP BY rev.model, rev.id, rev.revisionNumber
                HAVING rev.revisionNumber = max(revisions.revisionNumber)''', [model: model]) as List
            if (!result) {
                return null
            }
//            modelHistoryService.addModelToHistory(model)
            return Revision.get(result[0])
        }

        Set<String> roles = SpringSecurityUtils.authoritiesToRoles(SpringSecurityUtils.getPrincipalAuthorities())
        if (springSecurityService.isLoggedIn()) {
            // anonymous users do not have a principal
            roles.add((springSecurityService.getPrincipal() as UserDetails).getUsername())
        }
        List<Long> result = Revision.executeQuery('''
SELECT rev.id
FROM Revision AS rev, AclEntry AS ace
JOIN rev.model.revisions AS revisions
JOIN ace.aclObjectIdentity AS aoi
JOIN aoi.aclClass AS ac
JOIN ace.sid AS sid
WHERE
rev.model = :model
AND revisions.deleted = false
AND aoi.objectId = revisions.id
AND ac.className = :className
AND sid.sid IN (:roles)
AND ace.mask IN (:permissions)
AND ace.granting = true
GROUP BY rev.model, rev.id, rev.revisionNumber
HAVING rev.revisionNumber = max(revisions.revisionNumber)''', [
                model: model,
                className: Revision.class.getName(),
                roles: roles,
                permissions: [BasePermission.READ.getMask(), BasePermission.ADMINISTRATION.getMask()] ]) as List
            if (!result) {
                return null
            }
            modelHistoryService.addModelToHistory(model)
            return Revision.get(result[0])
    }

    /**
    * Queries the @p model for all revisions the user has read access to.
    * The returned list is ordered by revision number of the model.
    * @param model The Model for which all revisions should be retrieved
    * @return List of Revisions ordered by revision numbers of underlying VCS. If the user has no access to any revision an empty list is returned
    * @todo: add paginated version with offset and count. Problem: filter
    **/
    @PostFilter("hasPermission(filterObject, read) or hasRole('ROLE_ADMIN')")
    @PostLogging(LoggingEventType.RETRIEVAL)
    @Profiled(tag="modelService.getAllRevisions")
    public List<Revision> getAllRevisions(Model model) {
        if (model.state == ModelState.DELETED) {
            return []
        }
        // exclude deleted revisions
        return model.revisions.toList().findAll { !it.deleted }.sort {it.revisionNumber}
    }

    /**
     * Queries the @p model for the revision with @p revisionNumber.
     * If there is no such revision @c null will be returned. The
     * returned object is filtered against the ACL.
     * @param model The Model to which the revision belongs
     * @param revisionNumber The revision number in context of the Model
     * @return The revision or @c null if there is no such revision
     */
    @PostAuthorize("hasPermission(returnObject, read) or hasRole('ROLE_ADMIN')")
    @PostLogging(LoggingEventType.RETRIEVAL)
    @Profiled(tag="modelService.getRevision")
    public Revision getRevision(Model model, int revisionNumber) {
        Revision revision = Revision.findByRevisionNumberAndModel(revisionNumber, model)
        if (revision.deleted) {
            return null
        } else {
            modelHistoryService.addModelToHistory(model)
            revision.refresh()
            return revision
        }
    }

    /**
     * Returns the reference publication of this @p model.
     * @param model The Model for which the reference publication should be returned.
     * @return The reference publication
     * @throws IllegalArgumentException if @p model is null
     * @throws AccessDeniedException if the current user is not allowed to access at least one Model Revision
     */
    @PostLogging(LoggingEventType.RETRIEVAL)
    @Profiled(tag="modelService.getPublication")
    public Publication getPublication(final Model model) throws AccessDeniedException, IllegalArgumentException {
        if (!model) {
            throw new IllegalArgumentException("Model may not be null")
        }
        if (!getLatestRevision(model)) {
            throw new AccessDeniedException("You are not allowed to view Model with id ${model.id}")
        }
        return model.publication
    }

    
    
    
    
    /**
    * Creates a new Model and stores it in the VCS.
    *
    * Stores the @p repoFile as a new file in the VCS and creates a Model for it.
    * The Model will have one Revision attached to it. The MetaInformation for this
    * Model is taken from @p meta. The user who uploads the Model becomes the owner of
    * this Model. The new Model is not visible to anyone except the owner.
    * @param repoFile The wrapper for the model file that will be stored in the VCS.
    * @param meta Meta Information to be added to the model
    * @return The newly-created Model, or null if the model could not be created
    * @throws ModelException If Model File is not valid or the Model could not be stored in VCS
    **/
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostLogging(LoggingEventType.CREATION)
    @Profiled(tag="modelService.uploadModelAsFile")
    public Model uploadModelAsFile(final RepositoryFileTransportCommand repoFile, ModelTransportCommand meta)
            throws ModelException {
        if (repoFile) {
           return uploadModelAsList([repoFile], meta)
        }
        log.error("No file provided during the update of ${meta.properties}")
        throw new ModelException(meta, "The new version of the model does not have any files.")
    }

    
    
        /**
    * Adds a new Revision to the model, to be used by SubmissionService
    * The provided @p modelFiles will be stored in the VCS as an update to the existing files of the same @p model.
    * A new Revision will be created and appended to the list of Revisions of the @p model. 
    * The revision will not be validated, as the checks are assumed to have been conducted
    * already
    * @param model The Model the revision should be added
    * @param modelFiles The model files to be stored in the VCS as a new revision
    * @param format The format of the model files
    * @param comment The commit message for the new revision
    * @return The newly-added Revision. In case an error occurred while accessing the VCS @c null will be returned.
    * @throws ModelException If either @p model, @p modelFiles or @p comment are null or if the files do not exist or are directories.
    **/
//    @PreAuthorize("hasPermission(#model, write) or hasRole('ROLE_ADMIN')")
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostLogging(LoggingEventType.UPDATE)
    @Profiled(tag="modelService.addRevisionAsList")
    public Revision addValidatedRevision(final List<RepositoryFileTransportCommand> repoFiles, RevisionTransportCommand rev) throws ModelException {
        // TODO: the method should be thread safe, add a lock
        if (!rev.model) {
            throw new ModelException(null, "Model may not be null")
        }
        if (rev.model.state == ModelState.DELETED) {
            throw new ModelException(rev.model, "A new Revision cannot be added to a deleted model")
        }
        if (rev.comment == null) {
            throw new ModelException(rev.model, "Comment may not be null, empty comment is allowed")
        }
        List<File> modelFiles = []
        for (rf in repoFiles) {
            final def f = new File(rf.path)
            modelFiles.add(f)
        }
        final User currentUser = User.findByUsername(springSecurityService.authentication.name)
        Model model=getModel(rev.model.id)
        Revision revision = new Revision(model: model, name: rev.name, description: rev.description, comment: rev.comment, uploadDate: new Date(), owner: currentUser,
                minorRevision: false, validated:rev.validated, format: ModelFormat.findByIdentifier(rev.format.identifier))
        List<RepositoryFile> domainObjects = []
        for (rf in repoFiles) {
            final String fileName = rf.path.split(File.separator).last()
            final def domain = new RepositoryFile(path: rf.path, description: rf.description, 
                    mimeType: rf.mimeType, revision: revision)
            if (rf.mainFile) {
                domain.mainFile = rf.mainFile
            }
            if (rf.userSubmitted) {
                domain.userSubmitted = rf.userSubmitted
            }
            if (rf.hidden) {
                domain.hidden = rf.hidden
            }
            if (!domain.validate()) {
                final def m = model.toCommandObject()
                def msg = new StringBuffer("Invalid file ${rf.properties} uploaded during the update of model ${m.properties}.")
                msg.append("The file failed due to ${domain.errors.allErrors.inspect()}")
                log.error(msg)
                final String culprit = new File(rf.path).name
                throw new ModelException(m, "Your submission appears to contain invalid file ${fileName}. Please review it and try again.")
            } else {
                domainObjects.add(domain)
            }
        }

        // save the new model in the database
        try {
            String vcsId = vcsService.updateModel(model, modelFiles, revision.comment)
            revision.vcsId = vcsId
        } catch (VcsException e) {
            revision.discard()
            domainObjects.each{ it.discard() }
            log.error("Exception occurred during uploading a new Model Revision to VCS: ${e.getMessage()}")
            throw new ModelException(model.toCommandObject(),
                "Could not store new Model Revision for Model ${model.id} with VcsIdentifier ${model.vcsIdentifier} in VCS", e)
        }
        // calculate the new revision number - accessing the revisions directly to circumvent ACL
        revision.revisionNumber = model.revisions.sort {it.revisionNumber}.last().revisionNumber + 1

        if (revision.validate()) {
            model.addToRevisions(revision)
            //save repoFiles, revision and model in one go
            model.save(flush: true)
            aclUtilService.addPermission(revision, currentUser.username, BasePermission.ADMINISTRATION)
            aclUtilService.addPermission(revision, currentUser.username, BasePermission.READ)
            aclUtilService.addPermission(revision, currentUser.username, BasePermission.DELETE)
            // grant read access to all users having read access to the model
            Acl acl = aclUtilService.readAcl(model)
            for (ace in acl.entries) {
                if (ace.sid instanceof PrincipalSid && ace.permission == BasePermission.READ) {
                    aclUtilService.addPermission(revision, ace.sid.principal, BasePermission.READ)
                }
            }
            revision.refresh()
            executorService.submit(grailsApplication.mainContext.getBean("fetchAnnotations", model.id, revision.id))
            grailsApplication.mainContext.publishEvent(new RevisionCreatedEvent(this, revision.toCommandObject(), vcsService.retrieveFiles(revision)))
        } else {
            System.out.println(revision.errors)
            
            // TODO: this means we have imported the revision into the VCS, but it failed to be saved in the database, which is pretty bad
            revision.discard()
            final def m = model.toCommandObject()
            log.error("New Revision containing ${repoFiles.inspect()} for Model ${m} with VcsIdentifier ${model.vcsIdentifier} added to VCS, but not stored in database")
            throw new ModelException(m, "Revision stored in VCS, but not in database")
        }
        return revision
    }

    
    
    /**
    * Creates a new Model and stores it in the VCS. Stripped down version suitable
    * for calling from SubmissionService, where model has already been validated
    *
    * Stores the @p modelFile as a new file in the VCS and creates a Model for it.
    * The Model will have one Revision attached to it. The MetaInformation for this
    * Model is taken from @p meta. The user who uploads the Model becomes the owner of
    * this Model. The new Model is not visible to anyone except the owner.
    * @param repoFiles The list of command objects corresponding to the files of the model that is to be stored in the VCS.
    * @param rev Meta Information to be added to the model
    * @return The new created Model, or null if the model could not be created
    * @throws ModelException If Model File is not valid or the Model could not be stored in VCS
    **/
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostLogging(LoggingEventType.CREATION)
    @Profiled(tag="modelService.uploadValidatedModel")
    public Model uploadValidatedModel(final List<RepositoryFileTransportCommand> repoFiles, RevisionTransportCommand rev)
            throws ModelException {
        // TODO: to support anonymous submissions this method has to be changed
        if (Model.findByName(rev.model.name)) {
            final ModelTransportCommand MODEL = Model.findByName(rev.model.name).toCommandObject()
            final String msg = "There is already a Model with name ${rev.model.name}".toString()
            log.warn(msg)
            /*log.error(msg)
            throw new ModelException(rev.model, msg)*/
        }
        Model model = new Model(name: rev.model.name)
        List<File> modelFiles = []
        for (rf in repoFiles) {
            final String path = rf.path
            final def f = new File(path)
            modelFiles.add(f)
        }
        ModelFormat format=ModelFormat.findByIdentifier(rev.format.identifier)

        // vcs identifier is upload date + name - this should by all means be unique
        String pathPrefix =
                fileSystemService.findCurrentModelContainer() + File.separator
        String timestamp = new Date().format("yyyy-MM-dd'T'HH-mm-ss-SSS")
        String modelPath = new StringBuilder(pathPrefix).append(timestamp).append("_").append(model.name).
                append(File.separator).toString()
        boolean success = new File(modelPath).mkdirs()
        if (!success) {
            def err = "Cannot create the directory where the ${model.name} should be stored"
            log.error(err)
            throw new ModelException(rev.model, err)
        }
        model.vcsIdentifier = modelPath
        //model.vcsIdentifier = model.vcsIdentifier.replace('/', '_').replace(':', '_').replace('\\', '_')

        Revision revision = new Revision(model: model,
                revisionNumber: 1,
                owner: User.findByUsername(springSecurityService.authentication.name),
                minorRevision: false,
                validated: rev.validated,
                name: rev.name,
                description: rev.description,
                comment: rev.comment,
                uploadDate: new Date(),
                format: ModelFormat.findByIdentifier(rev.model.format.identifier))

        // keep a list of RFs closeby, as we may need to discard all of them
        List<RepositoryFile> domainObjects = []
        for (rf in repoFiles) {
            /*
             * only store the name of the file in the database, as the location can change and
             * we generate the correct path when the RepositoryFileTransportCommand wrapper is created
             */
            String fileName = rf.path.split(File.separator).last()
            final def domain = new RepositoryFile(path: rf.path, description: rf.description,
                    mimeType: rf.mimeType, revision: revision)
            if (rf.mainFile) {
                domain.mainFile = rf.mainFile
            }
            if (rf.userSubmitted) {
                domain.userSubmitted = rf.userSubmitted
            }
            if (rf.hidden) {
                domain.hidden = rf.hidden
            }
            if (!domain.validate()) {
                def msg = new StringBuffer("Invalid file ${rf.properties} uploaded during the creation of model ${rev.model}.")
                msg.append("The file failed due to ${domain.errors.allErrors.inspect()}")
                log.error(msg)
                throw new ModelException(rev.model, "The submission appears to contain invalid file ${fileName}. Please review it and try again.")
            } else {
                domainObjects.add(domain)
            }
        }

        try {
            revision.vcsId = vcsService.importModel(model, modelFiles)
        } catch (VcsException e) {
            revision.discard()
            domainObjects.each { it.discard() }
            model.discard()
            //TODO undo the addition of the files to the VCS.
            def errMsg = new StringBuffer("Exception occurred while storing new Model ")
            errMsg.append("${model.toCommandObject().properties} to VCS: ${e.getMessage()}.\n")
            errMsg.append("${model.errors.allErrors.inspect()}\n")
            errMsg.append("${revision.errors.allErrors.inspect()}\n")
            log.error(errMsg)
            throw new ModelException(model.toCommandObject(),
                "Could not store new Model ${model.toCommandObject().properties} in VCS", e)
        }

        if (revision.validate()) {
            model.addToRevisions(revision)
            if (rev.model.publication && rev.model.publication.linkProvider == PublicationLinkProvider.PUBMED) {
                try {
                    model.publication = pubMedService.getPublication(rev.model.publication.link)
                } catch (JummpException e) {
                    revision.discard()
                    domainObjects.each { it.discard() }
                    model.discard()
                    def error = new StringBuffer("New Model ${model.name} does not validate:")
                    error.append("${model.errors.allErrors.inspect()}\n")
                    error.append("${revision.errors.allErrors.inspect()}\n")
                    log.error(error)
                    throw new ModelException(model.toCommandObject(), "Error while parsing PubMed data for ${model.name}", e)
                }
            } else if (rev.model.publication &&
                    (rev.model.publication.linkProvider == PublicationLinkProvider.DOI || rev.model.publication.linkProvider == PublicationLinkProvider.URL)) {
                model.publication = Publication.fromCommandObject(rev.model.publication)
            }
            if (!model.validate()) {
                // TODO: this means we have imported the file into the VCS, but it failed to be saved in the database, which is pretty bad
                revision.discard()
                model.discard()
                def msg  = new StringBuffer("New Model ${model.name} does not validate:\n")
                msg.append("${model.errors.allErrors.inspect()}\n")
                msg.append("${revision.errors.allErrors.inspect()}\n")
                log.error(msg)
                throw new ModelException(model.toCommandObject(), "New model does not validate")
            }
            model.save(flush: true)
            // let's add the required rights
            final String username = revision.owner.username
            aclUtilService.addPermission(model, username, BasePermission.ADMINISTRATION)
            aclUtilService.addPermission(model, username, BasePermission.DELETE)
            aclUtilService.addPermission(model, username, BasePermission.READ)
            aclUtilService.addPermission(model, username, BasePermission.WRITE)
            aclUtilService.addPermission(revision, username, BasePermission.ADMINISTRATION)
            aclUtilService.addPermission(revision, username, BasePermission.DELETE)
            aclUtilService.addPermission(revision, username, BasePermission.READ)
            try {
                if (!rev.model.publication) {
                    String annotation = getPubMedAnnotation(model)
                    String pubMed
                    if (annotation) {
                        if (annotation.contains(":")) {
                            pubMed = annotation.substring(annotation.lastIndexOf(":")+1, annotation.indexOf("]")).trim()
                            //TODO Replace CiteXplore with EuropePMC URLs
                            //model.publication = pubMedService.getPublication(pubMed)
                            model.publication = null
                        }
                    }
                }
            } catch (JummpException e) {
                log.debug(e.message, e)
            }

            executorService.submit(grailsApplication.mainContext.getBean("fetchAnnotations", model.id))

            // broadcast event
            grailsApplication.mainContext.publishEvent(new ModelCreatedEvent(this, model.toCommandObject(), modelFiles))
        } else {
            System.out.println(revision.errors)
            // TODO: this means we have imported the file into the VCS, but it failed to be saved in the database, which is pretty bad
            revision.discard()
            domainObjects.each {it.discard()}
            model.discard()
            log.error("New Model ${model.properties} with properties ${rev.model.properties} does not validate:${revision.errors.allErrors.inspect()}")
            throw new ModelException(model.toCommandObject(), "Sorry, but the new Model does not seem to be valid.")
        }
        return model
    }
    
    
    
    
   
    
    
    
    /**
    * Creates a new Model and stores it in the VCS.
    *
    * Stores the @p modelFile as a new file in the VCS and creates a Model for it.
    * The Model will have one Revision attached to it. The MetaInformation for this
    * Model is taken from @p meta. The user who uploads the Model becomes the owner of
    * this Model. The new Model is not visible to anyone except the owner.
    * @param repoFiles The list of command objects corresponding to the files of the model that is to be stored in the VCS.
    * @param meta Meta Information to be added to the model
    * @return The new created Model, or null if the model could not be created
    * @throws ModelException If Model File is not valid or the Model could not be stored in VCS
    **/
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostLogging(LoggingEventType.CREATION)
    @Profiled(tag="modelService.uploadModelAsList")
    public Model uploadModelAsList(final List<RepositoryFileTransportCommand> repoFiles, ModelTransportCommand meta)
            throws ModelException {
        // TODO: to support anonymous submissions this method has to be changed
        if (Model.findByName(meta.name)) {
            final ModelTransportCommand MODEL = Model.findByName(meta.name).toCommandObject()
            final String msg = "There is already a Model with name ${meta.name}".toString()
            log.warn(msg)
            /*log.error(msg)
            throw new ModelException(meta, msg)*/
        }
        if (!repoFiles || repoFiles.size() == 0) {
            log.error("No files were provided as part of the submission of model ${meta.properties}")
            throw new ModelException(meta, "A model must contain at least one file.")
        }
        Model model = new Model(name: meta.name)
        List<File> modelFiles = []
        for (rf in repoFiles) {
            if (!rf || !rf.path) {
                log.error("No file was provided as part of the submission of model ${meta.properties}")
                throw new ModelException(meta, "Please supply at least one file for this model.")
            }
            final String path = rf.path
            if (!path || path.isEmpty()) {
                log.error("Null file encountered while creating ${meta.properties}: ${repoFiles.properties}")
                throw new ModelException(meta,
                    "Sorry, there was a problem with one of the files you submitted. Please refine the files you wish to upload and try again.")
            }
            final def f = new File(path)
            if (!f.exists()) {
                log.error("Non-existent file detected while uploading a new revision for ${meta.properties}: ${f.properties}")
                throw new ModelException(meta,
                    "Sorry, one of the files you submitted does not appear to exist. Please refine the files you wish to upload and try again")
            }
            if (f.isDirectory()) {
                log.error("Folder detected while uploading a new revision for ${meta.properties}: ${repoFiles.properties}")
                throw new ModelException(meta, "Sorry, we currently do not accept models organised into subfolders.")
            }
            modelFiles.add(f)
        }
        boolean valid=true
        ModelFormat format=ModelFormat.findByIdentifier(meta.format.identifier)
        if (!modelFileFormatService.validate(modelFiles, format)) {
            def err = "The files ${modelFiles.properties} do no comprise valid ${meta.format.identifier}"
            log.error(err)
       //     throw new ModelException(meta, "Invalid ${meta.format.identifier} submission.")
            valid=false
        }
        // model is valid, create a new repository and store it as revision1
        // vcs identifier is upload date + name - this should by all means be unique
        String pathPrefix =
                fileSystemService.findCurrentModelContainer() + File.separator
        String timestamp = new Date().format("yyyy-MM-dd'T'HH-mm-ss-SSS")
        String modelPath = new StringBuilder(pathPrefix).append(timestamp).append("_").append(model.name).
                append(File.separator).toString()
        boolean success = new File(modelPath).mkdirs()
        if (!success) {
            def err = "Cannot create the directory where the ${model.name} should be stored"
            log.error(err)
            throw new ModelException(meta, err)
        }
        model.vcsIdentifier = modelPath
        //model.vcsIdentifier = model.vcsIdentifier.replace('/', '_').replace(':', '_').replace('\\', '_')

        Revision revision = new Revision(model: model,
                revisionNumber: 1,
                owner: User.findByUsername(springSecurityService.authentication.name),
                minorRevision: false,
                validated: valid,
                name: modelFileFormatService.extractName(modelFiles, format),
                description: modelFileFormatService.extractDescription(modelFiles, format),
                comment: meta.comment,
                uploadDate: new Date(),
                format: ModelFormat.findByIdentifier(meta.format.identifier))

        // keep a list of RFs closeby, as we may need to discard all of them
        List<RepositoryFile> domainObjects = []
        for (rf in repoFiles) {
            /*
             * only store the name of the file in the database, as the location can change and
             * we generate the correct path when the RepositoryFileTransportCommand wrapper is created
             */
            String fileName = rf.path.split(File.separator).last()
            final def domain = new RepositoryFile(path: rf.path, description: rf.description,
                    mimeType: rf.mimeType, revision: revision)
            if (rf.mainFile) {
                domain.mainFile = rf.mainFile
            }
            if (rf.userSubmitted) {
                domain.userSubmitted = rf.userSubmitted
            }
            if (rf.hidden) {
                domain.hidden = rf.hidden
            }
            if (!domain.validate()) {
                def msg = new StringBuffer("Invalid file ${rf.properties} uploaded during the creation of model ${meta}.")
                msg.append("The file failed due to ${domain.errors.allErrors.inspect()}")
                log.error(msg)
                throw new ModelException(meta, "The submission appears to contain invalid file ${fileName}. Please review it and try again.")
            } else {
                domainObjects.add(domain)
            }
        }

        try {
            revision.vcsId = vcsService.importModel(model, modelFiles)
        } catch (VcsException e) {
            revision.discard()
            domainObjects.each { it.discard() }
            model.discard()
            //TODO undo the addition of the files to the VCS.
            def errMsg = new StringBuffer("Exception occurred while storing new Model ")
            errMsg.append("${model.toCommandObject().properties} to VCS: ${e.getMessage()}.\n")
            errMsg.append("${model.errors.allErrors.inspect()}\n")
            errMsg.append("${revision.errors.allErrors.inspect()}\n")
            log.error(errMsg)
            throw new ModelException(model.toCommandObject(),
                "Could not store new Model ${model.toCommandObject().properties} in VCS", e)
        }

        if (revision.validate()) {
            model.addToRevisions(revision)
            if (meta.publication && meta.publication.linkProvider == PublicationLinkProvider.PUBMED) {
                try {
                    model.publication = pubMedService.getPublication(meta.publication.link)
                } catch (JummpException e) {
                    revision.discard()
                    domainObjects.each { it.discard() }
                    model.discard()
                    def error = new StringBuffer("New Model ${model.name} does not validate:")
                    error.append("${model.errors.allErrors.inspect()}\n")
                    error.append("${revision.errors.allErrors.inspect()}\n")
                    log.error(error)
                    throw new ModelException(model.toCommandObject(), "Error while parsing PubMed data for ${model.name}", e)
                }
            } else if (meta.publication &&
                    (meta.publication.linkProvider == PublicationLinkProvider.DOI || meta.publication.linkProvider == PublicationLinkProvider.URL)) {
                model.publication = Publication.fromCommandObject(meta.publication)
            }
            if (!model.validate()) {
                // TODO: this means we have imported the file into the VCS, but it failed to be saved in the database, which is pretty bad
                revision.discard()
                model.discard()
                def msg  = new StringBuffer("New Model ${model.name} does not validate:\n")
                msg.append("${model.errors.allErrors.inspect()}\n")
                msg.append("${revision.errors.allErrors.inspect()}\n")
                log.error(msg)
                throw new ModelException(model.toCommandObject(), "New model does not validate")
            }
            model.save(flush: true)
            // let's add the required rights
            final String username = revision.owner.username
            aclUtilService.addPermission(model, username, BasePermission.ADMINISTRATION)
            aclUtilService.addPermission(model, username, BasePermission.DELETE)
            aclUtilService.addPermission(model, username, BasePermission.READ)
            aclUtilService.addPermission(model, username, BasePermission.WRITE)
            aclUtilService.addPermission(revision, username, BasePermission.ADMINISTRATION)
            aclUtilService.addPermission(revision, username, BasePermission.DELETE)
            aclUtilService.addPermission(revision, username, BasePermission.READ)
            try {
                if (!meta.publication) {
                    String annotation = getPubMedAnnotation(model)
                    String pubMed
                    if (annotation) {
                        if (annotation.contains(":")) {
                            pubMed = annotation.substring(annotation.lastIndexOf(":")+1, annotation.indexOf("]")).trim()
                            //TODO Replace CiteXplore with EuropePMC URLs
                            //model.publication = pubMedService.getPublication(pubMed)
                            model.publication = null
                        }
                    }
                }
            } catch (JummpException e) {
                log.debug(e.message, e)
            }

            executorService.submit(grailsApplication.mainContext.getBean("fetchAnnotations", model.id))

            // broadcast event
            grailsApplication.mainContext.publishEvent(new ModelCreatedEvent(this, model.toCommandObject(), modelFiles))
        } else {
            // TODO: this means we have imported the file into the VCS, but it failed to be saved in the database, which is pretty bad
            revision.discard()
            domainObjects.each {it.discard()}
            model.discard()
            log.error("New Model ${model.properties} with properties ${meta.properties} does not validate:${revision.errors.allErrors.inspect()}")
            throw new ModelException(model.toCommandObject(), "Sorry, but the new Model does not seem to be valid.")
        }
        return model
    }

    /* *
    * Adds a new Revision to the model.
    *
    * The provided @p file will be stored in the VCS as an update to an existing file of the same @p model.
    * A new Revision will be created and appended to the list of Revisions of the @p model.
    * @param model The Model the revision should be added
    * @param repoFile The RepositoryFileTransportCommand object corresponding to the file that is to be stored in the VCS as a new revision
    * @param format The format of the model file
    * @param comment The commit message for the new revision
    * @return The new added Revision. In case an error occurred while accessing the VCS @c null will be returned.
    * @throws ModelException If either @p model, @p file or @p comment are null or if the file does not exists or is a directory
    */
    @PreAuthorize("hasPermission(#model, write) or hasRole('ROLE_ADMIN')")
    @PostLogging(LoggingEventType.UPDATE)
    @Profiled(tag="modelService.addRevisionAsFile")
    public Revision addRevisionAsFile(Model model, final RepositoryFileTransportCommand repoFile,
            final ModelFormat format, final String comment) throws ModelException {
        return addRevisionAsList(model, [repoFile], format, comment)
    }

    /**
    * Adds a new Revision to the model.
    * The provided @p modelFiles will be stored in the VCS as an update to the existing files of the same @p model.
    * A new Revision will be created and appended to the list of Revisions of the @p model.
    * @param model The Model the revision should be added
    * @param modelFiles The model files to be stored in the VCS as a new revision
    * @param format The format of the model files
    * @param comment The commit message for the new revision
    * @return The newly-added Revision. In case an error occurred while accessing the VCS @c null will be returned.
    * @throws ModelException If either @p model, @p modelFiles or @p comment are null or if the files do not exist or are directories.
    **/
    @PreAuthorize("hasPermission(#model, write) or hasRole('ROLE_ADMIN')")
    @PostLogging(LoggingEventType.UPDATE)
    @Profiled(tag="modelService.addRevisionAsList")
    public Revision addRevisionAsList(Model model, final List<RepositoryFileTransportCommand> repoFiles,
            final ModelFormat format, final String comment) throws ModelException {
        // TODO: the method should be thread safe, add a lock
        if (!model) {
            throw new ModelException(null, "Model may not be null")
        }
        if (model.state == ModelState.DELETED) {
            throw new ModelException(model.toCommandObject(), "A new Revision cannot be added to a deleted model")
        }
        if (comment == null) {
            throw new ModelException(model.toCommandObject(), "Comment may not be null, empty comment is allowed")
        }
        if (!repoFiles || repoFiles.size() == 0) {
            log.error("No files were provided as part of the update of model ${model.properties}")
            throw new ModelException(model.toCommandObject(), "A new version of the model must contain at least one file.")
        }
        List<File> modelFiles = []
        for (rf in repoFiles) {
            if (!rf || !rf.path) {
                log.error("No file was provided as part of the update of model ${model.properties}")
                throw new ModelException(model.toCommandObject(), "Please supply at least one file for the new version of this model.")
            }
            final String path = rf.path
            if (!path || path.isEmpty()) {
                log.error("Null file encountered while uploading a new revision for ${model.properties}: ${repoFiles.properties}")
                throw new ModelException(model.toCommandObject(),
                    "Sorry, there was something wrong with one of the files you submitted. Please refine the files you wish to upload and try again.")
            }
            final def f = new File(path)
            if (!f.exists()) {
                log.error("Non-existent file detected while uploading a new revision for ${model.properties}: ${f.properties}")
                throw new ModelException(model.toCommandObject(),
                    "Sorry, one of the files you submitted does not appear to exist. Please refine the files you wish to upload and try again")
            }
            if (f.isDirectory()) {
                log.error("Folder detected while uploading a new revision for ${model.properties}: ${repoFiles.properties}")
                throw new ModelException(model.toCommandObject(),
                    "Sorry, we currently do not accept model organised into sub-folders.")
            }
            modelFiles.add(f)
        }
        boolean valid=true
        if (!modelFileFormatService.validate(modelFiles, format)) {
            final def m = model.toCommandObject()
            log.warn("New revision of model ${m.properties} containing ${modelFiles.properties} does not comprise valid ${format.identifier}")
            //throw new ModelException(m, "The file list does not comprise valid ${format.identifier}")
            valid=false;
        }

        final User currentUser = User.findByUsername(springSecurityService.authentication.name)
        Revision revision = new Revision(model: model, name: modelFileFormatService.extractName(modelFiles, format), description: modelFileFormatService.extractDescription(modelFiles, format), comment: comment, uploadDate: new Date(), owner: currentUser,
                minorRevision: false, validated:valid, format: format)
        List<RepositoryFile> domainObjects = []
        for (rf in repoFiles) {
            final String fileName = rf.path.split(File.separator).last()
            final def domain = new RepositoryFile(path: rf.path, description: rf.description, 
                    mimeType: rf.mimeType, revision: revision)
            if (rf.mainFile) {
                domain.mainFile = rf.mainFile
            }
            if (rf.userSubmitted) {
                domain.userSubmitted = rf.userSubmitted
            }
            if (rf.hidden) {
                domain.hidden = rf.hidden
            }
            if (!domain.validate()) {
                final def m = model.toCommandObject()
                def msg = new StringBuffer("Invalid file ${rf.properties} uploaded during the update of model ${m.properties}.")
                msg.append("The file failed due to ${domain.errors.allErrors.inspect()}")
                log.error(msg)
                final String culprit = new File(rf.path).name
                throw new ModelException(m, "Your submission appears to contain invalid file ${fileName}. Please review it and try again.")
            } else {
                domainObjects.add(domain)
            }
        }

        // save the new model in the database
        try {
            String vcsId = vcsService.updateModel(model, modelFiles, comment)
            revision.vcsId = vcsId
        } catch (VcsException e) {
            revision.discard()
            domainObjects.each{ it.discard() }
            log.error("Exception occurred during uploading a new Model Revision to VCS: ${e.getMessage()}")
            throw new ModelException(model.toCommandObject(),
                "Could not store new Model Revision for Model ${model.id} with VcsIdentifier ${model.vcsIdentifier} in VCS", e)
        }
        // calculate the new revision number - accessing the revisions directly to circumvent ACL
        revision.revisionNumber = model.revisions.sort {it.revisionNumber}.last().revisionNumber + 1

        if (revision.validate()) {
            model.addToRevisions(revision)
            //save repoFiles, revision and model in one go
            model.save(flush: true)
            aclUtilService.addPermission(revision, currentUser.username, BasePermission.ADMINISTRATION)
            aclUtilService.addPermission(revision, currentUser.username, BasePermission.READ)
            aclUtilService.addPermission(revision, currentUser.username, BasePermission.DELETE)
            // grant read access to all users having read access to the model
            Acl acl = aclUtilService.readAcl(model)
            for (ace in acl.entries) {
                if (ace.sid instanceof PrincipalSid && ace.permission == BasePermission.READ) {
                    aclUtilService.addPermission(revision, ace.sid.principal, BasePermission.READ)
                }
            }
            revision.refresh()
            executorService.submit(grailsApplication.mainContext.getBean("fetchAnnotations", model.id, revision.id))
            grailsApplication.mainContext.publishEvent(new RevisionCreatedEvent(this, revision.toCommandObject(), vcsService.retrieveFiles(revision)))
        } else {
            // TODO: this means we have imported the revision into the VCS, but it failed to be saved in the database, which is pretty bad
            revision.discard()
            final def m = model.toCommandObject()
            log.error("New Revision containing ${repoFiles.inspect()} for Model ${m} with VcsIdentifier ${model.vcsIdentifier} added to VCS, but not stored in database")
            throw new ModelException(m, "Revision stored in VCS, but not in database")
        }
        return revision
    }

    /**
     * Returns whether the current user has the right to add a revision to the model.
     * @param model The model to check
     * @return @c true if the user has write permission on the revision or is an admin user, @c false otherwise.
     */
    @PostLogging(LoggingEventType.RETRIEVAL)
    @Profiled(tag="modelService.canAddRevision")
    public Boolean canAddRevision(final Model model) {
        return (SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN") || aclUtilService.hasPermission(springSecurityService.authentication, model, BasePermission.WRITE))
    }

    /**
     * Retrieves the model files for the @p revision.
     * @param revision The Model Revision for which the files should be retrieved.
     * @return Byte Array of the content of the Model files for the revision.
     * @throws ModelException In case retrieving from VCS fails.
     */
    @PreAuthorize("hasPermission(#revision, read) or hasRole('ROLE_ADMIN')")
    @PostLogging(LoggingEventType.RETRIEVAL)
    @Profiled(tag="modelService.retrieveModelFiles")
    Map<String, byte[]> retrieveModelFiles(final Revision revision) throws ModelException {
        List<File> files
        try {
            files = vcsService.retrieveFiles(revision)
        } catch (VcsException e) {
            log.error("Retrieving Revision ${revision.vcsId} for Model ${revision.model.name} from VCS failed.")
            throw new ModelException(revision.model.toCommandObject(), "Retrieving Revision ${revision.vcsId} from VCS failed.")
        }
        def returnMe=new HashMap<String, byte[]>()
        File tempParentDir=null
        files.each
        {
            returnMe.put(it.name, it.getBytes())
            tempParentDir=it.getParentFile()
            FileUtils.forceDelete(it)
        }
        if (tempParentDir) tempParentDir.deleteDir()
        return returnMe
    }

    /**
     * Retrieves the model files for the latest revision of the @p model
     * @param model The Model for which the files should be retrieved
     * @return Byte Array of the content of the Model files.
     * @throws ModelException In case retrieving from VCS fails.
     */
    @PostLogging(LoggingEventType.RETRIEVAL)
    @Profiled(tag="modelService.retrieveModelFiles")
    Map<String, byte[]> retrieveModelFiles(final Model model) throws ModelException {
        final Revision revision = getLatestRevision(model)
        if (!revision) {
            throw new AccessDeniedException("Sorry you are not allowed to download this Model.")
        }
        return retrieveModelFiles(revision)
    }

    /**
    * Grants read access for @p model to @p collaborator.
    *
    * The @p collaborator receives the right to read all future revisions of the @p model
    * as well as read access to all revisions the current user has read access to.
    * The current user can only grant read access in case he has read access on the @p model
    * himself and the right to grant read access.
    *
    * @param model The Model for which read access should be granted
    * @param collaborator The user who should receive read access
    * @todo Might be better in a CollaborationService?
    **/
    @PreAuthorize("hasPermission(#model, admin) or hasRole('ROLE_ADMIN')")
    @PostLogging(LoggingEventType.UPDATE)
    @Profiled(tag="modelService.grantReadAccess")
    public void grantReadAccess(Model model, User collaborator) {
        // Read access is modeled by adding read access to the model (user will get read access for future revisions)
        // and by adding read access to all revisions the user has access to
        aclUtilService.addPermission(model, collaborator.username, BasePermission.READ)
        Set<Revision> revisions = model.revisions
        for (Revision revision in revisions) {
            if (aclUtilService.hasPermission(springSecurityService.authentication, revision, BasePermission.READ) || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')) {
                aclUtilService.addPermission(revision, collaborator.username, BasePermission.READ)
            }
        }
    }

    /**
    * Grants write access for @p model to @p collaborator.
    *
    * The @p collaborator receives the right to add new revisions to the @p model.
    * The current user can only grant write access in case he has write access on the @p model
    * himself and the right to grant write access.
    *
    * @param model The Model for which write access should be granted
    * @param collaborator The user who should receive write access
    * @todo Might be better in a CollaborationService?
    **/
    @PreAuthorize("hasPermission(#model, admin) or hasRole('ROLE_ADMIN')")
    @PostLogging(LoggingEventType.UPDATE)
    @Profiled(tag="modelService.grantWriteAccess")
    public void grantWriteAccess(Model model, User collaborator) {
        aclUtilService.addPermission(model, collaborator.username, BasePermission.WRITE)
    }

    /**
    * Revokes read access for @p model from @p collaborator.
    *
    * The @p collaborator gets the right to read future revisions to the @p model revoked.
    * Read access to existing revisions is not revoked.
    * Write access to the model (that is uploading new revisions) is also revoked.
    * The current user can only revoke the right if he has the right to read future revisions
    * himself and has the right to grant/revoke read rights on the model. The right is not revoked
    * if the user is an administrator of the model.
    * @param model The Model for which read access should be revoked
    * @param collaborator The User whose read access should be revoked
    * @return @c true if the right has been revoked, @c false otherwise
    * @todo Might be better in a CollaborationService?
    **/
    @PreAuthorize("hasPermission(#model, admin) or hasRole('ROLE_ADMIN')")
    @PostLogging(LoggingEventType.UPDATE)
    @Profiled(tag="modelService.revokeReadAccess")
    public boolean revokeReadAccess(Model model, User collaborator) {
        if (collaborator.username == springSecurityService.authentication.name) {
            // the user cannot revoke his own rights
            return false
        }
        // check whether the collaborator is admin of the model
        Acl acl = aclUtilService.readAcl(model)
        boolean adminToModel = false
        acl.entries.each { ace ->
            if (ace.sid.principal == collaborator.username && ace.permission == BasePermission.ADMINISTRATION) {
                adminToModel = true
            }
        }
        if (adminToModel) {
            return false
        }
        aclUtilService.deletePermission(model, collaborator.username, BasePermission.READ)
        aclUtilService.deletePermission(model, collaborator.username, BasePermission.WRITE)
        return true
    }

    /**
    * Revokes write access for @p model from @p collaborator.
    *
    * The @p collaborator gets the right to add revisions to the @p model revoked.
    * The current user can only revoke the right if he has the right to add revisions
    * himself and has the right to grant/revoke write rights on the model
    * @param model The Model for which write access should be revoked
    * @param collaborator The User whose write access should be revoked
    * @return @c true if the right has been revoked, @c false otherwise
    * @todo Might be better in a CollaborationService?
    **/
    @PreAuthorize("hasPermission(#model, admin) or hasRole('ROLE_ADMIN')")
    @PostLogging(LoggingEventType.UPDATE)
    @Profiled(tag="modelService.revokeWriteAccess")
    public boolean revokeWriteAccess(Model model, User collaborator) {
        if (collaborator.username == springSecurityService.authentication.name) {
            // the user cannot revoke his own rights
            return false
        }
        // check whether the collaborator is admin of the model
        Acl acl = aclUtilService.readAcl(model)
        boolean adminToModel = false
        acl.entries.each { ace ->
            if (ace.sid.principal == collaborator.username && ace.permission == BasePermission.ADMINISTRATION) {
                adminToModel = true
            }
        }
        if (adminToModel) {
            return false
        }
        aclUtilService.deletePermission(model, collaborator.username, BasePermission.WRITE)
        return true
    }

    /**
    * Transfers the ownership of the @p model to @p collaborator.
    *
    * The ownership can only be transferred from a user having the right to grant
    * read/write access and the @p model is not yet under curation or published.
    * The @p collaborator has to have read access to future revisions of the model.
    *
    * All Model specific rights are revoked from the owner and granted to the @p collaborator.
    * This includes:
    * @li Write access to the @p model
    * @li Read access to future revisions of the @p model
    * @li Start of curation
    * @li Grant/Revoke read/write access to the @p model
    * @param model The Model for which the ownership should be transferred.
    * @param collaborator The User who becomes the new owner
    * @todo Might be better in a CollaborationService?
    **/
    @PreAuthorize("hasPermission(#model, admin) or hasRole('ROLE_ADMIN')")
    @PostLogging(LoggingEventType.UPDATE)
    @Profiled(tag="modelService.transferOwnerShip")
    public void transferOwnerShip(Model model, User collaborator) {
        // TODO: implement me
    }

    /**
    * Deletes the @p model including all Revisions.
    *
    * Flags the @p model and all its revisions as deleted. A deletion from VCS is for
    * technical reasons not possible and because of that a deletion of the Model object
    * is not possible.
    *
    * Deletion of @p model is only possible if the model is neither under curation nor published.
    * @param model The Model to be deleted
    * @return @c true in case the Model has been deleted, @c false otherwise.
    * @see ModelService#restoreModel(Model model)
    **/
    @PreAuthorize("hasPermission(#model, delete) or hasRole('ROLE_ADMIN')")
    @PostLogging(LoggingEventType.DELETION)
    @Profiled(tag="modelService.deleteModel")
    public boolean deleteModel(Model model) {
        if (!model) {
            throw new IllegalArgumentException("Model may not be null")
        }
        // TODO: the code does not check whether the model exists
        if (model.state != ModelState.UNPUBLISHED) {
            return false
        }
        model.state = ModelState.DELETED
        model.save(flush: true)
        return model.state == ModelState.DELETED
    }

    /**
    * Restores the deleted @p model.
    *
    * Removes the deleted flag from the model and all its Revisions.
    * @param model The deleted Model to restore
    * @return @c true, whether the state was restored, @c false otherwise.
    * @see ModelService#deleteModel(Model model)
    * @todo might belong in an administration service?
    **/
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostLogging(LoggingEventType.UPDATE)
    @Profiled(tag="modelService.restoreModel")
    public boolean restoreModel(Model model) {
        if (!model) {
            throw new IllegalArgumentException("Model may not be null")
        }
        // TODO: the code does not check whether the model exists
        if (model.state == ModelState.DELETED) {
            model.state = ModelState.UNPUBLISHED
            model.save(flush: true)
            return model.state == ModelState.UNPUBLISHED
        } else {
            return false
        }
    }

    /**
     * Deletes the @p revision of the model if it is the latest Revision of the model.
     *
     * This is no real deletion, but only a flag which is set on the Revision. Due to
     * technical constraints of the underlying version control system a real deletion
     * is not possible.
     * @param revision The Revision to delete
     * @return @c true if revision was deleted, @c false otherwise
     */
    @PreAuthorize("hasPermission(#revision, delete) or hasRole('ROLE_ADMIN')")
    @PostLogging(LoggingEventType.DELETION)
    @Profiled(tag="modelService.deleteRevision")
    public boolean deleteRevision(Revision revision) {
        if (!revision) {
            throw new IllegalArgumentException("Revision may not be null")
        }
        if (revision.deleted) {
            // revision is already deleted
            return false
        }
        // check if the revision is the latest non-deleted method
        if (revision.id != revision.model.revisions.findAll { !it.deleted }.sort { it.revisionNumber }.last().id) {
            // TODO: maybe better throw an exception
            return false
        }
        if (revision.model.revisions.findAll { !it.deleted }.size() == 1) {
            // only one revision, delete the Model
            // first check the ACL, has to be manual as Spring would not intercept the direct method call
            if (aclUtilService.hasPermission(springSecurityService.authentication, revision.model, BasePermission.DELETE) || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')) {
                deleteModel(revision.model)
            } else {
                throw new AccessDeniedException("No permission to delete Model ${revision.model.id}")
            }
        }
        // TODO: delete the model if the revision is the first revision of the model
        revision.deleted = true
        revision.save(flush: true)
        return true
    }

    /**
     * Makes a Model Revision publicly available.
     * This means that ROLE_USER and ROLE_ANONYMOUS gain read access to the Revision and by that also to
     * the Model.
     *
     * Only a Curator with write permission on the Revision or an Administrator are allowed to call this
     * method.
     * @param revision The Revision to be published
     */
    @PreAuthorize("(hasRole('ROLE_CURATOR') and hasPermission(#revision, write)) or hasRole('ROLE_ADMIN')")
    @PostLogging(LoggingEventType.UPDATE)
    @Profiled(tag="modelService.publishModelRevision")
    public void publishModelRevision(Revision revision) {
        if (!revision) {
            throw new IllegalArgumentException("Revision may not be null")
        }
        if (revision.deleted) {
            throw new IllegalArgumentException("Revision may not be deleted")
        }
        aclUtilService.addPermission(revision, "ROLE_USER", BasePermission.READ)
        aclUtilService.addPermission(revision, "ROLE_ANONYMOUS", BasePermission.READ)
    }

    /**
     * Retrieves the pub med annotations of the @p model.
     * @param model The model of which the pub med annotation are to be retrieved
     * @return The retrieved pub med annotations or @c null
     * @throws JummpException
     */
    protected List<String> getPubMedAnnotation(Model model) throws JummpException {
        if (!model) {
            return null
        }
        Revision revision = getLatestRevision(model)
        if (!revision) {
            return null
        }
        return modelFileFormatService.getPubMedAnnotation(revision)
    }
}
