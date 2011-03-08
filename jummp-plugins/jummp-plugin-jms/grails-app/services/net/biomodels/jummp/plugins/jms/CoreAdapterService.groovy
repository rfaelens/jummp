package net.biomodels.jummp.plugins.jms

import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import net.biomodels.jummp.core.JummpException
import net.biomodels.jummp.core.ModelException
import net.biomodels.jummp.core.model.ModelFormatTransportCommand
import net.biomodels.jummp.core.model.ModelTransportCommand
import net.biomodels.jummp.core.model.RevisionTransportCommand
import net.biomodels.jummp.core.model.ModelListSorting
import net.biomodels.jummp.core.model.PublicationTransportCommand
import net.biomodels.jummp.plugins.security.User
import org.perf4j.aop.Profiled
import org.springframework.security.authentication.BadCredentialsException

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
    @Profiled(tag="coreAdapterService.authenticate")
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
    * @param sort The column to use for sorting
    * @return List of Models
    **/
    @Profiled(tag="coreAdapterService.getAllModels")
    public List<ModelTransportCommand> getAllModels(int offset, int count, boolean sortOrder, ModelListSorting sort) {
        def retVal = send("getAllModels", [offset, count, sortOrder, sort])
        validateReturnValue(retVal, List)
        return (List)retVal
    }

    /**
     * Convenient method for specifying the sort order.
     * @return List of Models
     */
    @Profiled(tag="coreAdapterService.getAllModels")
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
    @Profiled(tag="coreAdapterService.getAllModels")
    public List<ModelTransportCommand> getAllModels(int offset, int count, ModelListSorting sort) {
        def retVal = send("getAllModels", [offset, count, sort])
        validateReturnValue(retVal, List)
        return (List)retVal
    }

    /**
    * Convenient method for ascending sorting by id.
    *
    * @return List of Models sorted ascending by id
    * @see getAllModels(int offset, int count, boolean sortOrder)
    **/
    @Profiled(tag="coreAdapterService.getAllModels")
    public List<ModelTransportCommand> getAllModels(int offset, int count) {
        def retVal = send("getAllModels", [offset, count])
        validateReturnValue(retVal, List)
        return (List)retVal
    }

    /**
    * Convenient method for ascending sorting of first ten models
    *
    * @return List of first 10 Models sorted ascending by id
    * @see getAllModels(int offset, int count, boolean sortOrder)
    **/
    @Profiled(tag="coreAdapterService.getAllModels")
    public List<ModelTransportCommand> getAllModels(ModelListSorting sort) {
        def retVal = send("getAllModels", [sort])
        validateReturnValue(retVal, List)
        return (List)retVal
    }

    /**
    * Convenient method for ascending sorting of first ten models ordered by Id column.
    *
    * @return List of first 10 Models sorted ascending by id
    * @see getAllModels(int offset, int count, boolean sortOrder)
    **/
    @Profiled(tag="coreAdapterService.getAllModels")
    public List<ModelTransportCommand> getAllModels() {
        return getAllModels(ModelListSorting.ID)
    }

    /**
    * Returns the number of Models the user has access to.
    *
    * @see getAllModels
    **/
    @Profiled(tag="coreAdapterService.getModelCount")
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
    @Profiled(tag="coreAdapterService.getLatestRevision")
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
    @Profiled(tag="coreAdapterService.getAllRevisions")
    public List<RevisionTransportCommand> getAllRevisions(ModelTransportCommand model) {
        def retVal = send("getAllRevisions", model)
        validateReturnValue(retVal, List)
        return (List<RevisionTransportCommand>)retVal
    }

    /**
     * Returns the reference publication of this @p model.
     * @param model The Model for which the reference publication should be returned.
     * @return The reference publication
     */
    @Profiled(tag="coreAdapterService.getPublication")
    public PublicationTransportCommand getPublication(final ModelTransportCommand model) {
        def retVal = send("getPublication", model)
        validateReturnValue(retVal, PublicationTransportCommand)
        return (PublicationTransportCommand)retVal
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
    @Profiled(tag="coreAdapterService.uploadModel")
    public ModelTransportCommand uploadModel(byte[] bytes, ModelTransportCommand meta) throws ModelException {
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
    @Profiled(tag="coreAdapterService.addRevision")
    public RevisionTransportCommand addRevision(ModelTransportCommand model, byte[] bytes, ModelFormatTransportCommand format, String comment) throws ModelException {
        def retVal = send("addRevision", [model, bytes, format, comment])
        validateReturnValue(retVal, RevisionTransportCommand)
        return (RevisionTransportCommand)retVal
    }

    /**
     * Returns whether the current user has the right to add a revision to the model.
     * @param model The model to check
     * @return @c true if the user has write permission on the revision or is an admin user, @c false otherwise.
     */
    @Profiled(tag="coreAdapterService.canAddRevision")
    public Boolean canAddRevision(final ModelTransportCommand model) {
        def retVal = send("canAddRevision", model)
        validateReturnValue(retVal, Boolean)
        return (Boolean)retVal
    }

    /**
     * Retrieves the model file for the @p revision.
     * @param revision The Model Revision for which the file should be retrieved.
     * @return Byte Array of the content of the Model file for the revision.
     * @throws ModelException In case retrieving from VCS fails.
     */
    @Profiled(tag="coreAdapterService.retrieveModelFile")
    public byte[] retrieveModelFile(RevisionTransportCommand revision) {
        // TODO: verify closely because of byte[]
        def retVal = send("retrieveModelFile", revision)
        validateReturnValue(retVal, byte[].class)
        return (byte[])retVal
    }

    /**
     * Retrieves the model file for the latest revision of the @p model
     * @param model The Model for which the file should be retrieved
     * @return Byte Array of the content of the Model file.
     * @throws ModelException In case retrieving from VCS fails.
     */
    @Profiled(tag="coreAdapterService.retrieveModelFile")
    public byte[] retrieveModelFile(ModelTransportCommand model) {
        // TODO: verify closely because of byte[]
        def retVal = send("retrieveModelFile", model)
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
    @Profiled(tag="coreAdapterService.deleteModel")
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
    @Profiled(tag="coreAdapterService.restoreModel")
    public Boolean restoreModel(ModelTransportCommand model) {
        def retVal = send("restoreModel", model)
        validateReturnValue(retVal, Boolean)
        return (Boolean)retVal
    }

    /**
     * Changes the password of the currently logged in user.
     * @param oldPassword The old password for verification
     * @param newPassword The new password to be used
     * @throws BadCredentialsException if @p oldPassword is incorrect
     * @todo Maybe better in an own service?
     */
    @Profiled(tag="coreAdapterService.changePassword")
    public void changePassword(String oldPassword, String newPassword) throws BadCredentialsException {
        validateReturnValue(send("changePassword", [oldPassword, newPassword]), Boolean)
    }

    /**
     * Edit the non-security related parts of a user.
     * @param user The User with the updated fields
     */
    @Profiled(tag="coreAdapterService.editUser")
    public void editUser(User user) {
        validateReturnValue(send("editUser", user), Boolean)
    }

    /**
     *
     * @return The current (security sanitized) user
     */
    @Profiled(tag="coreAdapterService.getCurrentUser")
    public User getCurrentUser() {
        def retVal = send("getCurrentUser")
        validateReturnValue(retVal, User)
        return (User)retVal
    }

    /**
     * Retrieves a User object for the given @p username.
     * @param username The login identifier of the user to be retrieved
     * @returnThe (security sanitized) user
     * @throws IllegalArgumentException Thrown if there is no User for @p username
     */
    @Profiled(tag="coreAdapterService.getUser")
    public User getUser(String username) throws IllegalArgumentException {
        def retVal = send("getUser", username)
        validateReturnValue(retVal, User)
        return (User)retVal
    }

    /**
     * Retrieves list of users.
     * This method is only for administrative purpose. It does not sanitize the
     * returned Users, that is it includes all (also security relevant) elements.
     * The method only exists in a paginated version
     * @param offset Offset in the list
     * @param count Number of Users to return, Maximum is 100
     * @return List of Users ordered by Id
     */
    @Profiled(tag="coreAdapterService.getUser")
    public List<User> getAllUsers(Integer offset, Integer count) {
        def retVal = send("getAllUsers", [offset, count])
        validateReturnValue(retVal, List)
        return (List<User>)retVal
    }

    /**
     * Enables/Disables the user identified by @p userId
     * @param userId The unique id of the user
     * @param enable if @c true the user is enabled, if @c false the user is disabled
     * @return @c true, if the enable state was changed, @c false if the user was already in @p enable state
     * @throws IllegalArgumentException If the user specified by @p userId does not exist
     */
    @Profiled(tag="coreAdapterService.enableUser")
    Boolean enableUser(Long userId, Boolean enable) throws IllegalArgumentException {
        def retVal = send("enableUser", [userId, enable])
        validateReturnValue(retVal, Boolean)
        return (Boolean)retVal
    }

    /**
     * (Un)Locks the account for user identified by @p userId
     * @param userId The unique id of the user
     * @param lock if @c true the account is locked, if @c false the account is unlocked
     * @return @c true, if the account locked state was changed, @c false if the user was already in @p lock state
     * @throws IllegalArgumentException If the user specified by @p userId does not exist
     */
    @Profiled(tag="coreAdapterService.lockAccount")
    Boolean lockAccount(Long userId, Boolean lock) throws IllegalArgumentException {
        def retVal = send("lockAccount", [userId, lock])
        validateReturnValue(retVal, Boolean)
        return (Boolean)retVal
    }

    /**
     * (Un)Expires the account for user identified by @p userId
     * @param userId The unique id of the user
     * @param expire if @c true the account is expired, if @c false the account is un-expired
     * @return @c true, if the account expired state was changed, @c false if the user was already in @p expire state
     * @throws IllegalArgumentException If the user specified by @p userId does not exist
     */
    @Profiled(tag="coreAdapterService.expireAccount")
    Boolean expireAccount(Long userId, Boolean expire) throws IllegalArgumentException {
        def retVal = send("expireAccount", [userId, expire])
        validateReturnValue(retVal, Boolean)
        return (Boolean)retVal
    }

    /**
     * (Un)Expires the password for user identified by @p userId
     * @param userId The unique id of the user
     * @param expire if @c true the password is expired, if @c false the password is un-expired
     * @return @c true, if the password expired state was changed, @c false if the password was already in @p expire state
     * @throws IllegalArgumentException If the user specified by @p userId does not exist
     */
    @Profiled(tag="coreAdapterService.expirePassword")
    Boolean expirePassword(Long userId, Boolean expire) throws IllegalArgumentException {
        def retVal = send("expirePassword", [userId, expire])
        validateReturnValue(retVal, Boolean)
        return (Boolean)retVal
    }

    /**
     * Registers a new User.
     * @param user The new User to register
     * @throws JummpException Thrown in case the user could not be registered
     */
    @Profiled(tag="coreAdapterService.register")
    void register(User user) throws JummpException {
        validateReturnValue(send("register", user), Boolean)
    }

    /**
     * Validates the registration code of a new user.
     * @param username The name of the new user
     * @param code The validation code
     * @throws JummpException Thrown in case that the validation cannot be performed
     */
    @Profiled(tag="coreAdapterService.register")
    void validateRegistration(String username, String code) throws JummpException {
        validateReturnValue(send("validateRegistration", [username, code]), Boolean)
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
