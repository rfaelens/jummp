package net.biomodels.jummp.dbus.authentication

import org.freedesktop.dbus.exceptions.DBusExecutionException

/**
 * @short DBus Wrapper for org.springframework.security.access.AccessDeniedException
 */
public class AccessDeniedDBusException extends DBusExecutionException {
    public AccessDeniedDBusException(String msg) {
        super(msg)
    }
}
