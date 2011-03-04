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
}
