package net.biomodels.jummp.dbus;

import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusInterfaceName;

/**
 * @short DBus INterface for MiriamService.
 */
@DBusInterfaceName("net.biomodels.jummp.miriam")
public interface MiriamDBusAdapter extends DBusInterface {
    // TODO: add throws
    public void updateMiriamResources(String authenticationHash, String url, boolean force);
    public String miriamData(String authenticationHash, String urn);
}
