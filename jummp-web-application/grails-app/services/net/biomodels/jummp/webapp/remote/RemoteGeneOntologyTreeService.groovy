package net.biomodels.jummp.webapp.remote

import net.biomodels.jummp.remote.RemoteGeneOntologyTreeAdapter

class RemoteGeneOntologyTreeService implements RemoteGeneOntologyTreeAdapter {

    static transactional = true
    @Delegate RemoteGeneOntologyTreeAdapter remoteGeneOntologyTreeAdapter
}
