package net.biomodels.jummp.dbus.remote

import net.biomodels.jummp.core.model.ModelFormatTransportCommand
import net.biomodels.jummp.core.model.ModelListSorting
import net.biomodels.jummp.core.model.ModelTransportCommand
import net.biomodels.jummp.core.model.PublicationTransportCommand
import net.biomodels.jummp.core.model.RevisionTransportCommand
import net.biomodels.jummp.dbus.ModelDBusAdapter
import net.biomodels.jummp.remote.AbstractRemoteAdapter
import net.biomodels.jummp.remote.RemoteModelAdapter
import org.freedesktop.dbus.DBusConnection
import org.perf4j.aop.Profiled
import org.springframework.beans.factory.InitializingBean

/**
 * @short DBus Implementation of RemoteModelAdapter.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class RemoteModelAdapterDBusImpl extends AbstractRemoteAdapter implements RemoteModelAdapter, InitializingBean {
    private DBusConnection connection
    private ModelDBusAdapter modelDBusAdapter

    public void afterPropertiesSet() throws Exception {
        connection =  DBusConnection.getConnection(DBusConnection.SESSION)
        modelDBusAdapter = (ModelDBusAdapter)connection.getRemoteObject("net.biomodels.jummp", "/Model", ModelDBusAdapter.class)
    }

    @Profiled(tag="RemoteModelAdapterDBusImpl.getAllModels")
    List<ModelTransportCommand> getAllModels(int offset, int count, boolean sortOrder, ModelListSorting sort) {
        return retrieveModels(modelDBusAdapter.getAllModelsByOffsetCountSortOrderAndSortColumn(authenticationToken(), offset, count, sortOrder, sort.toString()))
    }

    @Profiled(tag="RemoteModelAdapterDBusImpl.getAllModels")
    List<ModelTransportCommand> getAllModels(int offset, int count, boolean sortOrder) {
        return retrieveModels(modelDBusAdapter.getAllModelsByOffsetCountAndSortOrder(authenticationToken(), offset, count, sortOrder))
    }

    @Profiled(tag="RemoteModelAdapterDBusImpl.getAllModels")
    List<ModelTransportCommand> getAllModels(int offset, int count, ModelListSorting sort) {
        return retrieveModels(modelDBusAdapter.getAllModelsByOffsetCountAndSortColumn(authenticationToken(), offset, count, sort.toString()))
    }

    @Profiled(tag="RemoteModelAdapterDBusImpl.getAllModels")
    List<ModelTransportCommand> getAllModels(int offset, int count) {
        return retrieveModels(modelDBusAdapter.getAllModelsByOffsetAndCount(authenticationToken(), offset, count))
    }

    @Profiled(tag="RemoteModelAdapterDBusImpl.getAllModels")
    List<ModelTransportCommand> getAllModels(ModelListSorting sort) {
        return retrieveModels(modelDBusAdapter.getAllModelsBySortColumn(authenticationToken(), sort.toString()))
    }

    @Profiled(tag="RemoteModelAdapterDBusImpl.getAllModels")
    List<ModelTransportCommand> getAllModels() {
        return retrieveModels(modelDBusAdapter.getAllModels(authenticationToken()))
    }

    @Profiled(tag="RemoteModelAdapterDBusImpl.getModelCount")
    Integer getModelCount() {
        return modelDBusAdapter.getModelCount(authenticationToken())
    }

    @Profiled(tag="RemoteModelAdapterDBusImpl.getLatestRevision")
    RevisionTransportCommand getLatestRevision(ModelTransportCommand model) {
        RevisionTransportCommand revision = modelDBusAdapter.getLatestRevision(authenticationToken(), model.id)
        revision.model = modelDBusAdapter.getModel(authenticationToken(), model.id)
        return revision
    }

    @Profiled(tag="RemoteModelAdapterDBusImpl.getAllRevisions")
    List<RevisionTransportCommand> getAllRevisions(ModelTransportCommand model) {
        List<RevisionTransportCommand> revisions = []
        modelDBusAdapter.getAllRevisions(authenticationToken(), model.id).each {
            revisions << modelDBusAdapter.getRevision(authenticationToken(), model.id, it as int)
        }
        return revisions
    }

    @Profiled(tag="RemoteModelAdapterDBusImpl.getPublication")
    PublicationTransportCommand getPublication(ModelTransportCommand model) {
        return modelDBusAdapter.getPublication(authenticationToken(), model.id)
    }

    @Profiled(tag="RemoteModelAdapterDBusImpl.uploadModel")
    ModelTransportCommand uploadModel(byte[] bytes, ModelTransportCommand meta) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Profiled(tag="RemoteModelAdapterDBusImpl.addRevision")
    RevisionTransportCommand addRevision(ModelTransportCommand model, byte[] bytes, ModelFormatTransportCommand format, String comment) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Profiled(tag="RemoteModelAdapterDBusImpl.canAddRevision")
    Boolean canAddRevision(ModelTransportCommand model) {
        return modelDBusAdapter.canAddRevision(authenticationToken(), model.id)
    }

    @Profiled(tag="RemoteModelAdapterDBusImpl.retrieveModelFile")
    byte[] retrieveModelFile(RevisionTransportCommand revision) {
        return new byte[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Profiled(tag="RemoteModelAdapterDBusImpl.retrieveModelFile")
    byte[] retrieveModelFile(ModelTransportCommand model) {
        return new byte[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Profiled(tag="RemoteModelAdapterDBusImpl.deleteModel")
    Boolean deleteModel(ModelTransportCommand model) {
        return modelDBusAdapter.deleteModel(authenticationToken(), model.id)
    }

    @Profiled(tag="RemoteModelAdapterDBusImpl.restoreModel")
    Boolean restoreModel(ModelTransportCommand model) {
        return modelDBusAdapter.restoreModel(authenticationToken(), model.id)
    }

    private List<ModelTransportCommand> retrieveModels(List<String> ids) {
        List<ModelTransportCommand> models = []
        ids.each {
            ModelTransportCommand model = modelDBusAdapter.getModel(authenticationToken(), it as Long)
            model.publication = modelDBusAdapter.getPublication(authenticationToken(), it as Long)
            models << model
        }
        return models
    }
}
