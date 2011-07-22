package net.biomodels.jummp.dbus.remote

import net.biomodels.jummp.core.miriam.IMiriamService
import net.biomodels.jummp.webapp.ast.RemoteDBusAdapter
import org.freedesktop.dbus.DBusConnection
import net.biomodels.jummp.dbus.MiriamDBusAdapter
import org.springframework.beans.factory.InitializingBean

/**
 * Created by IntelliJ IDEA.
 * User: graessli
 * Date: Jun 27, 2011
 * Time: 1:24:00 PM
 * To change this template use File | Settings | File Templates.
 */
@RemoteDBusAdapter(interfaceName="IMiriamService", dbusAdapterName="miriamDBusAdapter")
class RemoteMiriamAdapterDBusImpl extends AbstractRemoteDBusAdapter implements IMiriamService, InitializingBean {
    private DBusConnection connection
    private MiriamDBusAdapter miriamDBusAdapter

    public void afterPropertiesSet() throws Exception {
        connection = DBusConnection.getConnection(DBusConnection.SYSTEM)
        miriamDBusAdapter = (MiriamDBusAdapter)connection.getRemoteObject("net.biomodels.jummp", "/Miriam", MiriamDBusAdapter.class)
    }
}
