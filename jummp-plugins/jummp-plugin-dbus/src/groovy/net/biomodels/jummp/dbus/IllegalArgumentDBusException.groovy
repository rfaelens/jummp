package net.biomodels.jummp.dbus

import org.freedesktop.dbus.exceptions.DBusExecutionException

/**
 * @short DBus Wrapper for IllegalArgumentException.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
public class IllegalArgumentDBusException extends DBusExecutionException {
    public IllegalArgumentDBusException(String msg) {
        super(msg)
    }
}
