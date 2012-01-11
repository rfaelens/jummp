package net.biomodels.jummp.webapp.remote

import net.biomodels.jummp.remote.RemoteModelHistoryAdapter

class RemoteModelHistoryService implements RemoteModelHistoryAdapter {

    static transactional = true
    @SuppressWarnings("GrailsStatelessService")
    @Delegate RemoteModelHistoryAdapter remoteModelHistoryAdapter
}
