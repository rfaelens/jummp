package net.biomodels.jummp.core

import org.apache.commons.io.FileUtils
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.springframework.security.access.prepost.PostFilter
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.acls.domain.BasePermission
import org.springframework.security.acls.domain.PrincipalSid
import org.springframework.security.acls.model.Acl
import net.biomodels.jummp.core.vcs.VcsException
import net.biomodels.jummp.model.Model
import net.biomodels.jummp.core.model.ModelState
import net.biomodels.jummp.model.ModelVersion
import net.biomodels.jummp.plugins.security.User
import net.biomodels.jummp.model.ModelFormat
import net.biomodels.jummp.core.model.ModelTransportCommand
import net.biomodels.jummp.core.model.ModelListSorting
import org.perf4j.aop.Profiled
import org.springframework.security.access.AccessDeniedException
import net.biomodels.jummp.core.events.PostLogging
import net.biomodels.jummp.core.events.LoggingEventType
import net.biomodels.jummp.core.events.ModelCreatedEvent
import net.biomodels.jummp.core.events.ModelVersionCreatedEvent
import net.biomodels.jummp.core.model.PublicationLinkProvider
import net.biomodels.jummp.model.Publication
import org.springframework.security.access.prepost.PostAuthorize
import org.springframework.security.core.userdetails.UserDetails
import java.util.List;
import java.util.LinkedList;
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
 * @author Mihai Glonț <mglont@ebi.ac.uk>
 * @date 20/03/2013
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
                    versions {
                        order("uploadDate", sorting)
                    }
                    break
                case ModelListSorting.FORMAT:
                    versions {
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
SELECT DISTINCT m FROM ModelVersion AS v, AclEntry AS ace
JOIN v.model AS m
JOIN ace.aclObjectIdentity AS aoi
JOIN aoi.aclClass AS ac
JOIN ace.sid AS sid
WHERE
aoi.objectId = v.id
AND ac.className = :className
AND sid.sid IN (:roles)
AND ace.mask IN (:permissions)
AND ace.granting = true
AND m.state != :deleted
AND v.deleted = false
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
            query += "v.uploadDate"
            break
        case ModelListSorting.FORMAT:
            query += "v.format"
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
            className: ModelVersion.class.getName(),
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
SELECT COUNT(DISTINCT m.id) FROM ModelVersion AS v, AclEntry AS ace
JOIN v.model AS m
JOIN ace.aclObjectIdentity AS aoi
JOIN aoi.aclClass AS ac
JOIN ace.sid AS sid
WHERE
aoi.objectId = v.id
AND ac.className = :className
AND sid.sid IN (:roles)
AND ace.mask IN (:permissions)
AND ace.granting = true
AND m.state != :deleted
AND v.deleted = false
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
            className: ModelVersion.class.getName(),
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
            if (!getLatestVersion(model)) {
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
    @Profiled(tag="modelService.getLatestVersion")
    public ModelVersion getLatestVersion(Model model) {
        if (!model) {
            return null
        }
        if (model.state == ModelState.DELETED) {
            // exclude deleted models
            return null
        }
        // admin gets max (non deleted) revision
        if (SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")) {
            List<Long> result = ModelVersion.executeQuery('''
                SELECT ver.id
                FROM ModelVersion AS ver
                JOIN ver.model.versions AS versions
                WHERE
                ver.model = :model
                AND versions.deleted = false
                GROUP BY ver.model, ver.id, ver.versionNumber
                HAVING ver.versionNumber = max(versions.versionNumber)''', [model: model]) as List
            if (!result) {
                return null
            }
//            modelHistoryService.addModelToHistory(model)
            return ModelVersion.get(result[0])
        }

        Set<String> roles = SpringSecurityUtils.authoritiesToRoles(SpringSecurityUtils.getPrincipalAuthorities())
        if (springSecurityService.isLoggedIn()) {
            // anonymous users do not have a principal
            roles.add((springSecurityService.getPrincipal() as UserDetails).getUsername())
        }
        List<Long> result = ModelVersion.executeQuery('''
SELECT ver.id
FROM ModelVersion AS ver, AclEntry AS ace
JOIN ver.model.versions AS version
JOIN ace.aclObjectIdentity AS aoi
JOIN aoi.aclClass AS ac
JOIN ace.sid AS sid
WHERE
ver.model = :model
AND versions.deleted = false
AND aoi.objectId = versions.id
AND ac.className = :className
AND sid.sid IN (:roles)
AND ace.mask IN (:permissions)
AND ace.granting = true
GROUP BY ver.model, ver.id, ver.versionNumber
HAVING ver.versionNumber = max(versions.versionNumber)''', [
                model: model,
                className: ModelVersion.class.getName(),
                roles: roles,
                permissions: [BasePermission.READ.getMask(), BasePermission.ADMINISTRATION.getMask()] ]) as List
            if (!result) {
                return null
            }
            modelHistoryService.addModelToHistory(model)
            return ModelVersion.get(result[0])
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
    @Profiled(tag="modelService.getAllVersions")
    public List<ModelVersion> getAllVersions(Model model) {
        if (model.state == ModelState.DELETED) {
            return []
        }
        // exclude deleted revisions
        return model.versions.toList().findAll { !it.deleted }.sort {it.versionNumber}
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
    @Profiled(tag="modelService.getVersion")
    public ModelVersion getVersion(Model model, int versionNumber) {
        ModelVersion version = ModelVersion.findByVersionNumberAndModel(versionNumber, model)
        if (version.deleted) {
            return null
        } else {
            modelHistoryService.addModelToHistory(model)
            version.refresh()
            return version
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
        if (!getLatestVersion(model)) {
            throw new AccessDeniedException("You are not allowed to view Model with id ${model.id}")
        }
        return model.publication
    }
    
    /*Convenience function to make overloads from file to list
     */
    
    private List<File> getAsList(File file)
    {
        LinkedList<File> list=new LinkedList<File>();
        list.add(file);
        return list;
    }

    /**
    * Creates a new Model and stores it in the VCS.
    *
    * Stores the @p modelFile as a new file in the VCS and creates a Model for it.
    * The Model will have one Revision attached to it. The MetaInformation for this
    * Model is taken from @p meta. The user who uploads the Model becomes the owner of
    * this Model. The new Model is not visible to anyone except the owner.
    * @param modelFile The model file to be stored in the VCS.
    * @param meta Meta Information to be added to the model
    * @return The new created Model, or null if the model could not be created
    * @throws ModelException If Model File is not valid or the Model could not be stored in VCS
    **/
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostLogging(LoggingEventType.CREATION)
    @Profiled(tag="modelService.uploadModelAsFile")
    public Model uploadModelAsFile(final File modelFile, ModelTransportCommand meta) throws ModelException {
        return uploadModelAsList(getAsList(modelFile));
    }

    
    /**
    * Creates a new Model and stores it in the VCS.
    *
    * Stores the @p modelFile as a new file in the VCS and creates a Model for it.
    * The Model will have one Revision attached to it. The MetaInformation for this
    * Model is taken from @p meta. The user who uploads the Model becomes the owner of
    * this Model. The new Model is not visible to anyone except the owner.
    * @param modelFile The model file to be stored in the VCS.
    * @param meta Meta Information to be added to the model
    * @return The new created Model, or null if the model could not be created
    * @throws ModelException If Model File is not valid or the Model could not be stored in VCS
    **/
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostLogging(LoggingEventType.CREATION)
    @Profiled(tag="modelService.uploadModelAsList")
    public Model uploadModelAsList(final List<File> modelFiles, ModelTransportCommand meta) throws ModelException {
        // TODO: to support anonymous submissions this method has to be changed
        if (Model.findByName(meta.name)) {
            throw new ModelException(Model.findByName(meta.name).toCommandObject(), "There is already a Model with name ${meta.name}")
        }
        Model model = new Model(name: meta.name)
        modelFiles.each
        {
            if (!it) {
                throw new ModelException(model.toCommandObject(), "Null file uploaded")
            }
            if (!it.exists() || it.isDirectory()) {
                throw new ModelException(model.toCommandObject(), "The file ${it.path} does not exist or is a directory")
            }
        }
        if (!modelFileFormatService.validate(modelFiles, ModelFormat.findByIdentifier(meta.format.identifier))) {
                throw new ModelException(model.toCommandObject(), "The file ${modelFile.path} is not a valid ${meta.format} file")
        }
        ModelVersion version = new ModelVersion(model: model,
                versionNumber: 1,
                owner: User.findByUsername(springSecurityService.authentication.name),
                minorVersion: false,
                // comment: meta.comment, TODO!!!
                uploadDate: new Date(),
                format: ModelFormat.findByIdentifier(meta.format.identifier))
        // vcs identifier is upload date + name - this should by all means be unique
        model.vcsIdentifier = version.uploadDate.format("yyyy-MM-dd'T'HH-mm-ss-SSS") + "_" + model.name
   /*This probably needs to change! */     model.vcsIdentifier = model.vcsIdentifier.replace('/', '_').replace(':', '_').replace('\\', '_')
        try {
            version.vcsId = vcsService.importFile(model, modelFile)
        } catch (VcsException e) {
            version.discard()
            model.discard()
            log.error("Exception occurred during importing a new Model to VCS: ${e.getMessage()}")
            throw new ModelException(model.toCommandObject(), "Could not store new Model ${model.name} in VCS", e)
        }

        if (version.validate()) {
            model.addToVersions(version)
            if (meta.publication && meta.publication.linkProvider == PublicationLinkProvider.PUBMED) {
                try {
                    model.publication = pubMedService.getPublication(meta.publication.link)
                } catch (JummpException e) {
                    version.discard()
                    model.discard()
                    throw new ModelException(model.toCommandObject(), "Error while parsing PubMed data", e)
                }
            } else if (meta.publication &&
                    (meta.publication.linkProvider == PublicationLinkProvider.DOI || meta.publication.linkProvider == PublicationLinkProvider.URL)) {
                model.publication = Publication.fromCommandObject(meta.publication)
            }
            if (!model.validate()) {
                // TODO: this means we have imported the file into the VCS, but it failed to be saved in the database, which is pretty bad
                version.discard()
                model.discard()
                log.error("New Model does not validate")
                throw new ModelException(model.toCommandObject(), "Model does not validate")
            }
            model.save(flush: true)
            // let's add the required rights
            final String username = version.owner.username
            aclUtilService.addPermission(model, username, BasePermission.ADMINISTRATION)
            aclUtilService.addPermission(model, username, BasePermission.DELETE)
            aclUtilService.addPermission(model, username, BasePermission.READ)
            aclUtilService.addPermission(model, username, BasePermission.WRITE)
            aclUtilService.addPermission(version, username, BasePermission.ADMINISTRATION)
            aclUtilService.addPermission(version, username, BasePermission.DELETE)
            aclUtilService.addPermission(version, username, BasePermission.READ)
            try {
                if (!meta.publication) {
                    String annotation = getPubMedAnnotation(model)
                    String pubMed
                    if (annotation) {
                        if (annotation.contains(":")) {
                            pubMed = annotation.substring(annotation.lastIndexOf(":")+1, annotation.indexOf("]")).trim()
                            model.publication = pubMedService.getPublication(pubMed)
                        }
                    }
                }
            } catch (JummpException e) {
                log.debug(e.message, e)
            }

            executorService.submit(grailsApplication.mainContext.getBean("fetchAnnotations", model.id))

            // broadcast event
            grailsApplication.mainContext.publishEvent(new ModelCreatedEvent(this, model.toCommandObject(), modelFile))
        } else {
            // TODO: this means we have imported the file into the VCS, but it failed to be saved in the database, which is pretty bad
            version.discard()
            model.discard()
            log.error("New Model Version does not validate")
            throw new ModelException(model.toCommandObject(), "New Model Version does not validate")
        }
        return model
    }
    
    
    /**
    * Adds a new Revision to the model.
    *
    * The provided @p file will be stored in the VCS as an update to an existing file of the same @p model.
    * A new Revision will be created and appended to the list of Revisions of the @p model.
    * @param model The Model the revision should be added
    * @param file The model file to be stored in the VCS as a new revision
    * @param format The format of the model file
    * @param comment The commit message for the new revision
    * @return The new added Revision. In case an error occurred while accessing the VCS @c null will be returned.
    * @throws ModelException If either @p model, @p file or @p comment are null or if the file does not exists or is a directory
    **/
    @PreAuthorize("hasPermission(#model, write) or hasRole('ROLE_ADMIN')")
    @PostLogging(LoggingEventType.UPDATE)
    @Profiled(tag="modelService.addVersionAsFile")
    public ModelVersion addVersionAsFile(Model model, final File file, final ModelFormat format, final String comment) throws ModelException {
            return addVersionAsList(getAsList(file));
    }
    
    
        /**
    * Adds a new Revision to the model.
    *
    * The provided @p file will be stored in the VCS as an update to an existing file of the same @p model.
    * A new Revision will be created and appended to the list of Revisions of the @p model.
    * @param model The Model the revision should be added
    * @param file The model file to be stored in the VCS as a new revision
    * @param format The format of the model file
    * @param comment The commit message for the new revision
    * @return The new added Revision. In case an error occurred while accessing the VCS @c null will be returned.
    * @throws ModelException If either @p model, @p file or @p comment are null or if the file does not exists or is a directory
    **/
    @PreAuthorize("hasPermission(#model, write) or hasRole('ROLE_ADMIN')")
    @PostLogging(LoggingEventType.UPDATE)
    @Profiled(tag="modelService.addVersionAsList")
    public ModelVersion addVersionAsList(Model model, final List<File> modelFiles, final ModelFormat format, final String comment) throws ModelException {
        // TODO: the method should be thread safe, add a lock
        if (!model) {
            throw new ModelException(null, "Model may not be null")
        }
        if (model.state == ModelState.DELETED) {
            throw new ModelException(model.toCommandObject(), "A new version cannot be added to a deleted model")
        }
        if (comment == null) {
            throw new ModelException(model.toCommandObject(), "Comment may not be null, empty comment is allowed")
        }
        modelFiles.each
        {
            if (!it) {
                throw new ModelException(model.toCommandObject(), "File may not be null")
            }
            if (!file.exists() || file.isDirectory()) {
                throw new ModelException(model.toCommandObject(), "The file ${it.path} does not exist or is a directory")
            }
        }
        if (!modelFileFormatService.validate(modelFiles, format)) {
            throw new ModelException(model.toCommandObject(), "The files are not valid ${format}")
        }
        final User currentUser = User.findByUsername(springSecurityService.authentication.name)
        ModelVersion version = new ModelVersion(model: model, /*comment: comment,*/ uploadDate: new Date(), owner: currentUser, minorVersion: false, format: format)
        // save the new file in the database
        try {
            //String vcsId = vcsService.updateFile(model, modelFiles, comment)
            version.vcsId = "todo"
        } catch (VcsException e) {
            version.discard()
            log.error("Exception occurred during uploading a new Model Version to VCS: ${e.getMessage()}")
            throw new ModelException(model.toCommandObject(), "Could not store new Model Version for Model ${model.id} with VcsIdentifier ${model.vcsIdentifier} in VCS", e)
        }
        // calculate the new revision number - accessing the revisions directly to circumvent ACL
        version.versionNumber = model.versions.sort {it.versionNumber}.last().versionNumber + 1

        if (version.validate()) {
            model.addToVersions(version)
            model.save(flush: true)
            aclUtilService.addPermission(version, currentUser.username, BasePermission.ADMINISTRATION)
            aclUtilService.addPermission(version, currentUser.username, BasePermission.READ)
            aclUtilService.addPermission(version, currentUser.username, BasePermission.DELETE)
            // grant read access to all users having read access to the model
            Acl acl = aclUtilService.readAcl(model)
            for (ace in acl.entries) {
                if (ace.sid instanceof PrincipalSid && ace.permission == BasePermission.READ) {
                    aclUtilService.addPermission(version, ace.sid.principal, BasePermission.READ)
                }
            }
            version.refresh()
            executorService.submit(grailsApplication.mainContext.getBean("fetchAnnotations", model.id, version.id))
            grailsApplication.mainContext.publishEvent(new ModelVersionCreatedEvent(this, version.toCommandObject(), modelFiles))
        } else {
            // TODO: this means we have imported the revision into the VCS, but it failed to be saved in the database, which is pretty bad
            version.discard()
            log.error("New Version for Model ${model.id} with VcsIdentifier ${model.vcsIdentifier} added to VCS, but not stored in database")
            throw new ModelException(model.toCommandObject(), "Version stored in VCS, but not in database")
        }
        return version
    }

    
    

    /**
     * Returns whether the current user has the right to add a revision to the model.
     * @param model The model to check
     * @return @c true if the user has write permission on the revision or is an admin user, @c false otherwise.
     */
    @PostLogging(LoggingEventType.RETRIEVAL)
    @Profiled(tag="modelService.canAddVersion")
    public Boolean canAddVersion(final Model model) {
        return (SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN") || aclUtilService.hasPermission(springSecurityService.authentication, model, BasePermission.WRITE))
    }

    /**
     * Retrieves the model file for the @p revision.
     * @param revision The Model Revision for which the file should be retrieved.
     * @return Byte Array of the content of the Model file for the revision.
     * @throws ModelException In case retrieving from VCS fails.
     */
    @PreAuthorize("hasPermission(#version, read) or hasRole('ROLE_ADMIN')")
    @PostLogging(LoggingEventType.RETRIEVAL)
    @Profiled(tag="modelService.retrieveModel")
    byte[] retrieveModel(final ModelVersion version) throws ModelException {
        File file;
        try {
            file = vcsService.retrieveFile(version)
        } catch (VcsException e) {
            log.error("Retrieving ModelVersion ${version.vcsId} for Model ${version.model.name} from VCS failed.")
            throw new ModelException(version.model.toCommandObject(), "Retrieving Version ${version.vcsId} from VCS failed.")
        }
        byte[] bytes = file.getBytes()
        FileUtils.forceDelete(file)
        return bytes
    }

    /**
     * Retrieves the model file for the latest revision of the @p model
     * @param model The Model for which the file should be retrieved
     * @return Byte Array of the content of the Model file.
     * @throws ModelException In case retrieving from VCS fails.
     */
    @PostLogging(LoggingEventType.RETRIEVAL)
    @Profiled(tag="modelService.retrieveModelFile")
    byte[] retrieveModelFile(final Model model) throws ModelException {
        final ModelVersion version = getLatestVersion(model)
        if (!version) {
            throw new AccessDeniedException("Sorry you are not allowed to download this Model.")
        }
        return retrieveModelFile(version)
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
        Set<ModelVersion> versions = model.versions
        for (ModelVersion version in versions) {
            if (aclUtilService.hasPermission(springSecurityService.authentication, version, BasePermission.READ) || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')) {
                aclUtilService.addPermission(version, collaborator.username, BasePermission.READ)
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
    @PreAuthorize("hasPermission(#version, delete) or hasRole('ROLE_ADMIN')")
    @PostLogging(LoggingEventType.DELETION)
    @Profiled(tag="modelService.deleteVersion")
    public boolean deleteVersion(ModelVersion version) {
        if (!version) {
            throw new IllegalArgumentException("ModelVersion may not be null")
        }
        if (version.deleted) {
            // revision is already deleted
            return false
        }
        // check if the revision is the latest non-deleted method
        if (version.id != version.model.versions.findAll { !it.deleted }.sort { it.versionNumber }.last().id) {
            // TODO: maybe better throw an exception
            return false
        }
        if (version.model.versions.findAll { !it.deleted }.size() == 1) {
            // only one revision, delete the Model
            // first check the ACL, has to be manual as Spring would not intercept the direct method call
            if (aclUtilService.hasPermission(springSecurityService.authentication, revision.model, BasePermission.DELETE) || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')) {
                deleteModel(version.model)
            } else {
                throw new AccessDeniedException("No permission to delete Model ${version.model.id}")
            }
        }
        // TODO: delete the model if the revision is the first revision of the model
        version.deleted = true
        version.save(flush: true)
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
    @Profiled(tag="modelService.publishModelVersion")
    public void publishModelVersion(ModelVersion version) {
        if (!version) {
            throw new IllegalArgumentException("ModelVersion may not be null")
        }
        if (version.deleted) {
            throw new IllegalArgumentException("Version may not be deleted")
        }
        aclUtilService.addPermission(version, "ROLE_USER", BasePermission.READ)
        aclUtilService.addPermission(version, "ROLE_ANONYMOUS", BasePermission.READ)
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
        ModelVersion version = getLatestVersion(model)
        if (!version) {
            return null
        }
        return modelFileFormatService.getPubMedAnnotation(version)
    }
}
