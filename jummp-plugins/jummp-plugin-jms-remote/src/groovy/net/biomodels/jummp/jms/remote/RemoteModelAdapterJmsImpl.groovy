package net.biomodels.jummp.jms.remote

import org.perf4j.aop.Profiled
import net.biomodels.jummp.core.ModelException
import net.biomodels.jummp.core.model.ModelFormatTransportCommand
import net.biomodels.jummp.core.model.ModelListSorting
import net.biomodels.jummp.core.model.ModelTransportCommand
import net.biomodels.jummp.core.model.PublicationTransportCommand
import net.biomodels.jummp.core.model.RevisionTransportCommand
import net.biomodels.jummp.remote.RemoteModelAdapter
import net.biomodels.jummp.webapp.ast.RemoteJmsAdapter

/**
 * @short Service delegating to ModelService of the core via synchronous JMS
 *
 * This service communicates with ModelJmsAdapterService in core through JMS. The
 * service takes care of wrapping parameters into messages and evaluating returned
 * values.
 *
 * Any other Grails artifact can use this service as any other service. The fact that
 * it uses JMS internally is completely transparent to the users of this service.
 *
 * Important: The methods of this adapter are auto-generated through an AST transformation.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
@RemoteJmsAdapter("RemoteModelAdapter")
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
    public List<ModelTransportCommand> getAllModels(int offset, int count, ModelListSorting sort) {
        def retVal = send("getAllModels", [offset, count, sort])
        validateReturnValue(retVal, List)
        return (List)retVal
    }
    @Profiled(tag="RemoteModelAdapterJmsImpl.getAllModels")
    public List<ModelTransportCommand> getAllModels(ModelListSorting sort) {
        def retVal = send("getAllModels", [sort])
        validateReturnValue(retVal, List)
        return (List)retVal
    }
}
