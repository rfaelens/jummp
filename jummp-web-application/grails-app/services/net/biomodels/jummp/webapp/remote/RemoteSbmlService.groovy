package net.biomodels.jummp.webapp.remote

import net.biomodels.jummp.remote.RemoteSbmlAdapter
import org.perf4j.aop.Profiled

class RemoteSbmlService implements RemoteSbmlAdapter {

    static transactional = true
    RemoteSbmlAdapter remoteSbmlAdapter

    @Profiled(tag="RemoteSbmlService.getMetaId")
    String getMetaId(long modelId, int revisionNumber) {
        return remoteSbmlAdapter.getMetaId(modelId, revisionNumber)
    }

    @Profiled(tag="RemoteSbmlService.getVersion")
    long getVersion(long modelId, int revisionNumber) {
        return remoteSbmlAdapter.getVersion(modelId, revisionNumber)
    }

    @Profiled(tag="RemoteSbmlService.")
    long getLevel(long modelId, int revisionNumber) {
        return remoteSbmlAdapter.getLevel(modelId, revisionNumber)
    }

    @Profiled(tag="RemoteSbmlService.getNotes")
    String getNotes(long modelId, int revisionNumber) {
        return remoteSbmlAdapter.getNotes(modelId, revisionNumber)
    }

    @Profiled(tag="RemoteSbmlService.getAnnotations")
    List<Map> getAnnotations(long modelId, int revisionNumber) {
        return remoteSbmlAdapter.getAnnotations(modelId, revisionNumber)
    }

    @Profiled(tag="RemoteSbmlService.getParameters")
    List<Map> getParameters(long modelId, int revisionNumber) {
        return remoteSbmlAdapter.getParameters(modelId, revisionNumber)
    }

    @Profiled(tag="RemoteSbmlService.getParameter")
    Map getParameter(long modelId, int revisionNumber, String id) {
        return remoteSbmlAdapter.getParameter(modelId, revisionNumber, id)
    }

    @Profiled(tag="RemoteSbmlService.getLocalParameters")
    List<Map> getLocalParameters(long modelId, int revisionNumber) {
        return remoteSbmlAdapter.getLocalParameters(modelId, revisionNumber)
    }

    @Profiled(tag="RemoteSbmlService.getReactions")
    public List<Map> getReactions(long modelId, int revisionNumber) {
        return remoteSbmlAdapter.getReactions(modelId, revisionNumber)
    }

    @Profiled(tag="RemoteSbmlService.getReaction")
    public Map getReaction(long modelId, int revisionNumber, String id) {
        return remoteSbmlAdapter.getReaction(modelId, revisionNumber, id)
    }

    @Profiled(tag="RemoteSbmlService.getEvents")
    public List<Map> getEvents(long modelId, int revisionNumber) {
        return remoteSbmlAdapter.getEvents(modelId, revisionNumber)
    }

    @Profiled(tag="RemoteSbmlService.getEvent")
    public Map getEvent(long modelId, int revisionNumber, String id) {
        return remoteSbmlAdapter.getEvent(modelId, revisionNumber, id)
    }

    @Profiled(tag="RemoteSbmlService.getRules")
    public List<Map> getRules(long modelId, int revisionNumber) {
        return remoteSbmlAdapter.getRules(modelId, revisionNumber)
    }

    @Profiled(tag="RemoteSbmlService.getRule")
    public Map getRule(long modelId, int revisionNumber, String variable) {
        return remoteSbmlAdapter.getRule(modelId, revisionNumber, variable)
    }

    @Profiled(tag="RemoteSbmlService.getFunctionDefinitions")
    public List<Map> getFunctionDefinitions(long modelId, int revisionNumber) {
        return remoteSbmlAdapter.getFunctionDefinitions(modelId, revisionNumber)
    }

    @Profiled(tag="RemoteSbmlService.getFunctionDefinition")
    public Map getFunctionDefinition(long modelId, int revisionNumber, String id) {
        return remoteSbmlAdapter.getFunctionDefinition(modelId, revisionNumber, id)
    }
}
