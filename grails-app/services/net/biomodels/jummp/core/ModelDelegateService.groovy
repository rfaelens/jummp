package net.biomodels.jummp.core

import org.springframework.security.access.AccessDeniedException
import net.biomodels.jummp.core.model.ModelFormatTransportCommand
import net.biomodels.jummp.core.model.ModelListSorting
import net.biomodels.jummp.core.model.ModelTransportCommand
import net.biomodels.jummp.core.model.PublicationTransportCommand
import net.biomodels.jummp.core.model.RevisionTransportCommand
import net.biomodels.jummp.model.Model
import net.biomodels.jummp.model.ModelFormat
import net.biomodels.jummp.model.Revision
import net.biomodels.jummp.plugins.security.User

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

    ModelTransportCommand uploadModel(File modelFile, ModelTransportCommand meta) throws ModelException {
        return modelService.uploadModel(modelFile, meta).toCommandObject()
    }

    RevisionTransportCommand addRevision(long modelId, File file, ModelFormatTransportCommand format, String comment) throws ModelException {
        return modelService.addRevision(Model.get(modelId), file, ModelFormat.findByIdentifier(format.identifier), comment).toCommandObject()
    }

    Boolean canAddRevision(long modelId) {
        return modelService.canAddRevision(Model.get(modelId))
    }

    byte[] retrieveModelFile(RevisionTransportCommand revision) throws ModelException {
        return modelService.retrieveModelFile(Revision.get(revision.id))
    }

    byte[] retrieveModelFile(long modelId) {
        return modelService.retrieveModelFile(Model.get(modelId))
    }

    void grantReadAccess(long modelId, User collaborator) {
        modelService.grantReadAccess(Model.get(modelId), User.get(collaborator.id))
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
}
