package net.biomodels.jummp.plugins.jms

import org.perf4j.aop.Profiled
import net.biomodels.jummp.core.ModelException
import net.biomodels.jummp.core.model.ModelFormatTransportCommand
import net.biomodels.jummp.core.model.ModelListSorting
import net.biomodels.jummp.core.model.ModelTransportCommand
import net.biomodels.jummp.core.model.PublicationTransportCommand
import net.biomodels.jummp.core.model.RevisionTransportCommand

/**
 * @short Service delegating to ModelService of the core via synchronous JMS
 *
 * This service communicates with ModelJmsAdapterService in core through JMS. The
 * service takes care of wrapping parameters into messages and evaluating returned
 * values.
 *
 * Any other Grails artifact can use this service as any other service. The fact that
 * it uses JMS internally is completely transparent to the users of this service.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class ModelAdapterService extends AbstractJmsRemoteAdapter {

    static transactional = false
    private static final String ADAPTER_SERVICE_NAME = "modelJmsAdapter"

    protected String getAdapterServiceName() {
        return ADAPTER_SERVICE_NAME
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
    @Profiled(tag="modelAdapterService.getAllModels")
    public List<ModelTransportCommand> getAllModels(int offset, int count, boolean sortOrder, ModelListSorting sort) {
        def retVal = send("getAllModels", [offset, count, sortOrder, sort])
        validateReturnValue(retVal, List)
        return (List)retVal
    }

    /**
     * Convenient method for specifying the sort order.
     * @return List of Models
     */
    @Profiled(tag="modelAdapterService.getAllModels")
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
    @Profiled(tag="modelAdapterService.getAllModels")
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
    @Profiled(tag="modelAdapterService.getAllModels")
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
    @Profiled(tag="modelAdapterService.getAllModels")
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
    @Profiled(tag="modelAdapterService.getAllModels")
    public List<ModelTransportCommand> getAllModels() {
        return getAllModels(ModelListSorting.ID)
    }

    /**
    * Returns the number of Models the user has access to.
    *
    * @see getAllModels
    **/
    @Profiled(tag="modelAdapterService.getModelCount")
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
    @Profiled(tag="modelAdapterService.getLatestRevision")
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
    @Profiled(tag="modelAdapterService.getAllRevisions")
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
    @Profiled(tag="modelAdapterService.getPublication")
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
    @Profiled(tag="modelAdapterService.uploadModel")
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
    @Profiled(tag="modelAdapterService.addRevision")
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
    @Profiled(tag="modelAdapterService.canAddRevision")
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
    @Profiled(tag="modelAdapterService.retrieveModelFile")
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
    @Profiled(tag="modelAdapterService.retrieveModelFile")
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
    @Profiled(tag="modelAdapterService.deleteModel")
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
    @Profiled(tag="modelAdapterService.restoreModel")
    public Boolean restoreModel(ModelTransportCommand model) {
        def retVal = send("restoreModel", model)
        validateReturnValue(retVal, Boolean)
        return (Boolean)retVal
    }

}
