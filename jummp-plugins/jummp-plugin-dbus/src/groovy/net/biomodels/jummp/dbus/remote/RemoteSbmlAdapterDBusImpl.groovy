package net.biomodels.jummp.dbus.remote

import net.biomodels.jummp.remote.AbstractRemoteAdapter
import net.biomodels.jummp.remote.RemoteSbmlAdapter
import org.freedesktop.dbus.DBusConnection
import net.biomodels.jummp.dbus.SbmlDBusAdapter
import org.springframework.beans.factory.InitializingBean
import net.biomodels.jummp.webapp.ast.RemoteDBusAdapter

/**
 * @short DBus implementation of RemoteSbmlAdapter.
 *
 * The methods of the RemoteSbmlAdapter interface are auto-generated
 * by the RemoteDBusAdapterTransformation AST Transformation.
 */
@RemoteDBusAdapter(interfaceName="RemoteSbmlAdapter",dbusAdapterName="sbmlDBusAdapter")
class RemoteSbmlAdapterDBusImpl extends AbstractRemoteDBusAdapter implements RemoteSbmlAdapter, InitializingBean {
    private DBusConnection connection
    private SbmlDBusAdapter sbmlDBusAdapter

    public void afterPropertiesSet() throws Exception {
        connection =  DBusConnection.getConnection(DBusConnection.SESSION)
        sbmlDBusAdapter = (SbmlDBusAdapter)connection.getRemoteObject("net.biomodels.jummp", "/SBML", SbmlDBusAdapter.class)
    }
}
