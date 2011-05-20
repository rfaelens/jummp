package net.biomodels.jummp.dbus

/**
 * @short Interface for accessing from Java.
 *
 * See documentation of the implementation @link SbmlDBusAdapterHelperImpl.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
public interface SbmlDBusAdapterHelper {
    public String getAsJSON(String name, String authenticationHash, long modelId, int revisionNumber)
    public String getAsJSON(String name, String authenticationHash, long modelId, int revisionNumber, String id)
    public String getAsString(String name, String authenticationHash, long modelId, int revisionNumber)
    public long getAsLong(String name, String authenticationHash, long modelId, int revisionNumber)
}