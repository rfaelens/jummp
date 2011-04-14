package net.biomodels.jummp.dbus.user

import org.freedesktop.dbus.exceptions.DBusExecutionException

/**
 * @short DBus Wrapper for net.biomodels.jummp.core.user.UserManagementException
 */
class UserManagementDBusException extends DBusExecutionException {
    public UserManagementDBusException(String msg) {
        super(msg)
    }
}
