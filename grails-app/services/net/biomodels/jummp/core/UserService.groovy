package net.biomodels.jummp.core

import net.biomodels.jummp.plugins.security.Role
import net.biomodels.jummp.plugins.security.User
import net.biomodels.jummp.plugins.security.UserRole
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.authentication.BadCredentialsException
import net.biomodels.jummp.core.events.LoggingEventType
import net.biomodels.jummp.core.events.PostLogging
import net.biomodels.jummp.core.user.UserNotFoundException
import net.biomodels.jummp.core.user.UserInvalidException
import net.biomodels.jummp.core.user.UserCodeInvalidException
import net.biomodels.jummp.core.user.UserCodeExpiredException
import net.biomodels.jummp.core.user.RegistrationException
import net.biomodels.jummp.core.user.UserManagementException
import net.biomodels.jummp.core.user.RoleNotFoundException

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
     * Dependency injection of mail Service provided by the Mail plugin
     */
    def mailService
    /**
     * Random number generator for creating user validation ids.
     */
    private final Random random = new Random(System.currentTimeMillis())

    /**
     * Changes the password of the currently logged in user.
     * This method resets the password expired field of the user.
     * The change password functionality only supports passwords stored in the database
     * and not users authenticated through LDAP.
     * @param oldPassword The old password for verification
     * @param newPassword The new password to be used
     * @throws BadCredentialsException if @p oldPassword is incorrect
     */
    @PostLogging(LoggingEventType.UPDATE)
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
     * @throws UserInvalidException If the modified user does not validate
     */
    @PostLogging(LoggingEventType.UPDATE)
    @PreAuthorize("hasRole('ROLE_ADMIN') or authentication.name==#user.username")
    void editUser(User user) throws UserInvalidException {
        User origUser = User.findByUsername(user.username)
        origUser.userRealName = user.userRealName
        origUser.email = user.email
        if (!origUser.validate()) {
            throw new UserInvalidException(user.username)
        }
        origUser.save(flush: true)
    }

    /**
     *
     * @return The current (security sanitized) user
     */
    @PostLogging(LoggingEventType.RETRIEVAL)
    @PreAuthorize("hasRole('ROLE_USER')")
    User getCurrentUser() {
        return ((User)springSecurityService.getCurrentUser()).sanitizedUser()
    }

    /**
     * Retrieves a User object for the given @p username.
     * The returned object is sanitized to not include any security relevant data.
     * @param username The login identifier of the user to be retrieved
     * @return The (security sanitized) user
     * @throws UserNotFoundException Thrown if there is no User for @p username
     */
    @PostLogging(LoggingEventType.RETRIEVAL)
    @PreAuthorize("hasRole('ROLE_ADMIN') or authentication.name==#username")
    User getUser(String username) throws UserNotFoundException {
        User user = User.findByUsername(username)
        if (!user) {
            throw new UserNotFoundException(username)
        }
        return user.sanitizedUser()
    }

    /**
     * Retrieves a User object for the given @p username.
     * The returned object is not sanitized and includes any security relevant data.
     * This method is only for admin purpose.
     * @param username The login identifier of the user to be retrieved
     * @return The user
     * @throws UserNotFoundException Thrown if there is no User for @p username
     */
    @PostLogging(LoggingEventType.RETRIEVAL)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    User getUser(Long id) throws UserNotFoundException {
        User user = User.get(id)
        if (!user) {
            throw new UserNotFoundException(id)
        }
        return user
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
    @PostLogging(LoggingEventType.RETRIEVAL)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    List<User> getAllUsers(Integer offset, Integer count) {
        return User.list([offset: offset, max: Math.min(count, 100)])
    }

    /**
     * Enables/Disables the user identified by @p userId
     * @param userId The unique id of the user
     * @param enable if @c true the user is enabled, if @c false the user is disabled
     * @return @c true, if the enable state was changed, @c false if the user was already in @p enable state
     * @throws UserNotFoundException If the user specified by @p userId does not exist
     */
    @PostLogging(LoggingEventType.UPDATE)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    Boolean enableUser(Long userId, Boolean enable) throws UserNotFoundException {
        User user = User.get(userId)
        if (!user) {
            throw new UserNotFoundException(userId)
        }
        if (user.enabled != enable) {
            user.enabled = enable
            user.save(flush: true)
            return (User.get(userId).enabled == enable)
        } else {
            return false
        }
    }

    /**
     * (Un)Locks the account for user identified by @p userId
     * @param userId The unique id of the user
     * @param lock if @c true the account is locked, if @c false the account is unlocked
     * @return @c true, if the account locked state was changed, @c false if the user was already in @p lock state
     * @throws UserNotFoundException If the user specified by @p userId does not exist
     */
    @PostLogging(LoggingEventType.UPDATE)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    Boolean lockAccount(Long userId, Boolean lock) throws UserNotFoundException {
        User user = User.get(userId)
        if (!user) {
            throw new UserNotFoundException(userId)
        }
        if (user.accountLocked != lock) {
            user.accountLocked = lock
            user.save(flush: true)
            return (User.get(userId).accountLocked == lock)
        } else {
            return false
        }
    }

    /**
     * (Un)Expires the account for user identified by @p userId
     * @param userId The unique id of the user
     * @param expire if @c true the account is expired, if @c false the account is un-expired
     * @return @c true, if the account expired state was changed, @c false if the user was already in @p expire state
     * @throws UserNotFoundException If the user specified by @p userId does not exist
     */
    @PostLogging(LoggingEventType.UPDATE)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    Boolean expireAccount(Long userId, Boolean expire) throws UserNotFoundException {
        User user = User.get(userId)
        if (!user) {
            throw new UserNotFoundException(userId)
        }
        if (user.accountExpired != expire) {
            user.accountExpired = expire
            user.save(flush: true)
            return (User.get(userId).accountExpired == expire)
        } else {
            return false
        }
    }

    /**
     * (Un)Expires the password for user identified by @p userId
     * @param userId The unique id of the user
     * @param expire if @c true the password is expired, if @c false the password is un-expired
     * @return @c true, if the password expired state was changed, @c false if the password was already in @p expire state
     * @throws UserNotFoundException If the user specified by @p userId does not exist
     */
    @PostLogging(LoggingEventType.UPDATE)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    Boolean expirePassword(Long userId, Boolean expire) throws UserNotFoundException {
        User user = User.get(userId)
        if (!user) {
            throw new UserNotFoundException(userId)
        }
        if (user.passwordExpired != expire) {
            user.passwordExpired = expire
            user.save(flush: true)
            return (User.get(userId).passwordExpired == expire)
        } else {
            return false
        }
    }

    /**
     * Registers a new user.
     * If a new user registers himself the account will be initially disabled. A verification mail is
     * sent to either the new user or to a specific administration mail address. After navigating to the verification
     * URL the account will be enabled.
     * If an administrator creates the account the account will be enabled, but the password is expired as
     * an administrator cannot set the password. The user will be able to create a password after verification.
     * In case LDAP is used as an authentication backend, it is not possible to save a password, that is an invalid
     * password ("*") is stored in the database.
     * @param user The new User to register
     * @return Id of new created user
     * @throws RegistrationException In case a user with same name already exists
     * @throws UserInvalidException In case the new user does not validate
     * @see validateRegistration
     */
    @PostLogging(LoggingEventType.CREATION)
    @PreAuthorize("isAnonymous() or hasRole('ROLE_ADMIN')")
    Long register(User user) throws RegistrationException, UserInvalidException {
        if (springSecurityService.authentication instanceof AnonymousAuthenticationToken &&
                !ConfigurationHolder.config.jummp.security.anonymousRegistration) {
            throw new AccessDeniedException("Registration disabled for anonymous users")
        }
        if (User.findByUsername(user.username)) {
            throw new RegistrationException("User with same name already exists", user.username)
        }
        User newUser = user.sanitizedUser()
        if (SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")) {
            // admin user cannot set the password and creates the account enabled, but with expired password
            newUser.password = "*"
            newUser.enabled = true
            newUser.passwordExpired = true
        } else {
            if (ConfigurationHolder.config.jummp.security.ldap.enabled) {
                // disable password for ldap
                newUser.password = "*"
            } else {
                // TODO: validate the password length?
                newUser.password = springSecurityService.encodePassword(user.password, null)
            }
            // user disabled after registration
            newUser.enabled = false
            newUser.passwordExpired = false
        }

        newUser.accountLocked = false
        newUser.accountExpired = false
        newUser.id = null
        if (!newUser.validate()) {
            throw new UserInvalidException(user.username)
        }
        String registrationCode = String.valueOf(random.nextInt()) + user.username
        newUser.registrationCode = registrationCode.encodeAsMD5()
        GregorianCalendar registrationInvalidation = new GregorianCalendar()
        registrationInvalidation.add(GregorianCalendar.DAY_OF_MONTH, 1)
        newUser.registrationInvalidation = registrationInvalidation.getTime()
        newUser.save(flush: true)
        UserRole.create(newUser, Role.findByAuthority("ROLE_USER"), true)
        // send out notification mail
        if (ConfigurationHolder.config.jummp.security.registration.email.send) {
            String recipient = newUser.email
            if (ConfigurationHolder.config.jummp.security.registration.email.sendToAdmin) {
                recipient = ConfigurationHolder.config.jummp.security.registration.email.adminAddress
            }
            String url = ConfigurationHolder.config.jummp.security.registration.verificationURL
            url = url.replace("{{CODE}}", newUser.registrationCode)
            String emailBody = ConfigurationHolder.config.jummp.security.registration.email.body
            emailBody = emailBody.replace("{{NAME}}", newUser.userRealName)
            emailBody = emailBody.replace("{{URL}}", url)
            mailService.sendMail {
                to recipient
                from ConfigurationHolder.config.jummp.security.registration.email.sender
                subject ConfigurationHolder.config.jummp.security.registration.email.subject
                body emailBody
            }
        }
        return User.findByUsername(user.username).id
    }

    /**
     * Validates the registration code of a new user.
     * This method validates the validation Code and enables the user identified by @p username.
     * The registration code is invalidated at the same time.
     * In case the validation is not correct an exception is thrown
     * @param username The name of the new user
     * @param code The validation code
     * @throws UserManagementException Thrown in case that the validation cannot be performed
     * @see register
     */
    @PostLogging(LoggingEventType.UPDATE)
    @PreAuthorize("isAnonymous()")
    void validateRegistration(String username, String code) throws UserManagementException {
        User user = User.findByUsername(username)
        if (!user) {
            throw new UserNotFoundException(username)
        }
        if (user.enabled) {
            throw new RegistrationException("User already enabled", username)
        }
        if (user.registrationCode != code) {
            throw new UserCodeInvalidException(username, user.id, code)
        }
        if (!user.registrationInvalidation || user.registrationInvalidation.before(new Date())) {
            throw new UserCodeExpiredException(username, user.id)
        }
        user.enabled = true
        user.registrationCode = null
        user.registrationInvalidation = null
        user.save(flush: true)
    }

    /**
     * Validates the registration code of a new user registered by an administrator.
     * In opposite to @link validateAdministration the user's account is already enabled but
     * does not yet have a valid password and the password is set to expired.
     * This method allows the registered user to set himself a password (in case LDAP is not used).
     * The expiration of the password is removed.
     * @param username The name of the new user
     * @param code The validation code
     * @param password The new password, may be @c null in that case an invalid password is set
     * @throws UserManagementException Thrown in case that the validation cannot be performed
     */
    @PostLogging(LoggingEventType.UPDATE)
    @PreAuthorize("isAnonymous()")
    void validateAdminRegistration(String username, String code, String password) throws UserManagementException {
        User user = User.findByUsername(username)
        if (!user) {
            throw new UserNotFoundException(username)
        }
        if (user.registrationCode != code) {
            throw new UserCodeInvalidException(username, user.id, code)
        }
        if (!user.registrationInvalidation || user.registrationInvalidation.before(new Date())) {
            throw new UserCodeExpiredException(username, user.id)
        }
        if (!ConfigurationHolder.config.jummp.security.ldap.enabled) {
            if (password) {
                user.password = springSecurityService.encodePassword(password, null)
            } else {
                user.password = "*"
            }
        }
        user.passwordExpired = false
        user.registrationCode = null
        user.registrationInvalidation = null
        user.save(flush: true)
    }

    /**
     * Overloaded method for convenience for not setting a password.
     * @param username The name of the new user
     * @param code The validation code
     * @throws UserManagementException Thrown in case that the validation cannot be performed
     */
    @PostLogging(LoggingEventType.UPDATE)
    @PreAuthorize("isAnonymous()")
    void validateAdminRegistration(String username, String code) throws UserManagementException {
        this.validateAdminRegistration(username, code, null)
    }

    /**
     * Request a new password for user identified by @p username.
     * This method generates a unique token to reset the password and sends it to
     * the user's email address.
     * The password itself is unchanged and not reset.
     * @param username The login id of the user whose password should be reset.
     * @throws UserNotFoundException Thrown if there is no user with @p username
     */
    @PostLogging(LoggingEventType.UPDATE)
    @PreAuthorize("isAnonymous()")
    void requestPassword(String username) throws UserNotFoundException {
        User user = User.findByUsername(username)
        if (!user) {
            throw new UserNotFoundException(username)
        }
        String passwordCode = String.valueOf(random.nextInt()) + user.username
        user.passwordForgottenCode = passwordCode.encodeAsMD5()
        GregorianCalendar codeInvalidation = new GregorianCalendar()
        codeInvalidation.add(GregorianCalendar.DAY_OF_MONTH, 1)
        user.passwordForgottenInvalidation = codeInvalidation.getTime()
        user.save(flush: true)
        // send out notification mail
        if (ConfigurationHolder.config.jummp.security.resetPassword.email.send) {
            String recipient = user.email
            String url = ConfigurationHolder.config.jummp.security.resetPassword.url
            url = url.replace("{{CODE}}", user.passwordForgottenCode)
            String emailBody = ConfigurationHolder.config.jummp.security.resetPassword.email.body
            emailBody = emailBody.replace("{{NAME}}", user.userRealName)
            emailBody = emailBody.replace("{{URL}}", url)
            mailService.sendMail {
                to recipient
                from ConfigurationHolder.config.jummp.security.resetPassword.email.sender
                subject ConfigurationHolder.config.jummp.security.resetPassword.email.subject
                body emailBody
            }
        }
    }

    /**
     * Resets the password of user identified by @p username with @p password in case the @p code is valid.
     * In case the password was expired prior to the reset, the password will no longer be expired
     * @param code The Password Reset Code
     * @param username The Login Id of the User
     * @param password The new Password
     * @throws UserManagementException Thrown in case user is not found or the code is not valid
     */
    @PostLogging(LoggingEventType.UPDATE)
    @PreAuthorize("isAnonymous()")
    void resetPassword(String code, String username, String password) throws UserManagementException {
        User user = User.findByUsername(username)
        if (!user) {
            throw new UserNotFoundException(username)
        }
        if (user.passwordForgottenCode != code) {
            throw new UserCodeInvalidException(username, user.id, code)
        }
        if (!user.passwordForgottenInvalidation || user.passwordForgottenInvalidation.before(new Date())) {
            throw new UserCodeExpiredException(username, user.id)
        }
        // TODO: in case of LDAP we should not change the password
        user.passwordForgottenCode = null
        user.passwordForgottenInvalidation = null
        user.password = springSecurityService.encodePassword(password, null)
        // reset password expired state
        user.passwordExpired = false
        user.save(flush: true)
    }

    /**
     * Retrieves all available roles in this Jummp Instance.
     * As this is an admin method it does not provide a paginated version
     * @return List of all Roles
     */
    @PostLogging(LoggingEventType.RETRIEVAL)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    List<Role> getAllRoles() {
        return Role.listOrderById()
    }

    /**
     * Retrieves the Roles for the User identified by @p id.
     *
     * In case there is no user with @p id an empty list is returned.
     * @param id The user id
     * @return List of Roles assigned to the user
     */
    @PostLogging(LoggingEventType.RETRIEVAL)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    List<Role> getRolesForUser(Long id) {
        return Role.executeQuery("SELECT role FROM UserRole AS userRole JOIN userRole.role AS role JOIN userRole.user AS user WHERE user.id=:id ORDER BY role.id", [id: id])
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
    @PostLogging(LoggingEventType.UPDATE)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    void addRoleToUser(Long userId, Long roleId) throws UserNotFoundException, RoleNotFoundException {
        User user = User.get(userId)
        if (!user) {
            throw new UserNotFoundException(userId)
        }
        Role role = Role.get(roleId)
        if (!role) {
            throw new RoleNotFoundException(roleId)
        }
        if (!UserRole.get(userId, roleId)) {
            UserRole.create(user, role, true)
        }
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
    @PostLogging(LoggingEventType.UPDATE)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    void removeRoleFromUser(Long userId, Long roleId) throws UserNotFoundException, RoleNotFoundException {
        User user = User.get(userId)
        if (!user) {
            throw new UserNotFoundException(userId)
        }
        Role role = Role.get(roleId)
        if (!role) {
            throw new RoleNotFoundException(roleId)
        }
        UserRole.remove(user, role, true)
    }
}
