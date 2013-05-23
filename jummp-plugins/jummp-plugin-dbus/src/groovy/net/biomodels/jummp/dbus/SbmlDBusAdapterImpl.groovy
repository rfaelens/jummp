package net.biomodels.jummp.dbus;

import net.biomodels.jummp.webapp.ast.DBusAdapter
import net.biomodels.jummp.webapp.ast.DBusMethod
import net.biomodels.jummp.core.IModelService
import net.biomodels.jummp.core.ISbmlService


/**
 * @short Concrete Implementation of SbmlDBusAdapter.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
@SuppressWarnings(['EmptyMethod', 'UnusedMethodParameter'])
@DBusAdapter(interfaceName="SbmlDBusAdapter", serviceName="sbmlService")
public class SbmlDBusAdapterImpl extends AbstractDBusAdapter implements SbmlDBusAdapter {
    /**
     * Dependency Injection of sbmlService
     */
    ISbmlService sbmlService

    /**
     * Dependency Injection of modelDelegateService
     */
    IModelService modelDelegateService

    @DBusMethod(isAuthenticate = true, getVersion = [1, 2])
    public long getVersion(String authenticationHash, long modelId, int revisionNumber) {
    }

    @DBusMethod(isAuthenticate = true, getVersion = [1, 2])
    public long getLevel(String authenticationHash, long modelId, int revisionNumber) {
    }

    @DBusMethod(isAuthenticate = true, getVersion = [1, 2])
    public String getNotes(String authenticationHash, long modelId, int revisionNumber) {
    }

    @DBusMethod(isAuthenticate = true, getVersion = [1, 2])
    public String getMetaId(String authenticationHash, long modelId, int revisionNumber) {
    }

    @DBusMethod(isAuthenticate = true, getVersion = [1, 2], json = true)
    public String getAnnotations(String authenticationHash, long modelId, int revisionNumber) {
    }

    @DBusMethod(isAuthenticate = true, getVersion = [1, 2], json = true)
    public String getParameters(String authenticationHash, long modelId, int revisionNumber) {
    }

    @DBusMethod(isAuthenticate = true, getVersion = [1, 2], json = true)
    public String getParameter(String authenticationHash, long modelId, int revisionNumber, String id) {
    }

    @DBusMethod(isAuthenticate = true, getVersion = [1, 2], json = true)
    public String getLocalParameters(String authenticationHash, long modelId, int revisionNumber) {
    }

    @DBusMethod(isAuthenticate = true, getVersion = [1, 2], json = true)
    public String getReactions(String authenticationHash, long modelId, int revisionNumber) {
    }

    @DBusMethod(isAuthenticate = true, getVersion = [1, 2], json = true)
    public String getReaction(String authenticationHash, long modelId, int revisionNumber, String id) {
    }

    @DBusMethod(isAuthenticate = true, getVersion = [1, 2], json = true)
    public String getEvents(String authenticationHash, long modelId, int revisionNumber) {
    }

    @DBusMethod(isAuthenticate = true, getVersion = [1, 2], json = true)
    public String getEvent(String authenticationHash, long modelId, int revisionNumber, String id) {
    }

    @DBusMethod(isAuthenticate = true, getVersion = [1, 2], json = true)
    public String getRules(String authenticationHash, long modelId, int revisionNumber) {
    }

    @DBusMethod(isAuthenticate = true, getVersion = [1, 2], json = true)
    public String getRule(String authenticationHash, long modelId, int revisionNumber, String variable) {
    }

    @DBusMethod(isAuthenticate = true, getVersion = [1, 2], json = true)
    public String getFunctionDefinitions(String authenticationHash, long modelId, int revisionNumber) {
    }

    @DBusMethod(isAuthenticate = true, getVersion = [1, 2], json = true)
    public String getFunctionDefinition(String authenticationHash, long modelId, int revisionNumber, String id) {
    }

    @DBusMethod(isAuthenticate = true, getVersion = [1, 2], json = true)
    public String getCompartments(String authenticationHash, long modelId, int revisionNumber) {
    }

    @DBusMethod(isAuthenticate = true, getVersion = [1, 2], json = true)
    public String getCompartment(String authenticationHash, long modelId, int revisionNumber, String id) {
    }

    @DBusMethod(isAuthenticate = true, getVersion = [1, 2], json = true)
    public String getAllSpecies(String authenticationHash, long modelId, int revisionNumber) {
    }

    @DBusMethod(isAuthenticate = true, getVersion = [1, 2], json = true)
    public String getSpecies(String authenticationHash, long modelId, int revisionNumber, String id) {
    }

    @DBusMethod(isAuthenticate = true, getVersion = [1, 2])
    public String triggerSubmodelGeneration(String authenticationHash, long modelId, int revisionNumber, String subModelId, String metaId, List<String> compartmentIds, List<String> speciesIds, List<String> reactionIds, List<String> ruleIds, List<String> eventIds) {
    }

    @DBusMethod(isAuthenticate = true, getVersion = [1, 2])
    public byte[] generateSvg(String authenticationHash, long modelId, int revisionNumber) {
    }

    @DBusMethod(isAuthenticate = true, getVersion = [1, 2])
    public String generateOctave(String authenticationHash, long modelId, int revisionNumber) {
    }

    @DBusMethod(isAuthenticate = true, getVersion = [1, 2])
    public String generateBioPax(String authenticationHash, long modelId, int revisionNumber) {
    }
}
