package net.biomodels.jummp.dbus;

import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusInterfaceName;

@DBusInterfaceName("net.biomodels.jummp.go")
public interface GeneOntologyTreeDBusAdapter extends DBusInterface {

    String treeLevel(String authenticationHash, long goId);
}
