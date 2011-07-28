package net.biomodels.jummp.dbus
import org.freedesktop.dbus.DBusConnection
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.DisposableBean

/**
 * @short Concrete implementation of the DBusManager interface.
 *
 * This manager bean takes care of creating the connection to DBus and registering the service
 * at the bus. At deconstruction of this bean the bus name is released again.
 *
 * As a matter of fact the registration of the DBus name will fail badly if another instance of
 * this bean has already registered the bus name. This means it is implicitly only possible to
 * have one instance of this bean.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class DBusManagerImpl implements DBusManager, InitializingBean, DisposableBean {
    private DBusConnection connection

    public void afterPropertiesSet() throws Exception {
        connection = DBusConnection.getConnection(DBusConnection.SYSTEM)
        connection.requestBusName("net.biomodels.jummp")
    }

    void destroy() {
        connection.releaseBusName("net.biomodels.jummp")
    }

    public DBusConnection getConnection() {
        return connection
    }
}
