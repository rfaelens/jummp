package net.biomodels.jummp.plugins.jms

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
class ModelAdapterService extends AbstractJmsRemoteAdapter implements RemoteModelAdapter {

    static transactional = false
    private static final String ADAPTER_SERVICE_NAME = "modelJmsAdapter"

    protected String getAdapterServiceName() {
        return ADAPTER_SERVICE_NAME
    }

    @Profiled(tag="modelAdapterService.getAllModels")
    public List<ModelTransportCommand> getAllModels(int offset, int count, boolean sortOrder, ModelListSorting sort) {
        def retVal = send("getAllModels", [offset, count, sortOrder, sort])
        validateReturnValue(retVal, List)
        return (List)retVal
    }

    @Profiled(tag="modelAdapterService.getAllModels")
    public List<ModelTransportCommand> getAllModels(int offset, int count, boolean sortOrder) {
        def retVal = send("getAllModels", [offset, count, sortOrder])
        validateReturnValue(retVal, List)
        return (List)retVal
    }

    @Profiled(tag="modelAdapterService.getAllModels")
    public List<ModelTransportCommand> getAllModels(int offset, int count, ModelListSorting sort) {
        def retVal = send("getAllModels", [offset, count, sort])
        validateReturnValue(retVal, List)
        return (List)retVal
    }

    @Profiled(tag="modelAdapterService.getAllModels")
    public List<ModelTransportCommand> getAllModels(int offset, int count) {
        def retVal = send("getAllModels", [offset, count])
        validateReturnValue(retVal, List)
        return (List)retVal
    }

    @Profiled(tag="modelAdapterService.getAllModels")
    public List<ModelTransportCommand> getAllModels(ModelListSorting sort) {
        def retVal = send("getAllModels", [sort])
        validateReturnValue(retVal, List)
        return (List)retVal
    }

    @Profiled(tag="modelAdapterService.getAllModels")
    public List<ModelTransportCommand> getAllModels() {
        return getAllModels(ModelListSorting.ID)
    }

    @Profiled(tag="modelAdapterService.getModelCount")
    public Integer getModelCount() {
        def retVal = send("getModelCount")
        validateReturnValue(retVal, Integer)
        return (Integer)retVal
    }

    @Profiled(tag="modelAdapterService.getLatestRevision")
    public RevisionTransportCommand getLatestRevision(ModelTransportCommand model) {
        def retVal = send("getLatestRevision", model)
        validateReturnValue(retVal, RevisionTransportCommand)
        return (RevisionTransportCommand)retVal
    }

    @Profiled(tag="modelAdapterService.getAllRevisions")
    public List<RevisionTransportCommand> getAllRevisions(ModelTransportCommand model) {
        def retVal = send("getAllRevisions", model)
        validateReturnValue(retVal, List)
        return (List<RevisionTransportCommand>)retVal
    }

    @Profiled(tag="modelAdapterService.getPublication")
    public PublicationTransportCommand getPublication(final ModelTransportCommand model) {
        def retVal = send("getPublication", model)
        validateReturnValue(retVal, PublicationTransportCommand)
        return (PublicationTransportCommand)retVal
    }

    @Profiled(tag="modelAdapterService.uploadModel")
    public ModelTransportCommand uploadModel(byte[] bytes, ModelTransportCommand meta) throws ModelException {
        def retVal = send("uploadModel", [bytes, meta])
        validateReturnValue(retVal, ModelTransportCommand)
        return (ModelTransportCommand)retVal
    }

    @Profiled(tag="modelAdapterService.addRevision")
    public RevisionTransportCommand addRevision(ModelTransportCommand model, byte[] bytes, ModelFormatTransportCommand format, String comment) throws ModelException {
        def retVal = send("addRevision", [model, bytes, format, comment])
        validateReturnValue(retVal, RevisionTransportCommand)
        return (RevisionTransportCommand)retVal
    }

    @Profiled(tag="modelAdapterService.canAddRevision")
    public Boolean canAddRevision(final ModelTransportCommand model) {
        def retVal = send("canAddRevision", model)
        validateReturnValue(retVal, Boolean)
        return (Boolean)retVal
    }

    @Profiled(tag="modelAdapterService.retrieveModelFile")
    public byte[] retrieveModelFile(RevisionTransportCommand revision) {
        // TODO: verify closely because of byte[]
        def retVal = send("retrieveModelFile", revision)
        validateReturnValue(retVal, byte[].class)
        return (byte[])retVal
    }

    @Profiled(tag="modelAdapterService.retrieveModelFile")
    public byte[] retrieveModelFile(ModelTransportCommand model) {
        // TODO: verify closely because of byte[]
        def retVal = send("retrieveModelFile", model)
        validateReturnValue(retVal, byte[].class)
        return (byte[])retVal
    }

    // TODO: how to handle grant/revoke rights from the webapplication? Which users to show?
    @Profiled(tag="modelAdapterService.deleteModel")
    public Boolean deleteModel(ModelTransportCommand model) {
        def retVal = send("deleteModel", model)
        validateReturnValue(retVal, Boolean)
        return (Boolean)retVal
    }

    @Profiled(tag="modelAdapterService.restoreModel")
    public Boolean restoreModel(ModelTransportCommand model) {
        def retVal = send("restoreModel", model)
        validateReturnValue(retVal, Boolean)
        return (Boolean)retVal
    }

}
