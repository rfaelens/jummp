package net.biomodels.jummp.dbus.remote

import net.biomodels.jummp.core.model.ModelFormatTransportCommand
import net.biomodels.jummp.core.model.ModelListSorting
import net.biomodels.jummp.core.model.ModelTransportCommand
import net.biomodels.jummp.core.model.PublicationTransportCommand
import net.biomodels.jummp.core.model.RevisionTransportCommand
import net.biomodels.jummp.dbus.ModelDBusAdapter
import net.biomodels.jummp.remote.RemoteModelAdapter
import org.perf4j.aop.Profiled
import org.springframework.beans.factory.InitializingBean
import org.apache.commons.io.FileUtils
import net.biomodels.jummp.dbus.model.DBusModel
import net.biomodels.jummp.dbus.model.DBusPublication
import net.biomodels.jummp.core.ModelException
import net.biomodels.jummp.webapp.ast.RemoteDBusAdapter
import java.util.List
import java.util.LinkedList

/**
 * @short DBus Implementation of RemoteModelAdapter.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
@RemoteDBusAdapter(interfaceName="RemoteModelAdapter",dbusAdapterName="modelDBusAdapter")
class RemoteModelAdapterDBusImpl extends AbstractRemoteDBusAdapter implements RemoteModelAdapter, InitializingBean {
    private ModelDBusAdapter modelDBusAdapter

    public void afterPropertiesSet() throws Exception {
        modelDBusAdapter = getRemoteObject("/Model", ModelDBusAdapter.class)
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

    @Profiled(tag="RemoteModelAdapterDBusImpl.getLatestRevision")
    RevisionTransportCommand getLatestRevision(long modelId) {
        RevisionTransportCommand revision = modelDBusAdapter.getLatestRevision(authenticationToken(), modelId)
        revision.model = modelDBusAdapter.getModel(authenticationToken(), modelId)
        return revision
    }

    @Profiled(tag="RemoteModelAdapterDBusImpl.getRevision")
    RevisionTransportCommand getRevision(long modelId, int revisionNumber) {
        RevisionTransportCommand revision = modelDBusAdapter.getRevision(authenticationToken(), modelId, revisionNumber)
        revision.model = modelDBusAdapter.getModel(authenticationToken(), modelId)
        return revision
    }

    @Profiled(tag="RemoteModelAdapterDBusImpl.getAllRevisions")
    List<RevisionTransportCommand> getAllRevisions(long modelId) {
        List<RevisionTransportCommand> revisions = []
        modelDBusAdapter.getAllRevisions(authenticationToken(), modelId).each {
            revisions << modelDBusAdapter.getRevision(authenticationToken(), modelId, it as int)
        }
        return revisions
    }

    private List<File> getAsFileList(List<byte[]> bytes)
    {
        List<File> files=new LinkedList<File>();
        int counter=0;
        bytes.each
        { byteFile ->
           File file = File.createTempFile("jummp_${counter++}", "model")
           file.withWriter { fileWriter ->
                fileWriter.write(new String(byteFile))
            }
            files.add(file)
        }
        return files;
    }
    
    @Profiled(tag="RemoteModelAdapterDBusImpl.uploadModel")
    ModelTransportCommand uploadModel(List<byte[]> bytes, ModelTransportCommand meta) throws ModelException {
        ModelTransportCommand model = null
        files=getAsFileList(bytes);
        if (meta.publication) {
            model = modelDBusAdapter.uploadModelWithPublication(authenticationToken(), files, DBusModel.fromModelTransportCommand(meta), DBusPublication.fromPublicationTransportCommand(meta.publication))
        } else {
            model = modelDBusAdapter.uploadModel(authenticationToken(), files, DBusModel.fromModelTransportCommand(meta))
        }
        files.each
        {
            FileUtils.deleteQuietly(it)
        }
        return model
    }

    @Profiled(tag="RemoteModelAdapterDBusImpl.addRevision")
    RevisionTransportCommand addRevision(long modelId, byte[] bytes, ModelFormatTransportCommand format, String comment) throws ModelException {
        File file = File.createTempFile("jummp", "model")
        file.withWriter {
            it.write(new String(bytes))
        }
        RevisionTransportCommand revision = modelDBusAdapter.addRevision(authenticationToken(), modelId, file.getAbsolutePath(), format.identifier, comment)
        revision.model = modelDBusAdapter.getModel(authenticationToken(), modelId)
        FileUtils.deleteQuietly(file)
        return revision
    }

    @Profiled(tag="RemoteModelAdapterDBusImpl.retrieveModelFiles")
    byte[] retrieveModelFiles(RevisionTransportCommand revision) throws ModelException {
        File file = new File(modelDBusAdapter.retrieveModelFilesByRevision(authenticationToken(), revision.id))
        byte[] bytes = file.readBytes()
        FileUtils.deleteQuietly(file)
        return bytes
    }

    @Profiled(tag="RemoteModelAdapterDBusImpl.retrieveModelFiles")
    byte[] retrieveModelFiles(long modelId) throws ModelException {
        File file = new File(modelDBusAdapter.retrieveModelFilesByModel(authenticationToken(), modelId))
        byte[] bytes = file.readBytes()
        FileUtils.deleteQuietly(file)
        return bytes
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
