package net.biomodels.jummp.jms.remote

import org.perf4j.aop.Profiled
import net.biomodels.jummp.core.ModelException
import net.biomodels.jummp.core.model.ModelFormatTransportCommand
import net.biomodels.jummp.core.model.ModelListSorting
import net.biomodels.jummp.core.model.ModelTransportCommand
import net.biomodels.jummp.core.model.PublicationTransportCommand
import net.biomodels.jummp.core.model.RevisionTransportCommand
import net.biomodels.jummp.jms.remote.AbstractJmsRemoteAdapter
import net.biomodels.jummp.remote.RemoteModelAdapter

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
class RemoteModelAdapterJmsImpl extends AbstractJmsRemoteAdapter implements RemoteModelAdapter {

    static transactional = false
    private static final String ADAPTER_SERVICE_NAME = "modelJmsAdapter"

    protected String getAdapterServiceName() {
        return ADAPTER_SERVICE_NAME
    }

    @Profiled(tag="RemoteModelAdapterJmsImpl.getAllModels")
    public List<ModelTransportCommand> getAllModels(int offset, int count, boolean sortOrder, ModelListSorting sort) {
        def retVal = send("getAllModels", [offset, count, sortOrder, sort])
        validateReturnValue(retVal, List)
        return (List)retVal
    }

    @Profiled(tag="RemoteModelAdapterJmsImpl.getAllModels")
    public List<ModelTransportCommand> getAllModels(int offset, int count, boolean sortOrder) {
        def retVal = send("getAllModels", [offset, count, sortOrder])
        validateReturnValue(retVal, List)
        return (List)retVal
    }

    @Profiled(tag="RemoteModelAdapterJmsImpl.getAllModels")
    public List<ModelTransportCommand> getAllModels(int offset, int count, ModelListSorting sort) {
        def retVal = send("getAllModels", [offset, count, sort])
        validateReturnValue(retVal, List)
        return (List)retVal
    }

    @Profiled(tag="RemoteModelAdapterJmsImpl.getAllModels")
    public List<ModelTransportCommand> getAllModels(int offset, int count) {
        def retVal = send("getAllModels", [offset, count])
        validateReturnValue(retVal, List)
        return (List)retVal
    }

    @Profiled(tag="RemoteModelAdapterJmsImpl.getAllModels")
    public List<ModelTransportCommand> getAllModels(ModelListSorting sort) {
        def retVal = send("getAllModels", [sort])
        validateReturnValue(retVal, List)
        return (List)retVal
    }

    @Profiled(tag="RemoteModelAdapterJmsImpl.getAllModels")
    public List<ModelTransportCommand> getAllModels() {
        return getAllModels(ModelListSorting.ID)
    }

    @Profiled(tag="RemoteModelAdapterJmsImpl.getModelCount")
    public Integer getModelCount() {
        def retVal = send("getModelCount")
        validateReturnValue(retVal, Integer)
        return (Integer)retVal
    }

    @Profiled(tag="RemoteModelAdapterJmsImpl.getLatestRevision")
    public RevisionTransportCommand getLatestRevision(long modelId) {
        def retVal = send("getLatestRevision", modelId)
        validateReturnValue(retVal, RevisionTransportCommand)
        return (RevisionTransportCommand)retVal
    }

    @Profiled(tag="RemoteModelAdapterJmsImpl.getAllRevisions")
    public List<RevisionTransportCommand> getAllRevisions(long modelId) {
        def retVal = send("getAllRevisions", modelId)
        validateReturnValue(retVal, List)
        return (List<RevisionTransportCommand>)retVal
    }

    @Profiled(tag="RemoteModelAdapterJmsImpl.getPublication")
    public PublicationTransportCommand getPublication(final long modelId) {
        def retVal = send("getPublication", modelId)
        validateReturnValue(retVal, PublicationTransportCommand)
        return (PublicationTransportCommand)retVal
    }

    @Profiled(tag="RemoteModelAdapterJmsImpl.uploadModel")
    public ModelTransportCommand uploadModel(byte[] bytes, ModelTransportCommand meta) throws ModelException {
        def retVal = send("uploadModel", [bytes, meta])
        validateReturnValue(retVal, ModelTransportCommand)
        return (ModelTransportCommand)retVal
    }

    @Profiled(tag="RemoteModelAdapterJmsImpl.addRevision")
    public RevisionTransportCommand addRevision(long modelId, byte[] bytes, ModelFormatTransportCommand format, String comment) throws ModelException {
        def retVal = send("addRevision", [modelId, bytes, format, comment])
        validateReturnValue(retVal, RevisionTransportCommand)
        return (RevisionTransportCommand)retVal
    }

    @Profiled(tag="RemoteModelAdapterJmsImpl.canAddRevision")
    public Boolean canAddRevision(final long modelId) {
        def retVal = send("canAddRevision", modelId)
        validateReturnValue(retVal, Boolean)
        return (Boolean)retVal
    }

    @Profiled(tag="RemoteModelAdapterJmsImpl.retrieveModelFile")
    public byte[] retrieveModelFile(RevisionTransportCommand revision) {
        // TODO: verify closely because of byte[]
        def retVal = send("retrieveModelFile", revision)
        validateReturnValue(retVal, byte[].class)
        return (byte[])retVal
    }

    @Profiled(tag="RemoteModelAdapterJmsImpl.retrieveModelFile")
    public byte[] retrieveModelFile(long modelId) {
        // TODO: verify closely because of byte[]
        def retVal = send("retrieveModelFile", modelId)
        validateReturnValue(retVal, byte[].class)
        return (byte[])retVal
    }

    // TODO: how to handle grant/revoke rights from the webapplication? Which users to show?
    @Profiled(tag="RemoteModelAdapterJmsImpl.deleteModel")
    public Boolean deleteModel(long modelId) {
        def retVal = send("deleteModel", modelId)
        validateReturnValue(retVal, Boolean)
        return (Boolean)retVal
    }

    @Profiled(tag="RemoteModelAdapterJmsImpl.restoreModel")
    public Boolean restoreModel(long modelId) {
        def retVal = send("restoreModel", modelId)
        validateReturnValue(retVal, Boolean)
        return (Boolean)retVal
    }

}
