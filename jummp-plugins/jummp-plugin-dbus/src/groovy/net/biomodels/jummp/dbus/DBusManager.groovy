package net.biomodels.jummp.dbus

import org.freedesktop.dbus.DBusConnection

/**
 * @short Interface to manage a connection to DBus.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
public interface DBusManager {
    /**
     *
     * @return The connection to DBus managed by the manager.
     */
    public DBusConnection getConnection()
}
