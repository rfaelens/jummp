package net.biomodels.jummp.dbus.remote

import org.springframework.beans.factory.InitializingBean
import net.biomodels.jummp.core.model.ModelTransportCommand
import net.biomodels.jummp.dbus.ModelHistoryDBusAdapter
import net.biomodels.jummp.dbus.ModelDBusAdapter
import net.biomodels.jummp.remote.RemoteModelHistoryAdapter
import net.biomodels.jummp.webapp.ast.RemoteDBusAdapter
import org.perf4j.aop.Profiled

@RemoteDBusAdapter(interfaceName="RemoteModelHistoryAdapter", dbusAdapterName="modelHistoryDBusAdapter")
class RemoteModelHistoryDBusAdapterImpl extends AbstractRemoteDBusAdapter implements RemoteModelHistoryAdapter, InitializingBean {
    private ModelHistoryDBusAdapter modelHistoryDBusAdapter
    private ModelDBusAdapter modelDBusAdapter

    public void afterPropertiesSet() throws Exception {
        modelHistoryDBusAdapter = getRemoteObject("/ModelHistory", ModelHistoryDBusAdapter.class)
        modelDBusAdapter = getRemoteObject("/Model", ModelDBusAdapter.class)
    }

    @Profiled(tag="RemoteModelHistoryAdapterDBusImpl.getAllModels")
    List<ModelTransportCommand> history() {
        return retrieveModels(modelHistoryDBusAdapter.history(authenticationToken()))
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