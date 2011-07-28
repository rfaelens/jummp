package net.biomodels.jummp.jms.remote

import net.biomodels.jummp.remote.RemoteGeneOntologyTreeAdapter
import net.biomodels.jummp.webapp.ast.RemoteJmsAdapter

@RemoteJmsAdapter("RemoteGeneOntologyTreeAdapter")
class RemoteGeneOntologyTreeAdapterJmsImpl extends AbstractJmsRemoteAdapter implements RemoteGeneOntologyTreeAdapter {
    private static final String ADAPTER_SERVICE_NAME = "geneOntologyTreeJmsAdapter"

    protected String getAdapterServiceName() {
        return ADAPTER_SERVICE_NAME
    }
}
