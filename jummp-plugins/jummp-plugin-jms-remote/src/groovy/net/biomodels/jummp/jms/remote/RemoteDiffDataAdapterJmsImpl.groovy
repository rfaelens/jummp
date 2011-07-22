package net.biomodels.jummp.jms.remote

import net.biomodels.jummp.webapp.ast.RemoteJmsAdapter
import net.biomodels.jummp.remote.RemoteDiffDataAdapter


/**
 * 
 * Important: The methods of this adapter are auto-generated through an AST transformation.
 * @author Robert Haelke, robert.haelke@googlemail.com
 * @date 04.07.2011
 * @year 2011
 */
@RemoteJmsAdapter("RemoteDiffDataAdapter")
class RemoteDiffDataAdapterJmsImpl extends AbstractJmsRemoteAdapter implements RemoteDiffDataAdapter {

    static transactional = false
    private static final String ADAPTER_SERVICE_NAME = "diffDataJmsAdapter"

    protected String getAdapterServiceName() {
        return ADAPTER_SERVICE_NAME
    }
}
