package net.biomodels.jummp.core

import org.springframework.security.access.AccessDeniedException
import net.biomodels.jummp.core.model.ModelFormatTransportCommand
import net.biomodels.jummp.core.model.ModelListSorting
import net.biomodels.jummp.core.model.ModelTransportCommand
import net.biomodels.jummp.core.model.PublicationTransportCommand
import net.biomodels.jummp.core.model.RevisionTransportCommand
import net.biomodels.jummp.core.model.RepositoryFileTransportCommand
import net.biomodels.jummp.model.Model
import net.biomodels.jummp.model.ModelFormat
import net.biomodels.jummp.model.Revision
import net.biomodels.jummp.plugins.security.User
import java.util.List
import java.util.Map
import java.lang.ref.ReferenceQueue
import net.biomodels.jummp.core.WrappedRevisionReference

/**
 * @short Service delegating methods to ModelService.
 *
 * This service implements the IModelService interface and gets injected
 * into the remote adapters. The main purpose of this service is to translate
 * the CommandObjects to their respective DOM classes and vice versa. This
 * service should not be used internally in the core. In the core the
 * ModelService should be used directly.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class ModelDelegateService implements IModelService {

    static transactional = true
    def modelService
    def modelFileFormatService

    /*
    * Weak references to revision transport command objects, so that files can be deleted from exchange.
    */
    private final static Map<String,WrappedRevisionReference> weakRefs = new HashMap<String,WrappedRevisionReference>()
    private final static ReferenceQueue referenceQueue = new ReferenceQueue()
    
    String getPluginForFormat(ModelFormatTransportCommand format) {
    	    return modelFileFormatService.getPluginForFormat(format)
    }
    
    /*
    * Functions used by the quartz job for removing files from the exchange
    */
    ReferenceQueue getRefQueue() {
    	    return referenceQueue
    }
    
    void clearReference(String id) {
    	    weakRefs.remove(id)
    }
    
    List<ModelTransportCommand> getAllModels(int offset, int count, boolean sortOrder, ModelListSorting sortColumn) {
        List<ModelTransportCommand> models = []
        modelService.getAllModels(offset, count, sortOrder, sortColumn).each {
            models << it.toCommandObject()
        }
        return models
    }

    List<ModelTransportCommand> getAllModels(int offset, int count, boolean sortOrder) {
        List<ModelTransportCommand> models = []
        modelService.getAllModels(offset, count, sortOrder).each {
            models << it.toCommandObject()
        }
        return models
    }

    List<ModelTransportCommand> getAllModels(int offset, int count, ModelListSorting sortColumn) {
        List<ModelTransportCommand> models = []
        modelService.getAllModels(offset, count, sortColumn).each {
            models << it.toCommandObject()
        }
        return models
    }

    List<ModelTransportCommand> getAllModels(int offset, int count) {
        List<ModelTransportCommand> models = []
        modelService.getAllModels(offset, count).each {
            models << it.toCommandObject()
        }
        return models
    }

    List<ModelTransportCommand> getAllModels(ModelListSorting sortColumn) {
        List<ModelTransportCommand> models = []
        modelService.getAllModels(sortColumn).each {
            models << it.toCommandObject()
        }
        return models
    }

    List<ModelTransportCommand> getAllModels() {
        List<ModelTransportCommand> models = []
        modelService.getAllModels().each {
            models << it.toCommandObject()
        }
        return models
    }

    Integer getModelCount() {
        return modelService.getModelCount()
    }

    ModelTransportCommand getModel(long modelId) {
        return modelService.getModel(modelId).toCommandObject()
    }

    RevisionTransportCommand getLatestRevision(long modelId) {
        Revision rev = modelService.getLatestRevision(Model.get(modelId))
        if (rev) {
            return rev.toCommandObject()
        } else {
            throw new AccessDeniedException("No access to any revision of Model ${modelId}")
        }
    }

    List<RevisionTransportCommand> getAllRevisions(long modelId) {
        List<RevisionTransportCommand> revisions = []
        modelService.getAllRevisions(Model.get(modelId)).each {
            revisions << it.toCommandObject()
        }
        return revisions
    }

    RevisionTransportCommand getRevision(long modelId, int revisionNumber) {
        return modelService.getRevision(Model.get(modelId), revisionNumber).toCommandObject()
    }

    PublicationTransportCommand getPublication(long modelId) throws AccessDeniedException, IllegalArgumentException {
        return modelService.getPublication(Model.get(modelId))?.toCommandObject()
    }

    ModelTransportCommand uploadModel(List<File> modelFiles, ModelTransportCommand meta) throws ModelException {
        return modelService.uploadModelAsList(modelFiles, meta).toCommandObject()
    }

    RevisionTransportCommand addRevision(long modelId, File file, ModelFormatTransportCommand format, String comment) throws ModelException {
        return modelService.addRevision(Model.get(modelId), file,
                ModelFormat.findByIdentifierAndFormatVersion(format.identifier, format.formatVersion), comment).toCommandObject()
    }

    Boolean canAddRevision(long modelId) {
        return modelService.canAddRevision(Model.get(modelId))
    }

    List<RepositoryFileTransportCommand> retrieveModelFiles(RevisionTransportCommand revision) throws ModelException {
        List<RepositoryFileTransportCommand> files=modelService.retrieveModelFiles(Revision.get(revision.id))
        if (files && !files.isEmpty()) {
        	files.each {
        		it.revision=revision
        	}
        	/*
        	* Add revision to the weak reference data structures, so its files are released from disk.
        	*/
        	String folder=(new File(files.first().path)).getParent()
        	WrappedRevisionReference ref=new WrappedRevisionReference(revision, folder, referenceQueue)
        	weakRefs.put(folder,ref)
        }
        return files
    }

    List<RepositoryFileTransportCommand> retrieveModelFiles(long modelId) {
        return modelService.retrieveModelFiles(Model.get(modelId))
    }

    void grantReadAccess(long modelId, User collaborator) {
        modelService.grantReadAccess(Model.get(modelId), User.get(collaborator.id))
    }
    
    String getSearchIndexingContent(RevisionTransportCommand revision) {
    	    modelService.getSearchIndexingContent(revision)
    }

    void grantWriteAccess(long modelId, User collaborator) {
        modelService.grantWriteAccess(Model.get(modelId), User.get(collaborator.id))
    }

    boolean revokeReadAccess(long modelId, User collaborator) {
        return modelService.revokeReadAccess(Model.get(modelId), User.get(collaborator.id))
    }

    boolean revokeWriteAccess(long modelId, User collaborator) {
        return modelService.revokeWriteAccess(Model.get(modelId), User.get(collaborator.id))
    }

    void transferOwnerShip(long modelId, User collaborator) {
        modelService.transferOwnerShip(Model.get(modelId), User.get(collaborator.id))
    }

    boolean deleteModel(long modelId) {
        return modelService.deleteModel(Model.get(modelId))
    }

    boolean restoreModel(long modelId) {
        return modelService.restoreModel(Model.get(modelId))
    }

    boolean deleteRevision(RevisionTransportCommand revision) {
        return modelService.deleteRevision(Revision.get(revision.id))
    }

    void publishModelRevision(RevisionTransportCommand revision) {
        modelService.publishModelRevision(Revision.get(revision.id))
    }
}
