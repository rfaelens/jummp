package net.biomodels.jummp.dbus.user

import org.freedesktop.dbus.exceptions.DBusExecutionException

/**
 * @short DBus Wrapper around UserCodeExpiredException.
 */
class UserCodeExpiredDBusException extends DBusExecutionException {
    public UserCodeExpiredDBusException(String msg) {
        super(msg)
    }
}
