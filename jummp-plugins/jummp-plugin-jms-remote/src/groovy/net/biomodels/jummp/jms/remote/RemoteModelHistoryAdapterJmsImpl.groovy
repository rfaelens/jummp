package net.biomodels.jummp.jms.remote

import net.biomodels.jummp.remote.RemoteModelHistoryAdapter
import net.biomodels.jummp.webapp.ast.RemoteJmsAdapter

@RemoteJmsAdapter("RemoteModelHistoryAdapter")
class RemoteModelHistoryAdapterJmsImpl extends AbstractJmsRemoteAdapter implements RemoteModelHistoryAdapter {
    private static final String ADAPTER_SERVICE_NAME = "modelHistoryJmsAdapter"

    protected String getAdapterServiceName() {
        return ADAPTER_SERVICE_NAME
    }
}
