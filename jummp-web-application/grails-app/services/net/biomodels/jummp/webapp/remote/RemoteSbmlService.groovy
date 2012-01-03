package net.biomodels.jummp.webapp.remote

import net.biomodels.jummp.remote.RemoteSbmlAdapter

/**
 * @short Delegating Service to RemoteSbmlAdapter.
 *
 * Be aware: the methods are generated by the RemoteServiceTransformation!
 * Do not implement the methods of the RemoteSbmlAdapter!
 */
class RemoteSbmlService implements RemoteSbmlAdapter {

    static transactional = true
    @Delegate RemoteSbmlAdapter remoteSbmlAdapter
}
