package net.biomodels.jummp.webapp.remote

import net.biomodels.jummp.remote.RemoteSbmlAdapter

class RemoteSbmlService implements RemoteSbmlAdapter {

    static transactional = true
    RemoteSbmlAdapter remoteSbmlAdapter

    def String getMetaId(long modelId, int revisionNumber) {
        return remoteSbmlAdapter.getMetaId(modelId, revisionNumber)
    }

    def long getVersion(long modelId, int revisionNumber) {
        return remoteSbmlAdapter.getVersion(modelId, revisionNumber)
    }

    def long getLevel(long modelId, int revisionNumber) {
        return remoteSbmlAdapter.getLevel(modelId, revisionNumber)
    }

    def String getNotes(long modelId, int revisionNumber) {
        return remoteSbmlAdapter.getNotes(modelId, revisionNumber)
    }

    def List<Map> getAnnotations(long modelId, int revisionNumber) {
        return remoteSbmlAdapter.getAnnotations(modelId, revisionNumber)
    }

    List<Map> getParameters(long modelId, int revisionNumber) {
        return remoteSbmlAdapter.getParameters(modelId, revisionNumber)
    }
}
