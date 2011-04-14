package net.biomodels.jummp.dbus.user

import org.freedesktop.dbus.exceptions.DBusExecutionException

/**
 * @short DBus Wrapper for RoleNotFoundException
 */
class RoleNotFoundDBusException extends DBusExecutionException {
    public RoleNotFoundDBusException(String msg) {
        super(msg)
    }
}
