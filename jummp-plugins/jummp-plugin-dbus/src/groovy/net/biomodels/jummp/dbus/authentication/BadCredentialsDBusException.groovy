package net.biomodels.jummp.dbus.authentication

import org.freedesktop.dbus.exceptions.DBusExecutionException

/**
 * @short DBus Wrapper for org.springframework.security.authentication.BadCredentialsException
 */
public class BadCredentialsDBusException extends AuthenticationDBusException {
    public BadCredentialsDBusException(String msg) {
        super(msg)
    }
}
