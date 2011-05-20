package net.biomodels.jummp.dbus;

/**
 * @short Concrete Implementation of SbmlDBusAdapter.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
public class SbmlDBusAdapterImpl extends AbstractDBusAdapter implements SbmlDBusAdapter {
    /**
     * Dependency Injection of SbmlDBusAdapterHelper
     */
    private SbmlDBusAdapterHelper helper;

    public long getVersion(String authenticationHash, long modelId, int revisionNumber) {
        return helper.getAsLong("Version", authenticationHash, modelId, revisionNumber);
    }

    public long getLevel(String authenticationHash, long modelId, int revisionNumber) {
        return helper.getAsLong("Level", authenticationHash, modelId, revisionNumber);
    }

    public String getModelNotes(String authenticationHash, long modelId, int revisionNumber) {
        return helper.getAsString("Notes", authenticationHash, modelId, revisionNumber);
    }

    public String getModelMetaId(String authenticationHash, long modelId, int revisionNumber) {
        return helper.getAsString("MetaId", authenticationHash, modelId, revisionNumber);
    }

    public String getModelAnnotations(String authenticationHash, long modelId, int revisionNumber) {
        return helper.getAsJSON("Annotations", authenticationHash, modelId, revisionNumber);
    }

    public String getParameters(String authenticationHash, long modelId, int revisionNumber) {
        return helper.getAsJSON("Parameters", authenticationHash, modelId, revisionNumber);
    }

    public String getParameter(String authenticationHash, long modelId, int revisionNumber, String id) {
        return helper.getAsJSON("Parameter", authenticationHash, modelId, revisionNumber, id);
    }

    public String getLocalParameters(String authenticationHash, long modelId, int revisionNumber) {
        return helper.getAsJSON("LocalParameters", authenticationHash, modelId, revisionNumber);
    }

    public String getReactions(String authenticationHash, long modelId, int revisionNumber) {
        return helper.getAsJSON("Reactions", authenticationHash, modelId, revisionNumber);
    }

    public String getReaction(String authenticationHash, long modelId, int revisionNumber, String id) {
        return helper.getAsJSON("Reaction", authenticationHash, modelId, revisionNumber, id);
    }

    public String getEvents(String authenticationHash, long modelId, int revisionNumber) {
        return helper.getAsJSON("Events", authenticationHash, modelId, revisionNumber);
    }

    public String getEvent(String authenticationHash, long modelId, int revisionNumber, String id) {
        return helper.getAsJSON("Event", authenticationHash, modelId, revisionNumber, id);
    }

    public String getRules(String authenticationHash, long modelId, int revisionNumber) {
        return helper.getAsJSON("Rules", authenticationHash, modelId, revisionNumber);
    }

    public String getRule(String authenticationHash, long modelId, int revisionNumber, String variable) {
        return helper.getAsJSON("Rule", authenticationHash, modelId, revisionNumber, variable);
    }

    public boolean isRemote() {
        return false;
    }

    public void setHelper(SbmlDBusAdapterHelper helper) {
        this.helper = helper;
    }
}
