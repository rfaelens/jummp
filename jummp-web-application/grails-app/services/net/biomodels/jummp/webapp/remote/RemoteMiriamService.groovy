package net.biomodels.jummp.webapp.remote

import net.biomodels.jummp.core.miriam.IMiriamService
import net.biomodels.jummp.webapp.ast.RemoteService

@RemoteService("IMiriamService")
class RemoteMiriamService implements IMiriamService {

    static transactional = true
    IMiriamService iMiriamService
}
