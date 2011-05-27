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
class RemoteUserAdapterDBusImpl extends AbstractRemoteAdapter implements RemoteUserAdapter, InitializingBean {
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

    @Profiled(tag="RemoteUserAdapterDBusImpl.getAllUsers")
    List<User> getAllUsers(Integer offset, Integer count) {
        List<String> ids = userDBusAdapter.getAllUsers(authenticationToken(), offset, count)
        List<User> users = []
        ids.each {
            users << userDBusAdapter.getUserById(authenticationToken(), it as Long).toUser()
        }
        return users
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

    @Profiled(tag="RemoteUserAdapterDBusImpl.getAllRoles")
    List<Role> getAllRoles() {
        List<String> roles = userDBusAdapter.getAllRoles(authenticationToken())
        List<Role> returnVal = []
        roles.each {
            returnVal << userDBusAdapter.getRoleByAuthority(authenticationToken(), it).toRole()
        }
        return returnVal
    }

    @Profiled(tag="RemoteUserAdapterDBusImpl.getRolesForUser")
    List<Role> getRolesForUser(Long id) {
        List<String> roles = userDBusAdapter.getRolesForUser(authenticationToken(), id)
        List<Role> returnVal = []
        roles.each {
            returnVal << userDBusAdapter.getRoleByAuthority(authenticationToken(), it).toRole()
        }
        return returnVal
    }
}
