package net.biomodels.jummp.dbus;

import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusInterfaceName;

import java.util.List;
import java.util.Map;

/**
 * @short DBus interface to SBML.
 */
@DBusInterfaceName("net.biomodels.jummp.sbml")
public interface SbmlDBusAdapter  extends DBusInterface {

    public long getVersion(String authenticationHash, long modelId, int revisionNumber);
    public long getLevel(String authenticationHash, long modelId, int revisionNumber);
    public String getNotes(String authenticationHash, long modelId, int revisionNumber);
    public String getMetaId(String authenticationHash, long modelId, int revisionNumber);

    /**
     *
     * @param authenticationHash
     * @param modelId
     * @param revisionNumber
     * @return JSON String of the annotation on Model level
     */
    public String getAnnotations(String authenticationHash, long modelId, int revisionNumber);
    public String getParameters(String authenticationHash, long modelId, int revisionNumber);
    public String getParameter(String authenticationHash, long modelId, int revisionNumber, String id);
    public String getLocalParameters(String authenticationHash, long modelId, int revisionNumber);
    public String getReactions(String authenticationHash, long modelId, int revisionNumber);
    public String getReaction(String authenticationHash, long modelId, int revisionNumber, String id);
    public String getEvents(String authenticationHash, long modelId, int revisionNumber);
    public String getEvent(String authenticationHash, long modelId, int revisionNumber, String id);
    public String getRules(String authenticationHash, long modelId, int revisionNumber);
    public String getRule(String authenticationHash, long modelId, int revisionNumber, String variable);
    public String getFunctionDefinitions(String authenticationHash, long modelId, int revisionNumber);
    public String getFunctionDefinition(String authenticationHash, long modelId, int revisionNumber, String id);
    public String getCompartments(String authenticationHash, long modelId, int revisionNumber);
    public String getCompartment(String authenticationHash, long modelId, int revisionNumber, String id);
    public String getAllSpecies(String authenticationHash, long modelId, int revisionNumber);
    public String getSpecies(String authenticationHash, long modelId, int revisionNumber, String id);
}
