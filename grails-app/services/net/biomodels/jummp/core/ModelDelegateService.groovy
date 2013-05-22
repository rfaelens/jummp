package net.biomodels.jummp.core

import org.springframework.security.access.AccessDeniedException
import net.biomodels.jummp.core.model.ModelFormatTransportCommand
import net.biomodels.jummp.core.model.ModelListSorting
import net.biomodels.jummp.core.model.ModelTransportCommand
import net.biomodels.jummp.core.model.PublicationTransportCommand
import net.biomodels.jummp.core.model.ModelVersionTransportCommand
import net.biomodels.jummp.model.Model
import net.biomodels.jummp.model.ModelFormat
import net.biomodels.jummp.model.ModelVersion
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

    ModelVersionTransportCommand getLatestVersion(long modelId) {
        ModelVersion ver = modelService.getLatestVersion(Model.get(modelId))
        if (ver) {
            return ver.toCommandObject()
        } else {
            throw new AccessDeniedException("No access to any version of Model ${modelId}")
        }
    }

    List<ModelVersionTransportCommand> getAllVersions(long modelId) {
        List<ModelVersionTransportCommand> versions = []
        modelService.getAllVersions(Model.get(modelId)).each {
            versions << it.toCommandObject()
        }
        return versions
    }

    ModelVersionTransportCommand getVersion(long modelId, int versionNumber) {
        return modelService.getVersion(Model.get(modelId), versionNumber).toCommandObject()
    }

    PublicationTransportCommand getPublication(long modelId) throws AccessDeniedException, IllegalArgumentException {
        return modelService.getPublication(Model.get(modelId))?.toCommandObject()
    }

    ModelTransportCommand uploadModel(File modelFile, ModelTransportCommand meta) throws ModelException {
        return modelService.uploadModel(modelFile, meta).toCommandObject()
    }

    ModelVersionTransportCommand addVersion(long modelId, File file, ModelFormatTransportCommand format, String comment) throws ModelException {
        return modelService.addVersion(Model.get(modelId), file, ModelFormat.findByIdentifier(format.identifier), comment).toCommandObject()
    }

    Boolean canAddVersion(long modelId) {
        return modelService.canAddVersion(Model.get(modelId))
    }

    byte[] retrieveModelFile(ModelVersionTransportCommand version) throws ModelException {
        return modelService.retrieveModelFile(ModelVersion.get(version.id))
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

    boolean deleteVersion(ModelVersionTransportCommand version) {
        return modelService.deleteVersion(ModelVersion.get(version.id))
    }

    void publishModelVersion(ModelVersionTransportCommand version) {
        modelService.publishModelVersion(ModelVersion.get(version.id))
    }
}
