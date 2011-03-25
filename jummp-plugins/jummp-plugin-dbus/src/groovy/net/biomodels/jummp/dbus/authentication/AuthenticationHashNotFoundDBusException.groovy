package net.biomodels.jummp.dbus.authentication

import org.freedesktop.dbus.exceptions.DBusExecutionException

/**
 * @short DBus Wrapper for net.biomodels.jummp.core.user.AuthenticationHashNotFoundException
 */
public class AuthenticationHashNotFoundDBusException extends DBusExecutionException {
    public AuthenticationHashNotFoundDBusException(String msg) {
        super(msg)
    }
}
