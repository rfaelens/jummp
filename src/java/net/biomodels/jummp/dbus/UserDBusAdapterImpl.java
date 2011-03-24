package net.biomodels.jummp.dbus;

import net.biomodels.jummp.core.IUserService;
import net.biomodels.jummp.core.user.RegistrationException;
import net.biomodels.jummp.core.user.UserInvalidException;
import net.biomodels.jummp.core.user.UserManagementException;
import net.biomodels.jummp.core.user.UserNotFoundException;
import net.biomodels.jummp.dbus.authentication.AccessDeniedDBusException;
import net.biomodels.jummp.dbus.authentication.AuthenticationHashNotFoundDBusException;
import net.biomodels.jummp.dbus.authentication.BadCredentialsDBusException;
import net.biomodels.jummp.dbus.user.UserManagementDBusException;
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

    public void editUser(String authenticationHash, User user) throws AuthenticationHashNotFoundDBusException {
        try {
            setAuthentication(authenticationHash);
            userService.editUser(user.toUser());
        } catch (AccessDeniedException e) {
            throw new AccessDeniedDBusException(e.getMessage());
        } catch (UserInvalidException e) {
            throw new UserManagementDBusException(e.getMessage());
        } finally {
            restoreAuthentication();
        }
    }

    public User getCurrentUser(String authenticationHash) throws AuthenticationHashNotFoundDBusException {
        try {
            setAuthentication(authenticationHash);
            return User.fromUser(userService.getCurrentUser());
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
            for (net.biomodels.jummp.plugins.security.User u : userService.getAllUsers(offset, count)) {
                userNames.add(u.getId().toString());
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
            throw new UserManagementDBusException(e.getMessage());
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
            throw new UserManagementDBusException(e.getMessage());
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
            throw new UserManagementDBusException(e.getMessage());
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
            throw new UserManagementDBusException(e.getMessage());
        } finally {
            restoreAuthentication();
        }
    }

    public Long register(User user) {
        try {
            setAnonymousAuthentication();
            return userService.register(user.toUser());
        } catch (AccessDeniedException e) {
            throw new AccessDeniedDBusException(e.getMessage());
        } catch (UserInvalidException e) {
            throw new UserManagementDBusException(e.getMessage());
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
            throw new UserManagementDBusException(e.getMessage());
        } finally {
            restoreAuthentication();
        }
    }

    public User getUserById(String authenticationHash, Long id) throws AuthenticationHashNotFoundDBusException, UserManagementDBusException {
        User user = null;
        try {
            setAuthentication(authenticationHash);
            user = User.fromUser(userService.getUser(id));
        } catch (AccessDeniedException e) {
            throw new AccessDeniedDBusException(e.getMessage());
        } catch (UserNotFoundException e) {
            throw new UserManagementDBusException(e.getMessage());
        } finally {
            restoreAuthentication();
        }
        return user;
    }

    public User getUserByName(String authenticationHash, String username) throws AuthenticationHashNotFoundDBusException, UserManagementDBusException {
        User user = null;
        try {
            setAuthentication(authenticationHash);
            user = User.fromUser(userService.getUser(username));
        } catch (AccessDeniedException e) {
            throw new AccessDeniedDBusException(e.getMessage());
        } catch (UserNotFoundException e) {
            throw new UserManagementDBusException(e.getMessage());
        } finally {
            restoreAuthentication();
        }
        return user;
    }

    public boolean isRemote() {
        return false;
    }

    /**
     * Setter for Dependency Injection of UserService.
     * @param userService
     */
    public void setUserService(IUserService userService) {
        this.userService = userService;
    }
}
