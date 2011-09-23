package net.biomodels.jummp.webapp.remote

import net.biomodels.jummp.remote.RemoteModelHistoryAdapter
import net.biomodels.jummp.webapp.ast.RemoteService

@RemoteService("RemoteModelHistoryAdapter")
class RemoteModelHistoryService implements RemoteModelHistoryAdapter {

    static transactional = true
    RemoteModelHistoryAdapter remoteModelHistoryAdapter
}
