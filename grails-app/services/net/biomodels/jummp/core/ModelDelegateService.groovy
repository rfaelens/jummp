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

    RevisionTransportCommand getLatestRevision(ModelTransportCommand model) {
        Revision rev = modelService.getLatestRevision(Model.get(model.id))
        if (rev) {
            return rev.toCommandObject()
        } else {
            throw new AccessDeniedException("No access to any revision of Model ${model.id}")
        }
    }

    List<RevisionTransportCommand> getAllRevisions(ModelTransportCommand model) {
        List<RevisionTransportCommand> revisions = []
        modelService.getAllRevisions(Model.get(model.id)).each {
            revisions << it.toCommandObject()
        }
        return revisions
    }

    PublicationTransportCommand getPublication(ModelTransportCommand model) {
        return modelService.getPublication(Model.get(model.id)).toCommandObject()
    }

    ModelTransportCommand uploadModel(File modelFile, ModelTransportCommand meta) throws ModelException {
        return modelService.uploadModel(modelFile, meta).toCommandObject()
    }

    RevisionTransportCommand addRevision(ModelTransportCommand model, File file, ModelFormatTransportCommand format, String comment) throws ModelException {
        return modelService.addRevision(Model.get(model.id), file, ModelFormat.findByIdentifier(format.identifier), comment).toCommandObject()
    }

    Boolean canAddRevision(ModelTransportCommand model) {
        return modelService.canAddRevision(Model.get(model.id))
    }

    byte[] retrieveModelFile(RevisionTransportCommand revision) throws ModelException {
        return modelService.retrieveModelFile(Revision.get(revision.id))
    }

    byte[] retrieveModelFile(ModelTransportCommand model) {
        return modelService.retrieveModelFile(Model.get(model.id))
    }

    void grantReadAccess(ModelTransportCommand model, User collaborator) {
        modelService.grantReadAccess(Model.get(model.id), User.get(collaborator.id))
    }

    void grantWriteAccess(ModelTransportCommand model, User collaborator) {
        modelService.grantWriteAccess(Model.get(model.id), User.get(collaborator.id))
    }

    boolean revokeReadAccess(ModelTransportCommand model, User collaborator) {
        return modelService.revokeReadAccess(Model.get(model.id), User.get(collaborator.id))
    }

    boolean revokeWriteAccess(ModelTransportCommand model, User collaborator) {
        return modelService.revokeWriteAccess(Model.get(model.id), User.get(collaborator.id))
    }

    void transferOwnerShip(ModelTransportCommand model, User collaborator) {
        modelService.transferOwnerShip(Model.get(model.id), User.get(collaborator.id))
    }

    boolean deleteModel(ModelTransportCommand model) {
        return modelService.deleteModel(Model.get(model.id))
    }

    boolean restoreModel(ModelTransportCommand model) {
        return modelService.restoreModel(Model.get(model.id))
    }
}
