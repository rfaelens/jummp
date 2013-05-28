package net.biomodels.jummp.remote

import net.biomodels.jummp.core.model.ModelTransportCommand
import net.biomodels.jummp.core.model.ModelListSorting
import net.biomodels.jummp.core.model.RevisionTransportCommand
import net.biomodels.jummp.core.model.PublicationTransportCommand
import net.biomodels.jummp.core.ModelException
import net.biomodels.jummp.core.model.ModelFormatTransportCommand
import java.util.List

/**
 * @short Interface describing how to access the remote Model Service.
 *
 * This interface defines all the methods which have to be implemented by a remote
 * adapter to the core's ModelService.
 *
 * Each remote adapter exporting the ModelService needs to implement this interface.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
public interface RemoteModelAdapter {

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
    public List<ModelTransportCommand> getAllModels(int offset, int count, boolean sortOrder, ModelListSorting sort)
    /**
     * Convenient method for specifying the sort order.
     * @return List of Models
     */
    public List<ModelTransportCommand> getAllModels(int offset, int count, boolean sortOrder)
    /**
    * Convenient method for ascending sorting.
    *
    * @return List of Models sorted ascending
    * @see getAllModels(int offset, int count, boolean sortOrder)
    **/
    public List<ModelTransportCommand> getAllModels(int offset, int count, ModelListSorting sort)
    /**
    * Convenient method for ascending sorting by id.
    *
    * @return List of Models sorted ascending by id
    * @see getAllModels(int offset, int count, boolean sortOrder)
    **/
    public List<ModelTransportCommand> getAllModels(int offset, int count)
    /**
    * Convenient method for ascending sorting of first ten models
    *
    * @return List of first 10 Models sorted ascending by id
    * @see getAllModels(int offset, int count, boolean sortOrder)
    **/
    public List<ModelTransportCommand> getAllModels(ModelListSorting sort)
    /**
    * Convenient method for ascending sorting of first ten models ordered by Id column.
    *
    * @return List of first 10 Models sorted ascending by id
    * @see getAllModels(int offset, int count, boolean sortOrder)
    **/
    public List<ModelTransportCommand> getAllModels()
    /**
    * Returns the number of Models the user has access to.
    *
    * @see getAllModels
    **/
    public Integer getModelCount()
    /**
    * Queries the model for the latest available revision the user has read access to.
    * @param modelId The Model for which the latest revision should be retrieved.
    * @return Latest Revision the current user has read access to.
    **/
    public RevisionTransportCommand getLatestRevision(long modelId)
    /**
     * Queries the model for the revision identified with the specified revisionNumber.
     * @param modelId The Model fro which the revision should be retrieved.
     * @param revisionNumber The revision number in context to the model
     * @return The Revision
     */
    public RevisionTransportCommand getRevision(long modelId, int revisionNumber)
    /**
    * Queries the model for all revisions the user has read access to.
    * The returned list is ordered by revision number of the model.
    * @param modelId The id of the Model for which all revisions should be retrieved
    * @return List of Revisions ordered by revision numbers of underlying VCS. If the user has no access to any revision an empty list is returned
    **/
    public List<RevisionTransportCommand> getAllRevisions(long modelId)
    /**
     * Returns the reference publication of this model.
     * @param modelId The id of the Model for which the reference publication should be returned.
     * @return The reference publication
     */
    public PublicationTransportCommand getPublication(final long modelId)
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
    public ModelTransportCommand uploadModel(List<byte[]> bytes, ModelTransportCommand meta) throws ModelException
    /**
    * Adds a new Revision to the model.
    *
    * The provided @p file will be stored in the VCS as an update to an existing file of the same @p model.
    * A new Revision will be created and appended to the list of Revisions of the @p model.
    * @param modelId The id of the Model the revision should be added
    * @param file The model file to be stored in the VCS as a new revision
    * @param format The format of the model file
    * @param comment The commit message for the new revision
    * @return The new added Revision. In case an error occurred while accessing the VCS @c null will be returned.
    **/
    public RevisionTransportCommand addRevision(long modelId, byte[] bytes, ModelFormatTransportCommand format, String comment) throws ModelException
    /**
     * Returns whether the current user has the right to add a revision to the model.
     * @param modelId The id of the model to check
     * @return @c true if the user has write permission on the revision or is an admin user, @c false otherwise.
     */
    public Boolean canAddRevision(final long modelId)
    /**
     * Retrieves the model file for the @p revision.
     * @param revision The Model Revision for which the file should be retrieved.
     * @return Byte Array of the content of the Model file for the revision.
     * @throws ModelException In case retrieving from VCS fails.
     */
    public byte[] retrieveModelFile(RevisionTransportCommand revision)
    /**
     * Retrieves the model file for the latest revision of the model.
     * @param modelId The Model for which the file should be retrieved
     * @return Byte Array of the content of the Model file.
     * @throws ModelException In case retrieving from VCS fails.
     */
    public byte[] retrieveModelFile(long modelId)
    /**
    * Deletes the model including all Revisions.
    *
    * Flags the model and all its revisions as deleted. A deletion from VCS is for
    * technical reasons not possible and because of that a deletion of the Model object
    * is not possible.
    *
    * Deletion of model is only possible if the model is neither under curation nor published.
    * @param modelId The id of the Model to be deleted
    * @return @c true in case the Model has been deleted, @c false otherwise.
    * @see restoreModel
    **/
    public Boolean deleteModel(long modelId)
    /**
    * Restores the deleted model.
    *
    * Removes the deleted flag from the model and all its Revisions.
    * @param modelId The id of the deleted Model to restore
    * @return @c true, whether the state was restored, @c false otherwise.
    * @see deleteModel
    **/
    public Boolean restoreModel(long modelId)
    public Boolean deleteRevision(long modelId, int revisionNumber)
    public void publishModelRevision(long modelId, int revisionNumber)
}
