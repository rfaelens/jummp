package net.biomodels.jummp.dbus;

import net.biomodels.jummp.core.IUserService;
import net.biomodels.jummp.core.user.*;
import net.biomodels.jummp.dbus.authentication.AccessDeniedDBusException;
import net.biomodels.jummp.dbus.authentication.AuthenticationHashNotFoundDBusException;
import net.biomodels.jummp.dbus.user.*;
import org.springframework.security.access.AccessDeniedException;

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

    public List<String> getAllUsers(String authenticationHash, int offset, int count) throws AuthenticationHashNotFoundDBusException {
        try {
            setAuthentication(authenticationHash);
            return userService.getAllUsers(offset, count).collect { it.domainId().toString() }
        } catch (AccessDeniedException e) {
            throw new AccessDeniedDBusException(e.getMessage());
        } finally {
            restoreAuthentication();
        }
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

    public Long register(DBusUser user) {
        try {
            setAnonymousAuthentication();
            return userService.register(user.toUser());
        } catch (AccessDeniedException e) {
            throw new AccessDeniedDBusException(e.getMessage());
        } catch (UserInvalidException e) {
            throw new UserInvalidDBusException(e.getUserName());
        } catch (RegistrationException e) {
            throw new UserManagementDBusException(e.getMessage());
        } finally {
            restoreAuthentication();
        }
    }

    public void validateRegistration(String username, String code) {
        try {
            setAnonymousAuthentication();
            userService.validateRegistration(username, code);
        } catch (AccessDeniedException e) {
            throw new AccessDeniedDBusException(e.getMessage());
        } catch (UserManagementException e) {
            throw new UserManagementDBusException(e.getMessage());
        } finally {
            restoreAuthentication();
        }
    }

    public void validateAdminRegistration(String username, String code, String password) {
        try {
            setAnonymousAuthentication();
            userService.validateAdminRegistration(username, code, password);
        } catch (AccessDeniedException e) {
            throw new AccessDeniedDBusException(e.getMessage());
        } catch (UserManagementException e) {
            throw new UserManagementDBusException(e.getMessage());
        } finally {
            restoreAuthentication();
        }
    }

    public void requestPassword(String username) {
        try {
            setAnonymousAuthentication();
            userService.requestPassword(username);
        } catch (AccessDeniedException e) {
            throw new AccessDeniedDBusException(e.getMessage());
        } catch (UserNotFoundException e) {
            if (e.getUserName() != null) {
                throw new UserNotFoundDBusException(e.getUserName());
            } else {
                throw new UserNotFoundDBusException(e.getId().toString());
            }
        } finally {
            restoreAuthentication();
        }
    }

    public DBusUser getUserById(String authenticationHash, Long id) throws AuthenticationHashNotFoundDBusException, UserManagementDBusException {
        DBusUser user = null;
        try {
            setAuthentication(authenticationHash);
            user = DBusUser.fromUser(userService.getUser(id));
        } catch (AccessDeniedException e) {
            throw new AccessDeniedDBusException(e.getMessage());
        } catch (UserNotFoundException e) {
            if (e.getUserName() != null) {
                throw new UserNotFoundDBusException(e.getUserName());
            } else {
                throw new UserNotFoundDBusException(e.getId().toString());
            }
        } finally {
            restoreAuthentication();
        }
        return user;
    }

    public DBusUser getUserByName(String authenticationHash, String username) throws AuthenticationHashNotFoundDBusException, UserManagementDBusException {
        DBusUser user = null;
        try {
            setAuthentication(authenticationHash);
            user = DBusUser.fromUser(userService.getUser(username));
        } catch (AccessDeniedException e) {
            throw new AccessDeniedDBusException(e.getMessage());
        } catch (UserNotFoundException e) {
            if (e.getUserName() != null) {
                throw new UserNotFoundDBusException(e.getUserName());
            } else {
                throw new UserNotFoundDBusException(e.getId().toString());
            }
        } finally {
            restoreAuthentication();
        }
        return user;
    }

    public List<String> getAllRoles(String authenticationHash) throws AuthenticationHashNotFoundDBusException {
        try {
            setAuthentication(authenticationHash);
            return userService.getAllRoles().collect { it.authority }
        } catch (AccessDeniedException e) {
            throw new AccessDeniedDBusException(e.getMessage());
        } finally {
            restoreAuthentication();
        }
    }

    public List<String> getRolesForUser(String authenticationHash, Long userId) throws AuthenticationHashNotFoundDBusException {
        try {
            setAuthentication(authenticationHash);
            return userService.getRolesForUser(userId).collect { it.authority }
        } catch (AccessDeniedException e) {
            throw new AccessDeniedDBusException(e.getMessage());
        } finally {
            restoreAuthentication();
        }
    }

    @DBusMethod(isAuthenticate = true)
    public void addRoleToUser(String authenticationHash, Long userId, Long roleId) throws AuthenticationHashNotFoundDBusException, UserNotFoundDBusException, RoleNotFoundDBusException {
    }

    @DBusMethod(isAuthenticate = true)
    public void removeRoleFromUser(String authenticationHash, Long userId, Long roleId) throws AuthenticationHashNotFoundDBusException, UserNotFoundDBusException, RoleNotFoundDBusException {
    }

    public boolean isRemote() {
        return false;
    }

    @DBusMethod(isAuthenticate = true)
    public DBusRole getRoleByAuthority(String authenticationHash, String authority) throws AuthenticationHashNotFoundDBusException, RoleNotFoundDBusException {
    }

    public void resetPassword(String code, String username, String password) throws UserNotFoundDBusException, UserCodeInvalidDBusException, UserCodeExpiredDBusException {
        try {
            setAnonymousAuthentication();
            userService.resetPassword(code, username, password);
        } catch (UserCodeInvalidException e) {
            throw new UserCodeInvalidDBusException(e.getCode());
        } catch (UserCodeExpiredException e) {
            throw new UserCodeExpiredDBusException(e.getUserName());
        } catch (UserNotFoundException e) {
            if (e.getUserName() != null) {
                throw new UserNotFoundDBusException(e.getUserName());
            } else {
                throw new UserNotFoundDBusException(e.getId().toString());
            }
        } finally {
            restoreAuthentication();
        }
    }

    /**
     * Setter for Dependency Injection of UserService.
     * @param userService
     */
    public void setUserService(IUserService userService) {
        this.userService = userService;
    }
}
