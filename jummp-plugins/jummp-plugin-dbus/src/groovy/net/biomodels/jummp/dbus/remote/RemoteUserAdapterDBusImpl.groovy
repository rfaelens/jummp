package net.biomodels.jummp.dbus.remote

import net.biomodels.jummp.core.user.RegistrationException
import net.biomodels.jummp.core.user.RoleNotFoundException
import net.biomodels.jummp.core.user.UserInvalidException
import net.biomodels.jummp.core.user.UserManagementException
import net.biomodels.jummp.core.user.UserNotFoundException
import net.biomodels.jummp.dbus.DBusAuthentication
import net.biomodels.jummp.dbus.DBusUser
import net.biomodels.jummp.dbus.UserDBusAdapter
import net.biomodels.jummp.dbus.user.UserManagementDBusException
import net.biomodels.jummp.plugins.security.Role
import net.biomodels.jummp.plugins.security.User
import net.biomodels.jummp.remote.RemoteUserAdapter
import org.freedesktop.dbus.DBusConnection
import org.perf4j.aop.Profiled
import org.springframework.beans.factory.InitializingBean
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder

/**
 * @short DBus Implementation of the RemoteUserAdapter.
 *
 * The Bean delegates all method calls to the User object exported on the DBus and
 * translates the thrown DBus exceptions to the appropriate Application level exceptions.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class RemoteUserAdapterDBusImpl implements RemoteUserAdapter, InitializingBean {
    private DBusConnection connection
    private UserDBusAdapter userDBusAdapter

    public void afterPropertiesSet() throws Exception {
        connection =  DBusConnection.getConnection(DBusConnection.SESSION)
        userDBusAdapter = (UserDBusAdapter)connection.getRemoteObject("net.biomodels.jummp", "/User", UserDBusAdapter.class)
    }

    @Profiled(tag="RemoteUserAdapterDBusImpl.changePassword")
    void changePassword(String oldPassword, String newPassword) throws BadCredentialsException {
        userDBusAdapter.changePassword(authenticationToken(), oldPassword, newPassword)
    }

    @Profiled(tag="RemoteUserAdapterDBusImpl.editUser")
    void editUser(User user) throws UserInvalidException {
        try {
            userDBusAdapter.editUser(authenticationToken(), DBusUser.fromUser(user))
        } catch (UserManagementDBusException e) {
            throw new UserInvalidException(user.username)
        }
    }

    @Profiled(tag="RemoteUserAdapterDBusImpl.getCurrentUser")
    User getCurrentUser() {
        return userDBusAdapter.getCurrentUser(authenticationToken()).toUser()
    }

    @Profiled(tag="RemoteUserAdapterDBusImpl.getUser")
    User getUser(String username) throws UserNotFoundException {
        try {
            return userDBusAdapter.getUserByName(authenticationToken(), username).toUser()
        } catch (UserManagementDBusException e) {
            throw new UserNotFoundException(username)
        }
    }

    @Profiled(tag="RemoteUserAdapterDBusImpl.getUser")
    User getUser(Long id) throws UserNotFoundException {
        try {
            return userDBusAdapter.getUserById(authenticationToken(), id).toUser()
        } catch (UserManagementDBusException e) {
            throw new UserNotFoundException(id)
        }
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

    @Profiled(tag="RemoteUserAdapterDBusImpl.enableUser")
    Boolean enableUser(Long userId, Boolean enable) throws UserNotFoundException {
        try {
            return userDBusAdapter.enableUser(authenticationToken(), userId, enable)
        } catch (UserManagementDBusException e) {
            throw new UserNotFoundException(userId)
        }
    }

    @Profiled(tag="RemoteUserAdapterDBusImpl.lockAccount")
    Boolean lockAccount(Long userId, Boolean lock) throws UserNotFoundException {
        try {
            return userDBusAdapter.lockAccount(authenticationToken(), userId, lock)
        } catch (UserManagementDBusException e) {
            throw new UserNotFoundException(userId)
        }
    }

    @Profiled(tag="RemoteUserAdapterDBusImpl.expireAccount")
    Boolean expireAccount(Long userId, Boolean expire) throws UserNotFoundException {
        try {
            return userDBusAdapter.expireAccount(authenticationToken(), userId, expire)
        } catch (UserManagementDBusException e) {
            throw new UserNotFoundException(userId)
        }
    }

    @Profiled(tag="RemoteUserAdapterDBusImpl.expirePassword")
    Boolean expirePassword(Long userId, Boolean expire) throws UserNotFoundException {
        try {
            return userDBusAdapter.expirePassword(authenticationToken(), userId, expire)
        } catch (UserManagementDBusException e) {
            throw new UserNotFoundException(userId)
        }
    }

    @Profiled(tag="RemoteUserAdapterDBusImpl.register")
    Long register(User user) throws RegistrationException, UserInvalidException {
        return userDBusAdapter.register(DBusUser.fromUser(user))
        // TODO: catch all possible exceptions
    }

    @Profiled(tag="RemoteUserAdapterDBusImpl.validateRegistration")
    void validateRegistration(String username, String code) throws UserManagementException {
        userDBusAdapter.validateRegistration(username, code)
        // TODO: catch all possible exceptions
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

    @Profiled(tag="RemoteUserAdapterDBusImpl.requestPassword")
    void requestPassword(String username) throws UserNotFoundException {
        try {
            userDBusAdapter.requestPassword(username)
        } catch (UserManagementDBusException e) {
            throw new UserNotFoundException(username)
        }
    }

    @Profiled(tag="RemoteUserAdapterDBusImpl.resetPassword")
    void resetPassword(String code, String username, String password) throws UserNotFoundException {
        // TODO: implement me
    }

    @Profiled(tag="RemoteUserAdapterDBusImpl.getAllRoles")
    List<Role> getAllRoles() {
        // TODO: implement me
        return []
    }

    @Profiled(tag="RemoteUserAdapterDBusImpl.getRolesForUser")
    List<Role> getRolesForUser(Long id) {
        // TODO: implement me
        return []
    }

    @Profiled(tag="RemoteUserAdapterDBusImpl.addRoleToUser")
    void addRoleToUser(Long userId, Long roleId) throws UserNotFoundException, RoleNotFoundException {
        // TODO: implement me
    }

    @Profiled(tag="RemoteUserAdapterDBusImpl.removeRoleFromUser")
    void removeRoleFromUser(Long userId, Long roleId) throws UserNotFoundException, RoleNotFoundException {
        // TODO: implement me
    }

    private String authenticationToken() {
        Authentication auth = SecurityContextHolder.context.authentication
        if (auth instanceof AnonymousAuthenticationToken) {
            return "anonymous"
        } else if (auth instanceof DBusAuthentication) {
            return ((DBusAuthentication)auth).getHash()
        } else {
            return ""
        }
    }
}
