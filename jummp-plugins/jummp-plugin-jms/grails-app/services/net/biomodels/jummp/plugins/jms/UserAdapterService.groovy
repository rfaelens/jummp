package net.biomodels.jummp.plugins.jms

import org.springframework.beans.factory.InitializingBean
import org.perf4j.aop.Profiled
import org.springframework.security.authentication.BadCredentialsException
import net.biomodels.jummp.plugins.security.User
import net.biomodels.jummp.core.JummpException

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
class UserAdapterService extends CoreAdapterService implements InitializingBean {

    static transactional = false

    void afterPropertiesSet() {
        adapterServiceName = "userJmsAdapter"
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
     */
    @Profiled(tag="userAdapterService.editUser")
    public void editUser(User user) {
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
     * @returnThe (security sanitized) user
     * @throws IllegalArgumentException Thrown if there is no User for @p username
     */
    @Profiled(tag="userAdapterService.getUser")
    public User getUser(String username) throws IllegalArgumentException {
        def retVal = send("getUser", username)
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
     * @throws IllegalArgumentException If the user specified by @p userId does not exist
     */
    @Profiled(tag="userAdapterService.enableUser")
    Boolean enableUser(Long userId, Boolean enable) throws IllegalArgumentException {
        def retVal = send("enableUser", [userId, enable])
        validateReturnValue(retVal, Boolean)
        return (Boolean)retVal
    }

    /**
     * (Un)Locks the account for user identified by @p userId
     * @param userId The unique id of the user
     * @param lock if @c true the account is locked, if @c false the account is unlocked
     * @return @c true, if the account locked state was changed, @c false if the user was already in @p lock state
     * @throws IllegalArgumentException If the user specified by @p userId does not exist
     */
    @Profiled(tag="userAdapterService.lockAccount")
    Boolean lockAccount(Long userId, Boolean lock) throws IllegalArgumentException {
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
    Boolean expireAccount(Long userId, Boolean expire) throws IllegalArgumentException {
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
    Boolean expirePassword(Long userId, Boolean expire) throws IllegalArgumentException {
        def retVal = send("expirePassword", [userId, expire])
        validateReturnValue(retVal, Boolean)
        return (Boolean)retVal
    }

    /**
     * Registers a new User.
     * @param user The new User to register
     * @throws JummpException Thrown in case the user could not be registered
     */
    @Profiled(tag="userAdapterService.register")
    void register(User user) throws JummpException {
        validateReturnValue(send("register", user), Boolean)
    }

    /**
     * Validates the registration code of a new user.
     * @param username The name of the new user
     * @param code The validation code
     * @throws JummpException Thrown in case that the validation cannot be performed
     */
    @Profiled(tag="userAdapterService.register")
    void validateRegistration(String username, String code) throws JummpException {
        validateReturnValue(send("validateRegistration", [username, code]), Boolean)
    }

    /**
     * Request a new password for user identified by @p username.
     * @param username The login id of the user whose password should be reset.
     * @throws JummpException Thrown if there is no user with @p username
     */
    @Profiled(tag="userAdapterService.requestPassword")
    void requestPassword(String username) throws JummpException {
        validateReturnValue(send("requestPassword", username), Boolean)
    }

    /**
     * Resets the Password of the user.
     * @param code The Password Reset Code
     * @param username The Login Id of the User
     * @param password The new Password
     * @throws JummpException Thrown in case user is not found or the code is not valid
     */
    @Profiled(tag="userAdapterService.resetPassword")
    void resetPassword(String code, String username, String password) throws JummpException {
        validateReturnValue(send("resetPassword", [code, username, password]), Boolean)
    }
}
