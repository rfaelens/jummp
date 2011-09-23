package net.biomodels.jummp.dbus;

import java.util.List;

import net.biomodels.jummp.dbus.model.DBusModel;

import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusInterfaceName;

@DBusInterfaceName("net.biomodels.jummp.modelHistory")
public interface ModelHistoryDBusAdapter extends DBusInterface {

    public List<String> history(String authenticationHash);
    public DBusModel lastAccessedModel(String authenticationHash);

}
