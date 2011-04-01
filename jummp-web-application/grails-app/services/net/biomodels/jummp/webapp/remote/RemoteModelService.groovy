package net.biomodels.jummp.webapp.remote

import net.biomodels.jummp.remote.RemoteModelAdapter
import net.biomodels.jummp.core.model.ModelTransportCommand
import net.biomodels.jummp.core.model.ModelListSorting
import net.biomodels.jummp.core.model.RevisionTransportCommand
import net.biomodels.jummp.core.model.PublicationTransportCommand
import net.biomodels.jummp.core.model.ModelFormatTransportCommand
import org.perf4j.aop.Profiled
import net.biomodels.jummp.core.ModelException

class RemoteModelService implements RemoteModelAdapter {

    static transactional = true
    RemoteModelAdapter remoteModelAdapter

    @Profiled(tag="RemoteModelService.getAllModels")
    def List<ModelTransportCommand> getAllModels(int offset, int count, boolean sortOrder, ModelListSorting sort) {
        return remoteModelAdapter.getAllModels(offset, count, sortOrder, sort)
    }

    @Profiled(tag="RemoteModelService.getAllModels")
    def List<ModelTransportCommand> getAllModels(int offset, int count, boolean sortOrder) {
        return remoteModelAdapter.getAllModels(offset, count, sortOrder)
    }

    @Profiled(tag="RemoteModelService.getAllModels")
    def List<ModelTransportCommand> getAllModels(int offset, int count, ModelListSorting sort) {
        return remoteModelAdapter.getAllModels(offset, count, sort)
    }

    @Profiled(tag="RemoteModelService.getAllModels")
    def List<ModelTransportCommand> getAllModels(int offset, int count) {
        return remoteModelAdapter.getAllModels(offset, count)
    }

    @Profiled(tag="RemoteModelService.getAllModels")
    def List<ModelTransportCommand> getAllModels(ModelListSorting sort) {
        return remoteModelAdapter.getAllModels(sort)
    }

    @Profiled(tag="RemoteModelService.getAllModels")
    def List<ModelTransportCommand> getAllModels() {
        return remoteModelAdapter.getAllModels()
    }

    @Profiled(tag="RemoteModelService.getModelCount")
    def Integer getModelCount() {
        return remoteModelAdapter.getModelCount()
    }

    @Profiled(tag="RemoteModelService.getLatestRevision")
    def RevisionTransportCommand getLatestRevision(long modelId) {
        return remoteModelAdapter.getLatestRevision(modelId)
    }

    @Profiled(tag="RemoteModelService.getAllRevisions")
    def List<RevisionTransportCommand> getAllRevisions(long modelId) {
        return remoteModelAdapter.getAllRevisions(modelId)
    }

    @Profiled(tag="RemoteModelService.getPublication")
    def PublicationTransportCommand getPublication(long modelId) {
        return remoteModelAdapter.getPublication(modelId)
    }

    @Profiled(tag="RemoteModelService.uploadModel")
    def ModelTransportCommand uploadModel(byte[] bytes, ModelTransportCommand meta) throws ModelException {
        return remoteModelAdapter.uploadModel(bytes, meta)
    }

    @Profiled(tag="RemoteModelService.addRevision")
    def RevisionTransportCommand addRevision(long modelId, byte[] bytes, ModelFormatTransportCommand format, String comment) throws ModelException {
        return remoteModelAdapter.addRevision(modelId, bytes, format, comment)
    }

    @Profiled(tag="RemoteModelService.canAddRevision")
    def Boolean canAddRevision(long modelId) {
        return remoteModelAdapter.canAddRevision(modelId)
    }

    @Profiled(tag="RemoteModelService.retrieveModelFile")
    def byte[] retrieveModelFile(RevisionTransportCommand revision) throws ModelException {
        return remoteModelAdapter.retrieveModelFile(revision)
    }

    @Profiled(tag="RemoteModelService.retrieveModelFile")
    def byte[] retrieveModelFile(long modelId) throws ModelException {
        return remoteModelAdapter.retrieveModelFile(modelId)
    }

    @Profiled(tag="RemoteModelService.deleteModel")
    def Boolean deleteModel(long modelId) {
        return remoteModelAdapter.deleteModel(modelId)
    }

    @Profiled(tag="RemoteModelService.restoreModel")
    def Boolean restoreModel(long modelId) {
        return remoteModelAdapter.restoreModel(modelId)
    }
}
