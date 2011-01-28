package net.biomodels.jummp.plugins.jms

import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import net.biomodels.jummp.core.JummpException
import net.biomodels.jummp.core.model.ModelFormatTransportCommand
import net.biomodels.jummp.core.model.ModelTransportCommand
import net.biomodels.jummp.core.model.RevisionTransportCommand

/**
 * @short Service connecting to the core via synchronous JMS.
 *
 * This service can be used to connect to the core web application through JMS from
 * a different component such as the entry-point web application or web services.
 *
 * All methods are executed with synchronous JMS and it's taken care of the special situations.
 * This means the current Authentication is wrapped into each call and the return value is verified.
 * If an unexpected null value or an Exception is returned, an Exception will be re-thrown to be handled
 * by the application.
 *
 * The service provides access to all methods exported by the cores JmsAdapterService. All returned objects
 * are de-coupled from the database and any changes to the objects are not stored in the database. The appropriate
 * methods of this adapter have to be called to update objects in the database (and by that ensuring that the
 * business logic is used).
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class CoreAdapterService {
    def jmsSynchronousService

    static transactional = false

    /**
     * Tries to authenticate the given @p Authentication in the core.
     * @param authentication The Authentication to test. In most cases a UsernamePasswordAuthenticationToken
     * @return An authenticated user
     * @throws AuthenticationException If the Authentication is not valid
     * @throws JummpException If an error occurred
     */
    Authentication authenticate(Authentication authentication) throws AuthenticationException, JummpException {
        def retVal = send("authenticate", authentication, false)
        validateReturnValue(retVal, Authentication)
        return (Authentication)retVal
    }

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
    public List<ModelTransportCommand> getAllModels(int offset, int count, boolean sortOrder) {
        def retVal = send("getAllModels", [offset, count, sortOrder])
        validateReturnValue(retVal, List)
        return (List)retVal
    }

    /**
    * Convenient method for ascending sorting.
    *
    * @return List of Models sorted ascending
    * @see getAllModels(int offset, int count, boolean sortOrder)
    **/
    public List<ModelTransportCommand> getAllModels(int offset, int count) {
        return getAllModels(offset, count, true)
    }

    /**
    * Convenient method for ascending sorting of first ten models.
    *
    * @return List of first 10 Models sorted ascending
    * @see getAllModels(int offset, int count, boolean sortOrder)
    **/
    public List<ModelTransportCommand> getAllModels() {
        return getAllModels(0, 10, true)
    }

    /**
    * Returns the number of Models the user has access to.
    *
    * @see getAllModels
    **/
    public Integer getModelCount() {
        def retVal = send("getModelCount")
        validateReturnValue(retVal, Integer)
        return (Integer)retVal
    }

    /**
    * Queries the @p model for the latest available revision the user has read access to.
    * @param model The Model for which the latest revision should be retrieved.
    * @return Latest Revision the current user has read access to. 
    **/
    public RevisionTransportCommand getLatestRevision(ModelTransportCommand model) {
        def retVal = send("getLatestRevision", model)
        validateReturnValue(retVal, RevisionTransportCommand)
        return (RevisionTransportCommand)retVal
    }

    /**
    * Queries the @p model for all revisions the user has read access to.
    * The returned list is ordered by revision number of the model.
    * @param model The Model for which all revisions should be retrieved
    * @return List of Revisions ordered by revision numbers of underlying VCS. If the user has no access to any revision an empty list is returned
    **/
    public List<RevisionTransportCommand> getAllRevisions(ModelTransportCommand model) {
        def retVal = send("getAllRevisions", model)
        validateReturnValue(retVal, List)
        return (List<RevisionTransportCommand>)retVal
    }

    /**
    * Creates a new Model and stores it in the VCS.
    *
    * Stores the @p bytes as a new file in the VCS and creates a Model for it.
    * The Model will have one Revision attached to it. The MetaInformation for this
    * Model is taken from @p meta. The user who uploads the Model becomes the owner of
    * this Model. The new Model is not visible to anyone except the owner.
    * @param modelFile The model file to be stored in the VCS.
    * @param meta Meta Information to be added to the model
    * @return The new created Model as a ModelTransportCommand
    **/
    public ModelTransportCommand uploadModel(byte[] bytes, ModelTransportCommand meta) {
        def retVal = send("uploadModel", [bytes, meta])
        validateReturnValue(retVal, ModelTransportCommand)
        return (ModelTransportCommand)retVal
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
    **/
    public RevisionTransportCommand addRevision(ModelTransportCommand model, byte[] bytes, ModelFormatTransportCommand format, String comment) {
        def retVal = send("addRevision", [model, bytes, format, comment])
        validateReturnValue(retVal, RevisionTransportCommand)
        return (RevisionTransportCommand)retVal
    }

    /**
     * Retrieves the model file for the @p revision.
     * @param revision The Model Revision for which the file should be retrieved.
     * @return Byte Array of the content of the Model file for the revision.
     * @throws ModelException In case retrieving from VCS fails.
     */
    public byte[] retrieveModelFile(RevisionTransportCommand revision) {
        // TODO: verify closely because of byte[]
        def retVal = send("retrieveModelFile", revision)
        validateReturnValue(retVal, byte[].class)
        return (byte[])retVal
    }

    // TODO: how to handle grant/revoke rights from the webapplication? Which users to show?
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
    * @see restoreModel
    **/
    public Boolean deleteModel(ModelTransportCommand model) {
        def retVal = send("deleteModel", model)
        validateReturnValue(retVal, Boolean)
        return (Boolean)retVal
    }

    /**
    * Restores the deleted @p model.
    *
    * Removes the deleted flag from the model and all its Revisions.
    * @param model The deleted Model to restore
    * @return @c true, whether the state was restored, @c false otherwise.
    * @see deleteModel
    **/
    public Boolean restoreModel(ModelTransportCommand model) {
        def retVal = send("restoreModel", model)
        validateReturnValue(retVal, Boolean)
        return (Boolean)retVal
    }

    /**
     * Validates the @p retVal. In case of a @c null value an JummpException is thrown, in case the
     * value is an Exception itself, the Exception gets re-thrown, in case the value is not an instance
     * of @p expectedType an JummpException is thrown.
     * @param retVal The return value to validate
     * @param expectedType The expected type of the value
     * @throws JummpException In case of @p retVal being @c null or not the expected type
     */
    private void validateReturnValue(def retVal, Class expectedType) throws JummpException {
        if (retVal == null) {
            log.error("Received null value from core.")
            throw new JummpException("Received a null value from core")
        }
        if (retVal instanceof Exception) {
            throw retVal
        }
        if (!expectedType.isInstance(retVal)) {
            throw new JummpException("Expected a value of type ${expectedType.toString()} but received ${retVal.class}")
        }
    }

    /**
     * Convenient overwrite for case of no arguments except Authentication
     * @param method  The name of the method to invoke
     * @return Whatever the core returns
     */
    private def send(String method) {
        return send(method, null, true)
    }

    /**
     * Convenient overwrite to default to authenticate
     * @param method The name of the method to invoke
     * @param message The arguments which are expected
     * @return Whatever the core returns
     */
    private def send(String method, def message) {
        return send(method, message, true)
    }

    /**
     * Helper method to send a JMS message to core.
     * @param method The name of the method to invoke
     * @param message The arguments which are expected
     * @param authenticated Whether the Authentication should be prepended to the message
     * @return Whatever the core returns
     */
    private def send(String method, def message, boolean authenticated) {
        if (authenticated && message) {
            Authentication auth = SecurityContextHolder.context.authentication
            if (message instanceof List) {
                ((List)message).add(0, auth)
            } else {
                message = [auth, message]
            }
        } else if (authenticated && !message) {
            message = SecurityContextHolder.context.authentication
        }
        return jmsSynchronousService.send([app: "jummp", service: "jmsAdapter", method: method],message, [service: "jmsAdapter", method: "${method}.response"])
    }
}
