package net.biomodels.jummp.core

import org.springframework.security.authentication.BadCredentialsException
import net.biomodels.jummp.plugins.security.User

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
}
