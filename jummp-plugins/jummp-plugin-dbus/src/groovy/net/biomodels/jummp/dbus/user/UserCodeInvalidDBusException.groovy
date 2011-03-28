package net.biomodels.jummp.dbus.user

import org.freedesktop.dbus.exceptions.DBusExecutionException

/**
 * @short DBus Wrapper around UserCodeInvalidException.
 */
class UserCodeInvalidDBusException extends DBusExecutionException {
    public UserCodeInvalidDBusException(String message) {
        super(message)
    }
}
