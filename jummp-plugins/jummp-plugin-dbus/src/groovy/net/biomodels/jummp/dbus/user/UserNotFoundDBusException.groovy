package net.biomodels.jummp.dbus.user

import org.freedesktop.dbus.exceptions.DBusExecutionException

/**
 * @short DBus Wrapper for net.biomodels.jummp.core.user.UserNotFoundException
 */
class UserNotFoundDBusException extends DBusExecutionException {
    public UserNotFoundDBusException(String msg) {
        super(msg)
    }
}
