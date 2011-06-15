package net.biomodels.jummp.dbus;

import net.biomodels.jummp.webapp.ast.DBusAdapter
import net.biomodels.jummp.webapp.ast.DBusMethod

/**
 * @short Concrete Implementation of SbmlDBusAdapter.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
@DBusAdapter(interfaceName="SbmlDBusAdapter", serviceName="sbmlService")
public class SbmlDBusAdapterImpl extends AbstractDBusAdapter implements SbmlDBusAdapter {
    /**
     * Dependency Injection of SbmlDBusAdapterHelper
     */
    private SbmlDBusAdapterHelper helper;
    /**
     * Dependency Injection of sbmlService
     */
    def sbmlService
    /**
     * Dependency Injection of modelDelegateService
     */
    def modelDelegateService

    @DBusMethod(isAuthenticate = true, getRevision = [1, 2])
    public long getVersion(String authenticationHash, long modelId, int revisionNumber) {
    }

    @DBusMethod(isAuthenticate = true, getRevision = [1, 2])
    public long getLevel(String authenticationHash, long modelId, int revisionNumber) {
    }

    @DBusMethod(isAuthenticate = true, getRevision = [1, 2])
    public String getNotes(String authenticationHash, long modelId, int revisionNumber) {
    }

    @DBusMethod(isAuthenticate = true, getRevision = [1, 2])
    public String getMetaId(String authenticationHash, long modelId, int revisionNumber) {
    }

    public String getAnnotations(String authenticationHash, long modelId, int revisionNumber) {
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

    public String getFunctionDefinitions(String authenticationHash, long modelId, int revisionNumber) {
        return helper.getAsJSON("FunctionDefinitions", authenticationHash, modelId, revisionNumber);
    }

    public String getFunctionDefinition(String authenticationHash, long modelId, int revisionNumber, String id) {
        return helper.getAsJSON("FunctionDefinition", authenticationHash, modelId, revisionNumber, id);
    }

    public String getCompartments(String authenticationHash, long modelId, int revisionNumber) {
        return helper.getAsJSON("Compartments", authenticationHash, modelId, revisionNumber);
    }

    public String getCompartment(String authenticationHash, long modelId, int revisionNumber, String id) {
        return helper.getAsJSON("Compartment", authenticationHash, modelId, revisionNumber, id);
    }

    public String getAllSpecies(String authenticationHash, long modelId, int revisionNumber) {
        return helper.getAsJSON("AllSpecies", authenticationHash, modelId, revisionNumber);
    }

    public String getSpecies(String authenticationHash, long modelId, int revisionNumber, String id) {
        return helper.getAsJSON("Species", authenticationHash, modelId, revisionNumber, id);
    }

    public boolean isRemote() {
        return false;
    }

    public void setHelper(SbmlDBusAdapterHelper helper) {
        this.helper = helper;
    }
}
