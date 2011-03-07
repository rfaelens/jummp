package net.biomodels.jummp.core

import org.springframework.security.authentication.BadCredentialsException
import net.biomodels.jummp.plugins.security.User
import org.springframework.security.access.prepost.PreAuthorize

/**
 * @short Service for User administration.
 *
 * This service is meant for any kind of user management, such as changing password
 * and administrative tasks like enabling/disabling users, etc.
 * 
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class UserService {

    static transactional = true
    /**
     * Dependency injection of springSecurityService
     */
    def springSecurityService

    /**
     * Changes the password of the currently logged in user.
     * This method resets the password expired field of the user.
     * The change password functionality only supports passwords stored in the database
     * and not users authenticated through LDAP.
     * @param oldPassword The old password for verification
     * @param newPassword The new password to be used
     * @throws BadCredentialsException if @p oldPassword is incorrect
     */
    void changePassword(String oldPassword, String newPassword) throws BadCredentialsException {
        User user = (User)springSecurityService.getCurrentUser()
        if (user.password != springSecurityService.encodePassword(oldPassword, null)) {
            throw new BadCredentialsException("Cannot change password, old password is incorrect")
        }
        // TODO: verify password strength?
        user.password = springSecurityService.encodePassword(newPassword, null)
        user.passwordExpired = false
        user.save()
        springSecurityService.reauthenticate(user.username, newPassword)
    }

    /**
     * Edit the non-security related parts of a user.
     *
     * This method might be used by an administrator or by the user itself to change the
     * parts of the user object which are not security related.
     * @param user The User with the updated fields
     */
    @PreAuthorize("hasRole('ROLE_ADMIN') or authentication.name==#user.username")
    void editUser(User user) {
        User origUser = User.findByUsername(user.username)
        origUser.userRealName = user.userRealName
        origUser.email = user.email
        if (!origUser.validate()) {
            throw new IllegalArgumentException("User does not validate")
        }
        origUser.save(flush: true)
    }

    /**
     *
     * @return The current (security sanitized) user
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    User getCurrentUser() {
        return ((User)springSecurityService.getCurrentUser()).sanitizedUser()
    }

    /**
     * Retrieves a User object for the given @p username.
     * The returned object is sanitized to not include any security relevant data.
     * @param username The login identifier of the user to be retrieved
     * @return The (security sanitized) user
     * @throws IllegalArgumentException Thrown if there is no User for @p username
     */
    @PreAuthorize("hasRole('ROLE_ADMIN') or authentication.name==#username")
    User getUser(String username) {
        User user = User.findByUsername(username)
        if (!user) {
            throw new IllegalArgumentException("No user for given username")
        }
        return user.sanitizedUser()
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
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    List<User> getAllUsers(Integer offset, Integer count) {
        return User.list([offset: offset, max: Math.min(count, 100)])
    }

    /**
     * Enables/Disables the user identified by @p userId
     * @param userId The unique id of the user
     * @param enable if @c true the user is enabled, if @c false the user is disabled
     * @return @c true, if the enable state was changed, @c false if the user was already in @p enable state
     * @throws IllegalArgumentException If the user specified by @p userId does not exist
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    Boolean enableUser(Long userId, Boolean enable) throws IllegalArgumentException {
        User user = User.get(userId)
        if (!user) {
            throw new IllegalArgumentException("No user for given id")
        }
        if (user.enabled != enable) {
            user.enabled = enable
            user.save(flush: true)
            return (User.get(userId).enabled == enable)
        } else {
            return false
        }
    }
}
