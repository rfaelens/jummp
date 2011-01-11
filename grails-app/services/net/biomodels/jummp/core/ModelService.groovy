package net.biomodels.jummp.core

import net.biomodels.jummp.model.Model
import net.biomodels.jummp.model.Revision
import org.springframework.security.access.prepost.PostFilter
import org.springframework.security.access.prepost.PreAuthorize
import net.biomodels.jummp.plugins.security.User
import org.springframework.security.acls.domain.BasePermission
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

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
 */
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
    * @return List of Models
    **/
    public List<Model> getAllModels(int offset, int count, boolean sortOrder) {
        // TODO: implement better by going down to database
        List<Model> allModels = Model.list([sort: 'id', order: sortOrder ? 'asc' : 'desc'])
        // first skip all models till offset
        int skipCounter = 0
        int index = 0
        while (skipCounter < offset && index < allModels.size()) {
            Model model = allModels[index++]
            // admin has access to all models
            if (getLatestRevision(model) || SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")) {
                skipCounter++
            }
        }
        // now add each model the user has access to revisions to the return list
        List<Model> returnList = []
        while (returnList.size() < count && index < allModels.size()) {
            Model model = allModels[index++]
            if (getLatestRevision(model) || SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")) {
                returnList << model
            }
        }
        return returnList
    }

    /**
    * Convenient method for ascending sorting.
    *
    * @return List of Models sorted ascending
    * @see getAllModels(int offset, int count, boolean sortOrder)
    **/
    public List<Model> getAllModels(int offset, int count) {
        return getAllModels(offset, count, true)
    }

    /**
    * Convenient method for ascending sorting of first ten models.
    *
    * @return List of first 10 Models sorted ascending
    * @see getAllModels(int offset, int count, boolean sortOrder)
    **/
    public List<Model> getAllModels() {
        return getAllModels(0, 10, true)
    }

    /**
    * Returns the number of Models the user has access to.
    *
    * @see getAllModels
    **/
    public Integer getModelCount() {
        // TODO: implement better by going down to database
        List<Model> models = Model.list()
        int count = 0
        models.each { model ->
            if (getLatestRevision(model) || SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")) {
                count++
            }
        }
        return count
    }

    /**
    * Queries the @p model for the latest available revision the user has read access to.
    * @param model The Model for which the latest revision should be retrieved.
    * @return Latest Revision the current user has read access to. If there is no such revision null is returned
    **/
    public Revision getLatestRevision(Model model) {
        // TODO: maybe querying the database directly is more efficient?
        // we cannot call getAllRevisions as filtering does not work when calling methods directly
        List<Revision> revisions = []
        boolean admin = SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")
        for (revision in model.revisions) {
            if (admin || aclUtilService.hasPermission(springSecurityService.authentication, revision, BasePermission.READ)) {
                revisions << revision
            }
        }
        if (revisions.isEmpty()) {
            return null
        } else {
            revisions = revisions.sort{it.revisionNumber}
            return revisions.last()
        }
    }

    /**
    * Queries the @p model for all revisions the user has read access to.
    * The returned list is ordered by revision number of the model.
    * @param model The Model for which all revisions should be retrieved
    * @return List of Revisions ordered by revision numbers of underlying VCS. If the user has no access to any revision an empty list is returned
    * @todo: add paginated version with offset and count. Problem: filter
    **/
    @PostFilter("hasPermission(filterObject, read) or hasRole('ROLE_ADMIN')")
    public List<Revision> getAllRevisions(Model model) {
        return model.revisions.toList().sort {it.revisionNumber}
    }

    /**
    * Stores a new Model in the VCS.
    *
    * Stores the @p modelFile as a new file in the VCS and creates a Model for it.
    * The Model will have one Revision attached to it. The MetaInformation for this
    * Model is taken from @p meta. The user who uploads the Model becomes the owner of
    * this Model. The new Model is not visible to anyone except the owner.
    * @param modelFile The model file to be stored in the VCS.
    * @param meta Meta Information to be added to the model
    * @return The new created Model, or null if the model could not be created
    **/
    @PreAuthorize("hasRole('ROLE_USER')")
    public Model uploadModel(File modelFile, /*MetaInformationCommand*/def meta) {
        // TODO: implement me
        return null
    }

    /**
    * Adds a new Revision to the model.
    *
    * The provided @p file will be stored in the VCS as an update to an existing file of the same @p model.
    * A new Revision will be created and appended to the list of Revisions of the @p model.
    * @param model The Model the revision should be added
    * @param file The model file to be stored in the VCS as a new revision
    * @param comment The commit message for the new revision
    * @return The new added Revision. In case an error occurred while accessing the VCS @c null will be returned.
    **/
    @PreAuthorize("hasPermission(#model, write) or hasRole('ROLE_ADMIN')")
    public Revision addRevision(Model model, File file, String comment) {
        // TODO: the method should throw exceptions in error cases
        // TODO: the method should be thread safe, add a lock
        // TODO: validate the file
        final User currentUser = User.findByUsername(springSecurityService.authentication.name)
        Revision revision = new Revision(model: model, comment: comment, uploadDate: new Date(), owner: currentUser, minorRevision: false)
        // save the new file in the database
        String vcsId = vcsService.updateFile(file, model.vcsIdentifier, comment)
        if (!vcsId) {
            revision.discard()
            return null
        }
        revision.vcsId = vcsId
        // calculate the new revision number - accessing the revisions directly to circumvent ACL
        revision.revisionNumber = model.revisions.sort {it.revisionNumber}.last().revisionNumber + 1

        if (revision.validate()) {
            model.addToRevisions(revision)
            aclUtilService.addPermission(revision, currentUser.username, BasePermission.ADMINISTRATION)
            aclUtilService.addPermission(revision, currentUser.username, BasePermission.READ)
            aclUtilService.addPermission(revision, currentUser.username, BasePermission.WRITE)
        } else {
            revision.discard()
            revision = null
        }
        return revision
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
    public void grantReadAccess(Model model, User collaborator) {
        // Read access is modeled by adding read access to the model (user will get read access for future revisions)
        // and by adding read access to all revisions the user has access to
        aclUtilService.addPermission(model, collaborator.username, BasePermission.READ)
        List<Revision> revisions = getAllRevisions(model)
        revisions.each { revision ->
            aclUtilService.addPermission(revision, collaborator.username, BasePermission.READ)
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
    * @todo Might be better in a CollaborationService?
    **/
    @PreAuthorize("hasPermission(#model, admin) or hasRole('ROLE_ADMIN')")
    public void revokeReadAccess(Model model, User collaborator) {
        if (collaborator.username == springSecurityService.authentication.name) {
            // the user cannot revoke his own rights
            return
        }
        // check whether the collaborator is admin of the model
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(collaborator.username, "invalid")
        if (!aclUtilService.hasPermission(authentication, model, BasePermission.ADMINISTRATION)) {
            aclUtilService.deletePermission(model, collaborator.username, BasePermission.READ)
            if (aclUtilService.hasPermission(authentication, model, BasePermission.WRITE)) {
                aclUtilService.deletePermission(model, collaborator.username, BasePermission.WRITE)
            }
        }
    }

    /**
    * Revokes write access for @p model from @p collaborator.
    *
    * The @p collaborator gets the right to add revisions to the @p model revoked.
    * The current user can only revoke the right if he has the right to add revisions
    * himself and has the right to grant/revoke write rights on the model
    * @param model The Model for which write access should be revoked
    * @param collaborator The User whose write access should be revoked
    * @todo Might be better in a CollaborationService?
    **/
    @PreAuthorize("hasPermission(#model, admin) or hasRole('ROLE_ADMIN')")
    public void revokeWriteAccess(Model model, User collaborator) {
        if (collaborator.username == springSecurityService.authentication.name) {
            // the user cannot revoke his own rights
            return
        }
        // check whether the collaborator is admin of the model
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(collaborator.username, "invalid")
        if (!aclUtilService.hasPermission(authentication, model, BasePermission.ADMINISTRATION)) {
            aclUtilService.deletePermission(model, collaborator.username, BasePermission.WRITE)
        }
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
    * @see restore
    **/
    @PreAuthorize("hasPermission(#model, delete)")
    public boolean delete(Model model) {
        // TODO: implement me
        return false;
    }

    /**
    * Restores the deleted @p model including all Revisions.
    *
    * Removes the deleted flag from the model and all its Revisions.
    * @param model The deleted Model to restore
    * @see delete
    * @todo might belong in an administration service?
    **/
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void restore(Model model) {
        // TODO: implement me
    }
}
