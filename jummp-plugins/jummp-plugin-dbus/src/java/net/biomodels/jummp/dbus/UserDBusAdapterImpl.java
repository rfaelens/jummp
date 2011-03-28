package net.biomodels.jummp.dbus;

import net.biomodels.jummp.core.IUserService;
import net.biomodels.jummp.core.user.*;
import net.biomodels.jummp.dbus.authentication.AccessDeniedDBusException;
import net.biomodels.jummp.dbus.authentication.AuthenticationHashNotFoundDBusException;
import net.biomodels.jummp.dbus.authentication.BadCredentialsDBusException;
import net.biomodels.jummp.dbus.user.RoleNotFoundDBusException;
import net.biomodels.jummp.dbus.user.UserInvalidDBusException;
import net.biomodels.jummp.dbus.user.UserManagementDBusException;
import net.biomodels.jummp.dbus.user.UserNotFoundDBusException;
import net.biomodels.jummp.plugins.security.Role;
import net.biomodels.jummp.plugins.security.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.ArrayList;
import java.util.List;

/**
 * @short Concrete implementation of interface UserDBusAdapter.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
public class UserDBusAdapterImpl extends AbstractDBusAdapter implements UserDBusAdapter {
    /**
     * Dependency Injection of UserService
     */
    private IUserService userService;

    /**
     * Empty default constructor.
     */
    public UserDBusAdapterImpl() {}

    public void changePassword(String authenticationHash, String oldPassword, String newPassword) throws AuthenticationHashNotFoundDBusException {
        try {
            setAuthentication(authenticationHash);
            userService.changePassword(oldPassword, newPassword);
        } catch (BadCredentialsException e) {
            throw new BadCredentialsDBusException(e.getMessage());
        } finally {
            restoreAuthentication();
        }
    }

    public void editUser(String authenticationHash, DBusUser user) throws AuthenticationHashNotFoundDBusException {
        try {
            setAuthentication(authenticationHash);
            userService.editUser(user.toUser());
        } catch (AccessDeniedException e) {
            throw new AccessDeniedDBusException(e.getMessage());
        } catch (UserInvalidException e) {
            throw new UserInvalidDBusException(e.getUserName());
        } finally {
            restoreAuthentication();
        }
    }

    public DBusUser getCurrentUser(String authenticationHash) throws AuthenticationHashNotFoundDBusException {
        try {
            setAuthentication(authenticationHash);
            return DBusUser.fromUser(userService.getCurrentUser());
        } catch (AccessDeniedException e) {
            throw new AccessDeniedDBusException(e.getMessage());
        } finally {
            restoreAuthentication();
        }
    }

    public List<String> getAllUsers(String authenticationHash, int offset, int count) throws AuthenticationHashNotFoundDBusException {
        try {
            setAuthentication(authenticationHash);
            List<String> userNames = new ArrayList<String>();
            for (User u : userService.getAllUsers(offset, count)) {
                userNames.add(u.domainId().toString());
            }
            return userNames;
        } catch (AccessDeniedException e) {
            throw new AccessDeniedDBusException(e.getMessage());
        } finally {
            restoreAuthentication();
        }
    }

    public boolean enableUser(String authenticationHash, Long userId, boolean enable) throws AuthenticationHashNotFoundDBusException {
        try {
            setAuthentication(authenticationHash);
            return userService.enableUser(userId, enable);
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

    public boolean lockAccount(String authenticationHash, Long userId, boolean lock) throws AuthenticationHashNotFoundDBusException {
        try {
            setAuthentication(authenticationHash);
            return userService.lockAccount(userId, lock);
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

    public boolean expireAccount(String authenticationHash, Long userId, boolean expire) throws AuthenticationHashNotFoundDBusException {
        try {
            setAuthentication(authenticationHash);
            return userService.expireAccount(userId, expire);
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

    public boolean expirePassword(String authenticationHash, Long userId, boolean expire) throws AuthenticationHashNotFoundDBusException {
        try {
            setAuthentication(authenticationHash);
            return userService.expirePassword(userId, expire);
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
        List<String> returnRoles = new ArrayList<String>();
        try {
            setAuthentication(authenticationHash);
            List<Role> roles = userService.getAllRoles();
            for (Role role : roles) {
                returnRoles.add(role.getAuthority());
            }
        } catch (AccessDeniedException e) {
            throw new AccessDeniedDBusException(e.getMessage());
        } finally {
            restoreAuthentication();
        }
        return returnRoles;
    }

    public List<String> getRolesForUser(String authenticationHash, Long userId) throws AuthenticationHashNotFoundDBusException {
        List<String> returnRoles = new ArrayList<String>();
        try {
            setAuthentication(authenticationHash);
            List<Role> roles = userService.getRolesForUser(userId);
            for (Role role : roles) {
                returnRoles.add(role.getAuthority());
            }
        } catch (AccessDeniedException e) {
            throw new AccessDeniedDBusException(e.getMessage());
        } finally {
            restoreAuthentication();
        }
        return returnRoles;
    }

    public void addRoleToUser(String authenticationHash, Long userId, Long roleId) throws AuthenticationHashNotFoundDBusException, UserNotFoundDBusException, RoleNotFoundDBusException {
        try {
            setAuthentication(authenticationHash);
            userService.addRoleToUser(userId, roleId);
        } catch (RoleNotFoundException e) {
            throw new RoleNotFoundDBusException(e.getMessage());
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

    public void removeRoleFromUser(String authenticationHash, Long userId, Long roleId) throws AuthenticationHashNotFoundDBusException, UserNotFoundDBusException, RoleNotFoundDBusException {
        try {
            setAuthentication(authenticationHash);
            userService.removeRoleFromUser(userId, roleId);
        } catch (RoleNotFoundException e) {
            throw new RoleNotFoundDBusException(e.getMessage());
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

    public boolean isRemote() {
        return false;
    }

    public DBusRole getRoleByAuthority(String authenticationHash, String authority) throws AuthenticationHashNotFoundDBusException, RoleNotFoundDBusException {
        try {
            setAuthentication(authenticationHash);
            return DBusRole.fromRole(userService.getRoleByAuthority(authority));
        } catch (AccessDeniedException e) {
            throw new AccessDeniedDBusException(e.getMessage());
        } catch (RoleNotFoundException e) {
            throw new RoleNotFoundDBusException(e.getMessage());
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
