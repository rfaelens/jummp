package net.biomodels.jummp.dbus;

import net.biomodels.jummp.dbus.authentication.AuthenticationDBusException;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusInterfaceName;

import java.util.Map;

/**
 * @short DBus Interface for Application instance methods.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
@DBusInterfaceName("net.biomodels.jummp.application")
public interface ApplicationDBusAdapter extends DBusInterface {
    /**
     * Performs an Authentication against the local AuthenticationManager.
     * In case the Authentication is successful the returned Authentication instance is
     * added to the internal AuthenticationHash and a DBusAuthentication is populated
     * with the Authentication's GrantedAuthorities and the hash.
     * @param userName The login id
     * @param password The password
     * @return A fully populated DBusAuthentication
     * @throws AuthenticationDBusException Wrapper for AuthenticationException following the same contract.
     */
    public DBusAuthentication authenticate(String userName, String password) throws AuthenticationDBusException;
    /**
     * Retrieves the jummp configuration of the core application.
     * @return The core's configuration as a flattened map structure.
     */
    public Map<String, String> getJummpConfig();
}
