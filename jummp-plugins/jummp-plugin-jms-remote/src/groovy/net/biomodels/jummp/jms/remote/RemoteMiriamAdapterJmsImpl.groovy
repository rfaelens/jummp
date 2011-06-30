package net.biomodels.jummp.jms.remote

import net.biomodels.jummp.core.miriam.IMiriamService
import net.biomodels.jummp.webapp.ast.RemoteJmsAdapter

/**
 * @short Remote JMS Adapter to MiriamService
 */
@RemoteJmsAdapter("IMiriamService")
class RemoteMiriamAdapterJmsImpl extends AbstractJmsRemoteAdapter implements IMiriamService {

    private static final ADAPTER_SERVICE_NAME = "miriamJmsAdapter"

    protected String getAdapterServiceName() {
        return ADAPTER_SERVICE_NAME
    }
}
