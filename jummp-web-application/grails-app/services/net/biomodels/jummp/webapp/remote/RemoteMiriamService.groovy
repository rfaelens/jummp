package net.biomodels.jummp.webapp.remote

import net.biomodels.jummp.core.miriam.IMiriamService
import net.biomodels.jummp.webapp.ast.RemoteService

class RemoteMiriamService implements IMiriamService {

    static transactional = true
    @Delegate IMiriamService iMiriamService
}
