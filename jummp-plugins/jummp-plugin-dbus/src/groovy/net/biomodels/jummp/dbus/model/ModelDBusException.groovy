package net.biomodels.jummp.dbus.model

import org.freedesktop.dbus.exceptions.DBusExecutionException

/**
 * @short DBus wrapper for ModelException.
 *
 * The wrapper does not include the actual ModelTransportCommand.
 * It needs to be recreated when wrapped back to ModelException.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
public class ModelDBusException extends DBusExecutionException  {
    public ModelDBusException(String msg) {
        super(msg)
    }
}
