package net.biomodels.jummp.webapp.remote

import net.biomodels.jummp.remote.RemoteSbmlAdapter

class RemoteSbmlService implements RemoteSbmlAdapter {

    static transactional = true
    RemoteSbmlAdapter remoteSbmlAdapter

    String getMetaId(long modelId, int revisionNumber) {
        return remoteSbmlAdapter.getMetaId(modelId, revisionNumber)
    }

    long getVersion(long modelId, int revisionNumber) {
        return remoteSbmlAdapter.getVersion(modelId, revisionNumber)
    }

    long getLevel(long modelId, int revisionNumber) {
        return remoteSbmlAdapter.getLevel(modelId, revisionNumber)
    }

    String getNotes(long modelId, int revisionNumber) {
        return remoteSbmlAdapter.getNotes(modelId, revisionNumber)
    }

    List<Map> getAnnotations(long modelId, int revisionNumber) {
        return remoteSbmlAdapter.getAnnotations(modelId, revisionNumber)
    }

    List<Map> getParameters(long modelId, int revisionNumber) {
        return remoteSbmlAdapter.getParameters(modelId, revisionNumber)
    }

    Map getParameter(long modelId, int revisionNumber, String id) {
        return remoteSbmlAdapter.getParameter(modelId, revisionNumber, id)
    }

    List<Map> getLocalParameters(long modelId, int revisionNumber) {
        return remoteSbmlAdapter.getLocalParameters(modelId, revisionNumber)
    }

    public List<Map> getReactions(long modelId, int revisionNumber) {
        return remoteSbmlAdapter.getReactions(modelId, revisionNumber)
    }

    public Map getReaction(long modelId, int revisionNumber, String id) {
        return remoteSbmlAdapter.getReaction(modelId, revisionNumber, id)
    }
}
