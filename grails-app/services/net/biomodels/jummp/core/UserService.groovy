package net.biomodels.jummp.core

import net.biomodels.jummp.plugins.security.Role
import net.biomodels.jummp.plugins.security.User
import net.biomodels.jummp.plugins.security.UserRole
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.authentication.BadCredentialsException

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

    /**
     * (Un)Locks the account for user identified by @p userId
     * @param userId The unique id of the user
     * @param lock if @c true the account is locked, if @c false the account is unlocked
     * @return @c true, if the account locked state was changed, @c false if the user was already in @p lock state
     * @throws IllegalArgumentException If the user specified by @p userId does not exist
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    Boolean lockAccount(Long userId, Boolean lock) throws IllegalArgumentException {
        User user = User.get(userId)
        if (!user) {
            throw new IllegalArgumentException("No user for given id")
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
     * @throws IllegalArgumentException If the user specified by @p userId does not exist
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    Boolean expireAccount(Long userId, Boolean expire) throws IllegalArgumentException {
        User user = User.get(userId)
        if (!user) {
            throw new IllegalArgumentException("No user for given id")
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
     * @throws IllegalArgumentException If the user specified by @p userId does not exist
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    Boolean expirePassword(Long userId, Boolean expire) throws IllegalArgumentException {
        User user = User.get(userId)
        if (!user) {
            throw new IllegalArgumentException("No user for given id")
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
     * @throws JummpException In case a user with same name already exists
     * @see validateRegistration
     * @todo more specific Exception
     * @todo send out notification mail
     * @todo include some settings to decide whether anonymous registration is allowed
     */
    @PreAuthorize("isAnonymous() or hasRole('ROLE_ADMIN')")
    void register(User user) throws JummpException {
        if (User.findByUsername(user.username)) {
            throw new JummpException("User with same name already exists")
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
            throw new JummpException("User does not validate")
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
    }

    /**
     * Validates the registration code of a new user.
     * This method validates the validation Code and enables the user identified by @p username.
     * The registration code is invalidated at the same time.
     * In case the validation is not correct an exception is thrown
     * @param username The name of the new user
     * @param code The validation code
     * @throws JummpException Thrown in case that the validation cannot be performed
     * @see register
     * @todo more specific Exception
     */
    @PreAuthorize("isAnonymous()")
    void validateRegistration(String username, String code) throws JummpException {
        User user = User.findByUsername(username)
        if (!user) {
            throw new JummpException("User not found")
        }
        if (user.enabled) {
            throw new JummpException("User already enabled")
        }
        if (user.registrationCode != code) {
            throw new JummpException("Registration code not valid")
        }
        if (!user.registrationInvalidation || user.registrationInvalidation.before(new Date())) {
            throw new JummpException("Registration code is not valid any more")
        }
        user.enabled = true
        user.registrationCode = null
        user.registrationInvalidation = null
        user.save(flush: true)
    }
}
