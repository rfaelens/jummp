package net.biomodels.jummp.dbus.user

import org.freedesktop.dbus.exceptions.DBusExecutionException

/**
 * @short DBus Wrapper around net.biomodels.jummp.core.user.UserInvalidException
 */
class UserInvalidDBusException extends DBusExecutionException {
    public UserInvalidDBusException(String msg) {
        super(msg)
    }
}
