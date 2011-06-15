package net.biomodels.jummp.dbus;

import net.biomodels.jummp.core.IUserService;
import net.biomodels.jummp.dbus.authentication.AuthenticationHashNotFoundDBusException;
import net.biomodels.jummp.dbus.user.*;

import net.biomodels.jummp.webapp.ast.DBusAdapter
import net.biomodels.jummp.webapp.ast.DBusMethod

/**
 * @short Concrete implementation of interface UserDBusAdapter.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
@DBusAdapter(interfaceName="UserDBusAdapter", serviceName="userService")
public class UserDBusAdapterImpl extends AbstractDBusAdapter implements UserDBusAdapter {
    /**
     * Dependency Injection of UserService
     */
    private IUserService userService;

    /**
     * Empty default constructor.
     */
    public UserDBusAdapterImpl() {}

    @DBusMethod(isAuthenticate = true)
    public void changePassword(String authenticationHash, String oldPassword, String newPassword) throws AuthenticationHashNotFoundDBusException {
    }

    @DBusMethod(isAuthenticate = true)
    public void editUser(String authenticationHash, DBusUser user) throws AuthenticationHashNotFoundDBusException {
    }

    @DBusMethod(isAuthenticate = true)
    public DBusUser getCurrentUser(String authenticationHash) throws AuthenticationHashNotFoundDBusException {
    }

    @DBusMethod(isAuthenticate = true, collect = "id")
    public List<String> getAllUsers(String authenticationHash, int offset, int count) throws AuthenticationHashNotFoundDBusException {
    }

    @DBusMethod(isAuthenticate = true)
    public boolean enableUser(String authenticationHash, Long userId, boolean enable) throws AuthenticationHashNotFoundDBusException {
    }

    @DBusMethod(isAuthenticate = true)
    public boolean lockAccount(String authenticationHash, Long userId, boolean lock) throws AuthenticationHashNotFoundDBusException {
    }

    @DBusMethod(isAuthenticate = true)
    public boolean expireAccount(String authenticationHash, Long userId, boolean expire) throws AuthenticationHashNotFoundDBusException {
    }

    @DBusMethod(isAuthenticate = true)
    public boolean expirePassword(String authenticationHash, Long userId, boolean expire) throws AuthenticationHashNotFoundDBusException {
    }

    @DBusMethod(isAnonymous = true)
    public Long register(DBusUser user) {
    }

    @DBusMethod(isAnonymous = true)
    public void validateRegistration(String username, String code) {
    }

    @DBusMethod(isAnonymous = true)
    public void validateAdminRegistration(String username, String code, String password) {
    }

    @DBusMethod(isAnonymous = true)
    public void requestPassword(String username) {
    }

    @DBusMethod(isAuthenticate = true, delegate = "getUser")
    public DBusUser getUserById(String authenticationHash, Long id) throws AuthenticationHashNotFoundDBusException, UserManagementDBusException {
    }

    @DBusMethod(isAuthenticate = true, delegate = "getUser")
    public DBusUser getUserByName(String authenticationHash, String username) throws AuthenticationHashNotFoundDBusException, UserManagementDBusException {
    }

    @DBusMethod(isAuthenticate = true, collect = "authority")
    public List<String> getAllRoles(String authenticationHash) throws AuthenticationHashNotFoundDBusException {
    }

    @DBusMethod(isAuthenticate = true, collect = "authority")
    public List<String> getRolesForUser(String authenticationHash, Long userId) throws AuthenticationHashNotFoundDBusException {
    }

    @DBusMethod(isAuthenticate = true)
    public void addRoleToUser(String authenticationHash, Long userId, Long roleId) throws AuthenticationHashNotFoundDBusException, UserNotFoundDBusException, RoleNotFoundDBusException {
    }

    @DBusMethod(isAuthenticate = true)
    public void removeRoleFromUser(String authenticationHash, Long userId, Long roleId) throws AuthenticationHashNotFoundDBusException, UserNotFoundDBusException, RoleNotFoundDBusException {
    }

    @DBusMethod(isAuthenticate = true)
    public DBusRole getRoleByAuthority(String authenticationHash, String authority) throws AuthenticationHashNotFoundDBusException, RoleNotFoundDBusException {
    }

    @DBusMethod(isAnonymous = true)
    public void resetPassword(String code, String username, String password) throws UserNotFoundDBusException, UserCodeInvalidDBusException, UserCodeExpiredDBusException {
    }

    /**
     * Setter for Dependency Injection of UserService.
     * @param userService
     */
    public void setUserService(IUserService userService) {
        this.userService = userService;
    }
}
