package net.biomodels.jummp.plugins.jms

import org.perf4j.aop.Profiled
import org.springframework.security.authentication.BadCredentialsException
import net.biomodels.jummp.plugins.security.User

import net.biomodels.jummp.plugins.security.Role
import net.biomodels.jummp.core.user.UserNotFoundException
import net.biomodels.jummp.core.user.RoleNotFoundException
import net.biomodels.jummp.core.user.UserInvalidException
import net.biomodels.jummp.core.user.RegistrationException
import net.biomodels.jummp.core.user.UserManagementException

/**
 * @short Service delegating to UserService of the core via synchronous JMS
 *
 * This service communicates with UserJmsAdapterService in core through JMS. The
 * service takes care of wrapping parameters into messages and evaluating returned
 * values.
 *
 * Any other Grails artifact can use this service as any other service. The fact that
 * it uses JMS internally is completely transparent to the users of this service.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class UserAdapterService extends AbstractJmsRemoteAdapter  {

    static transactional = false
    private static final String ADAPTER_SERVICE_NAME = "userJmsAdapter"

    protected String getAdapterServiceName() {
        return ADAPTER_SERVICE_NAME
    }

    /**
     * Changes the password of the currently logged in user.
     * @param oldPassword The old password for verification
     * @param newPassword The new password to be used
     * @throws org.springframework.security.authentication.BadCredentialsException if @p oldPassword is incorrect
     * @todo Maybe better in an own service?
     */
    @Profiled(tag="userAdapterService.changePassword")
    public void changePassword(String oldPassword, String newPassword) throws BadCredentialsException {
        validateReturnValue(send("changePassword", [oldPassword, newPassword]), Boolean)
    }

    /**
     * Edit the non-security related parts of a user.
     * @param user The User with the updated fields
     * @throws UserInvalidException If the modified user does not validate
     */
    @Profiled(tag="userAdapterService.editUser")
    public void editUser(User user) throws UserInvalidException {
        validateReturnValue(send("editUser", user), Boolean)
    }

    /**
     *
     * @return The current (security sanitized) user
     */
    @Profiled(tag="userAdapterService.getCurrentUser")
    public User getCurrentUser() {
        def retVal = send("getCurrentUser")
        validateReturnValue(retVal, User)
        return (User)retVal
    }

    /**
     * Retrieves a User object for the given @p username.
     * @param username The login identifier of the user to be retrieved
     * @return The (security sanitized) user
     * @throws UserNotFoundException Thrown if there is no User for @p username
     */
    @Profiled(tag="userAdapterService.getUser")
    public User getUser(String username) throws UserNotFoundException {
        def retVal = send("getUser", username)
        validateReturnValue(retVal, User)
        return (User)retVal
    }

    /**
     * Retrieves a User object for the given @p username.
     * @param id The user Id
     * @return The (security sanitized) user
     * @throws UserNotFoundException Thrown if there is no User for @p username
     */
    @Profiled(tag="userAdapterService.getUser")
    public User getUser(Long id) throws UserNotFoundException {
        def retVal = send("getUser", id)
        validateReturnValue(retVal, User)
        return (User)retVal
    }

    /**
     * Retrieves list of users.
     * This method is only for administrative purpose. It does not sanitize the
     * returned Users, that is it includes all (also security relevant) elements.
     * The method only exists in a paginated version
     * @param offset Offset in the list
     * @param count Number of Users to return, Maximum is 100
     * @return List of Users ordered by Id
     */
    @Profiled(tag="userAdapterService.getUser")
    public List<User> getAllUsers(Integer offset, Integer count) {
        def retVal = send("getAllUsers", [offset, count])
        validateReturnValue(retVal, List)
        return (List<User>)retVal
    }

    /**
     * Enables/Disables the user identified by @p userId
     * @param userId The unique id of the user
     * @param enable if @c true the user is enabled, if @c false the user is disabled
     * @return @c true, if the enable state was changed, @c false if the user was already in @p enable state
     * @throws UserNotFoundException If the user specified by @p userId does not exist
     */
    @Profiled(tag="userAdapterService.enableUser")
    Boolean enableUser(Long userId, Boolean enable) throws UserNotFoundException {
        def retVal = send("enableUser", [userId, enable])
        validateReturnValue(retVal, Boolean)
        return (Boolean)retVal
    }

    /**
     * (Un)Locks the account for user identified by @p userId
     * @param userId The unique id of the user
     * @param lock if @c true the account is locked, if @c false the account is unlocked
     * @return @c true, if the account locked state was changed, @c false if the user was already in @p lock state
     * @throws UserNotFoundException If the user specified by @p userId does not exist
     */
    @Profiled(tag="userAdapterService.lockAccount")
    Boolean lockAccount(Long userId, Boolean lock) throws UserNotFoundException {
        def retVal = send("lockAccount", [userId, lock])
        validateReturnValue(retVal, Boolean)
        return (Boolean)retVal
    }

    /**
     * (Un)Expires the account for user identified by @p userId
     * @param userId The unique id of the user
     * @param expire if @c true the account is expired, if @c false the account is un-expired
     * @return @c true, if the account expired state was changed, @c false if the user was already in @p expire state
     * @throws IllegalArgumentException If the user specified by @p userId does not exist
     */
    @Profiled(tag="userAdapterService.expireAccount")
    Boolean expireAccount(Long userId, Boolean expire) throws UserNotFoundException {
        def retVal = send("expireAccount", [userId, expire])
        validateReturnValue(retVal, Boolean)
        return (Boolean)retVal
    }

    /**
     * (Un)Expires the password for user identified by @p userId
     * @param userId The unique id of the user
     * @param expire if @c true the password is expired, if @c false the password is un-expired
     * @return @c true, if the password expired state was changed, @c false if the password was already in @p expire state
     * @throws IllegalArgumentException If the user specified by @p userId does not exist
     */
    @Profiled(tag="userAdapterService.expirePassword")
    Boolean expirePassword(Long userId, Boolean expire) throws UserNotFoundException {
        def retVal = send("expirePassword", [userId, expire])
        validateReturnValue(retVal, Boolean)
        return (Boolean)retVal
    }

    /**
     * Registers a new User.
     * @param user The new User to register
     * @return Id of new created user
     * @throws RegistrationException In case a user with same name already exists
     * @throws UserInvalidException In case the new user does not validate
     */
    @Profiled(tag="userAdapterService.register")
    Long register(User user) throws RegistrationException, UserInvalidException {
        def retVal = send("register", user)
        validateReturnValue(retVal, Long)
        return (Long)retVal
    }

    /**
     * Validates the registration code of a new user.
     * @param username The name of the new user
     * @param code The validation code
     * @throws UserManagementException Thrown in case that the validation cannot be performed
     */
    @Profiled(tag="userAdapterService.register")
    void validateRegistration(String username, String code) throws UserManagementException {
        validateReturnValue(send("validateRegistration", [username, code]), Boolean)
    }

    /**
     * Validates the registration code of a new user registered by an admin.
     * @param username The name of the new user
     * @param code The validation code
     * @throws UserManagementException Thrown in case that the validation cannot be performed
     */
    @Profiled(tag="userAdapterService.validateAdminRegistration")
    void validateAdminRegistration(String username, String code) throws UserManagementException {
        validateReturnValue(send("validateAdminRegistration", [username, code]), Boolean)
    }

    /**
     * Validates the registration code of a new user registered by an admin.
     * @param username The name of the new user
     * @param code The validation code
     * @param password The user's new password
     * @throws UserManagementException Thrown in case that the validation cannot be performed
     */
    @Profiled(tag="userAdapterService.validateAdminRegistration")
    void validateAdminRegistration(String username, String code, String password) throws UserManagementException {
        validateReturnValue(send("validateAdminRegistration", [username, code, password]), Boolean)
    }

    /**
     * Request a new password for user identified by @p username.
     * @param username The login id of the user whose password should be reset.
     * @throws UserNotFoundException Thrown if there is no user with @p username
     */
    @Profiled(tag="userAdapterService.requestPassword")
    void requestPassword(String username) throws UserNotFoundException {
        validateReturnValue(send("requestPassword", username), Boolean)
    }

    /**
     * Resets the Password of the user.
     * @param code The Password Reset Code
     * @param username The Login Id of the User
     * @param password The new Password
     * @throws UserManagementException Thrown in case user is not found or the code is not valid
     */
    @Profiled(tag="userAdapterService.resetPassword")
    void resetPassword(String code, String username, String password) throws UserManagementException {
        validateReturnValue(send("resetPassword", [code, username, password]), Boolean)
    }

    /**
     * Retrieves all available roles from the Core Jummp Instance.
     * As this is an admin method it does not provide a paginated version
     * @return List of all Roles
     */
    @Profiled(tag="userAdapterService.getAllRoles")
    List<Role> getAllRoles() {
        def retVal = send("getAllRoles")
        validateReturnValue(retVal, List)
        return (List<Role>)retVal
    }

    /**
     * Retrieves the Roles for the User identified by @p id.
     *
     * In case there is no user with @p id an empty list is returned.
     * @param id The user id
     * @return List of Roles assigned to the user
     */
    @Profiled(tag="userAdapterService.getRolesForUser")
    List<Role> getRolesForUser(Long id) {
        def retVal = send("getRolesForUser", id)
        validateReturnValue(retVal, List)
        return (List<Role>)retVal
    }

    /**
     * Adds a Role to the user.
     * If the user already has the role, the user is not changed and no feedback for this situation is
     * provided. The method only ensures that the user has the role after execution.
     * @param userId The id of the user who should receive a new role
     * @param roleId The id or the role to be added to the user
     * @throws UserNotFoundException In case there is no user with @p userId
     * @throws RoleNotFoundException In case there is no role with @p roleId
     */
    @Profiled(tag="userAdapterService.addRoleToUser")
    void addRoleToUser(Long userId, Long roleId) throws UserNotFoundException, RoleNotFoundException {
        validateReturnValue(send("addRoleToUser", [userId, roleId]), Boolean)
    }

    /**
     * Removes a Role from the user.
     * If the user does not have the role, the user is not changed and no feedback for this situation is
     * provided. The method only ensures that the user does not have the role after execution.
     * @param userId The id of the user from whom the role should be removed
     * @param roleId The id or the role to be removed from the user
     * @throws UserNotFoundException In case there is no user with @p userId
     * @throws RoleNotFoundException In case there is no role with @p roleId
     */
    @Profiled(tag="userAdapterService.removeRoleFromUser")
    void removeRoleFromUser(Long userId, Long roleId) throws UserNotFoundException, RoleNotFoundException {
        validateReturnValue(send("removeRoleFromUser", [userId, roleId]), Boolean)
    }
}
