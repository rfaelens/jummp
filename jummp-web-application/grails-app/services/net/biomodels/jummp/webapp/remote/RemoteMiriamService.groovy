package net.biomodels.jummp.webapp.remote

import net.biomodels.jummp.core.miriam.IMiriamService

class RemoteMiriamService implements IMiriamService {

    static transactional = true
    @Delegate IMiriamService iMiriamService
}
