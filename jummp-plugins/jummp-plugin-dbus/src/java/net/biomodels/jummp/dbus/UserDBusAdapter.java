package net.biomodels.jummp.dbus;

import net.biomodels.jummp.dbus.authentication.AuthenticationHashNotFoundDBusException;
import net.biomodels.jummp.dbus.user.UserManagementDBusException;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusInterfaceName;

import java.util.List;

/**
 * @short DBus Interface for UserService.
 *
 * This interface describes the DBus Interface for the service described by IUserService.
 * In opposite to IUserService all methods take an additional first parameter for the
 * authentication identifier. All methods can throw specific DBusExceptions for cases
 * like AccessDeniedException or AuthenticationHashNotFoundException plus the wrappers
 * for the normal to be expected Exceptions from IUserService.
 * The return values are wrappers implementing the DBusSerializable interface.
 * 
 * Please not that the methods are only documented in case they differ from IUserService
 * in more than the just described additions. For documentation on the methods please
 * refer to IUserService.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 * @see net.biomodels.jummp.core.IUserService
 */
@DBusInterfaceName("net.biomodels.jummp.user")
public interface UserDBusAdapter extends DBusInterface {
    void changePassword(String authenticationHash, String oldPassword, String newPassword) throws AuthenticationHashNotFoundDBusException;
    void editUser(String authenticationHash, DBusUser user) throws AuthenticationHashNotFoundDBusException;
    DBusUser getCurrentUser(String authenticationHash) throws AuthenticationHashNotFoundDBusException;

    /**
     * Wrapper for getAllUsers. Returns List of ids wrapped in a string list.
     * @param authenticationHash
     * @param offset
     * @param count
     * @return List of User-Ids
     * @throws AuthenticationHashNotFoundDBusException
     */
    List<String> getAllUsers(String authenticationHash, int offset, int count) throws AuthenticationHashNotFoundDBusException;
    boolean enableUser(String authenticationHash, Long userId, boolean enable) throws AuthenticationHashNotFoundDBusException;
    boolean lockAccount(String authenticationHash, Long userId, boolean lock) throws AuthenticationHashNotFoundDBusException;
    boolean expireAccount(String authenticationHash, Long userId, boolean expire) throws AuthenticationHashNotFoundDBusException;
    boolean expirePassword(String authenticationHash, Long userId, boolean expire) throws AuthenticationHashNotFoundDBusException;
    Long register(DBusUser user);
    void validateRegistration(String username, String code);
    void validateAdminRegistration(String username, String code, String password);
    void requestPassword(String username);

    /**
     * Wrapper around getUser. Needed as DBus does not support method overloading.
     * @param authenticationHash
     * @param id
     * @return
     * @throws AuthenticationHashNotFoundDBusException if authenticationHash is invalid
     * @throws UserManagementDBusException If user not found
     */
    public DBusUser getUserById(String authenticationHash, Long id) throws AuthenticationHashNotFoundDBusException, UserManagementDBusException;
    /**
     * Wrapper around getUser. Needed as DBus does not support method overloading.
     * @param authenticationHash
     * @param username
     * @return
     * @throws AuthenticationHashNotFoundDBusException if authenticationHash is invalid
     * @throws UserManagementDBusException If user not found
     */
    public DBusUser getUserByName(String authenticationHash, String username) throws AuthenticationHashNotFoundDBusException, UserManagementDBusException;
}
