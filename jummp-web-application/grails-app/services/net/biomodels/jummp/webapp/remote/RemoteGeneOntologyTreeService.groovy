package net.biomodels.jummp.webapp.remote

import net.biomodels.jummp.remote.RemoteGeneOntologyTreeAdapter
import net.biomodels.jummp.webapp.ast.RemoteService

class RemoteGeneOntologyTreeService implements RemoteGeneOntologyTreeAdapter {

    static transactional = true
    @Delegate RemoteGeneOntologyTreeAdapter remoteGeneOntologyTreeAdapter
}
