package net.biomodels.jummp.dbus.remote

import net.biomodels.jummp.core.user.UserManagementException
import net.biomodels.jummp.core.user.UserNotFoundException
import net.biomodels.jummp.dbus.UserDBusAdapter
import net.biomodels.jummp.plugins.security.Role
import net.biomodels.jummp.plugins.security.User
import net.biomodels.jummp.remote.RemoteUserAdapter
import org.freedesktop.dbus.DBusConnection
import org.perf4j.aop.Profiled
import org.springframework.beans.factory.InitializingBean
import net.biomodels.jummp.remote.AbstractRemoteAdapter
import net.biomodels.jummp.webapp.ast.RemoteDBusAdapter

/**
 * @short DBus Implementation of the RemoteUserAdapter.
 *
 * The Bean delegates all method calls to the User object exported on the DBus and
 * translates the thrown DBus exceptions to the appropriate Application level exceptions.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
@RemoteDBusAdapter(interfaceName="RemoteUserAdapter",dbusAdapterName="userDBusAdapter")
class RemoteUserAdapterDBusImpl extends AbstractRemoteDBusAdapter implements RemoteUserAdapter, InitializingBean {
    private DBusConnection connection
    private UserDBusAdapter userDBusAdapter

    public void afterPropertiesSet() throws Exception {
        connection =  DBusConnection.getConnection(DBusConnection.SESSION)
        userDBusAdapter = (UserDBusAdapter)connection.getRemoteObject("net.biomodels.jummp", "/User", UserDBusAdapter.class)
    }

    @Profiled(tag="RemoteUserAdapterDBusImpl.getUser")
    User getUser(String username) throws UserNotFoundException {
        return userDBusAdapter.getUserByName(authenticationToken(), username).toUser()
    }

    @Profiled(tag="RemoteUserAdapterDBusImpl.getUser")
    User getUser(Long id) throws UserNotFoundException {
        return userDBusAdapter.getUserById(authenticationToken(), id).toUser()
    }

    @Profiled(tag="RemoteUserAdapterDBusImpl.validateAdminRegistration")
    void validateAdminRegistration(String username, String code) throws UserManagementException {
        userDBusAdapter.validateAdminRegistration(username, code, "")
        // TODO: catch all possible exceptions
    }

    @Profiled(tag="RemoteUserAdapterDBusImpl.validateAdminRegistration")
    void validateAdminRegistration(String username, String code, String password) throws UserManagementException {
        userDBusAdapter.validateAdminRegistration(username, code, password)
        // TODO: catch all possible exceptions
    }
}
