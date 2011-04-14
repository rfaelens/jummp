package net.biomodels.jummp.dbus.authentication

import org.freedesktop.dbus.exceptions.DBusExecutionException

/**
 * @short DBus Wrapper around org.springframework.security.core.AuthenticationException
 */
public class AuthenticationDBusException extends DBusExecutionException {
    public AuthenticationDBusException(String msg) {
        super(msg)
    }
}
