package net.biomodels.jummp.core

import net.biomodels.jummp.plugins.security.Role
import net.biomodels.jummp.plugins.security.User
import net.biomodels.jummp.plugins.security.UserRole
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.perf4j.aop.Profiled
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
import org.springframework.transaction.TransactionStatus

/**
 * @short Service for User administration.
 *
 * This service is meant for any kind of user management, such as changing password
 * and administrative tasks like enabling/disabling users, etc.
 * 
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class UserService implements IUserService {

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

    @PostLogging(LoggingEventType.UPDATE)
    @Profiled(tag="userService.changePassword")
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

    @PostLogging(LoggingEventType.UPDATE)
    @Profiled(tag="userService.editUser")
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

    @PostLogging(LoggingEventType.RETRIEVAL)
    @Profiled(tag="userService.getCurrentUser")
    @PreAuthorize("hasRole('ROLE_USER')")
    User getCurrentUser() {
        return User.findByUsername(springSecurityService.authentication.principal.username).sanitizedUser()
    }

    @PostLogging(LoggingEventType.RETRIEVAL)
    @Profiled(tag="userService.getUser")
    @PreAuthorize("hasRole('ROLE_ADMIN') or authentication.name==#username")
    User getUser(String username) throws UserNotFoundException {
        User user = User.findByUsername(username)
        if (!user) {
            throw new UserNotFoundException(username)
        }
        return user.sanitizedUser()
    }

    @PostLogging(LoggingEventType.RETRIEVAL)
    @Profiled(tag="userService.getUser")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    User getUser(Long id) throws UserNotFoundException {
        User user = User.get(id)
        if (!user) {
            throw new UserNotFoundException(id)
        }
        return user
    }

    @PostLogging(LoggingEventType.RETRIEVAL)
    @Profiled(tag="userService.getAllUsers")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    List<User> getAllUsers(Integer offset, Integer count) {
        return User.list([offset: offset, max: Math.min(count, 100)])
    }

    @PostLogging(LoggingEventType.UPDATE)
    @Profiled(tag="userService.enableUser")
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

    @PostLogging(LoggingEventType.UPDATE)
    @Profiled(tag="userService.lockAccount")
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

    @PostLogging(LoggingEventType.UPDATE)
    @Profiled(tag="userService.expireAccount")
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

    @PostLogging(LoggingEventType.UPDATE)
    @Profiled(tag="userService.expirePassword")
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

    @PostLogging(LoggingEventType.CREATION)
    @Profiled(tag="userService.register")
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
        boolean adminRegistration = false
        if (SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")) {
            // admin user cannot set the password and creates the account enabled, but with expired password
            newUser.password = "*"
            newUser.enabled = true
            newUser.passwordExpired = true
            adminRegistration = true
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
            String emailBody = ConfigurationHolder.config.jummp.security.registration.email.body
            String emailSubject = ConfigurationHolder.config.jummp.security.registration.email.subject
            if (adminRegistration) {
                url = ConfigurationHolder.config.jummp.security.activation.activationURL
                emailSubject = ConfigurationHolder.config.jummp.security.activation.email.subject
                emailBody = ConfigurationHolder.config.jummp.security.activation.email.body
                emailBody = emailBody.replace("{{USERNAME}}", newUser.username)
            }
            url = url.replace("{{CODE}}", newUser.registrationCode)
            emailBody = emailBody.replace("{{NAME}}", newUser.userRealName)
            emailBody = emailBody.replace("{{URL}}", url)
            mailService.sendMail {
                to recipient
                from ConfigurationHolder.config.jummp.security.registration.email.sender
                subject emailSubject
                body emailBody
            }
        }
        return User.findByUsername(user.username).id
    }

    @PostLogging(LoggingEventType.UPDATE)
    @Profiled(tag="userService.validateRegistration")
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

    @PostLogging(LoggingEventType.UPDATE)
    @Profiled(tag="userService.validateAdminRegistration")
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

    @PostLogging(LoggingEventType.UPDATE)
    @Profiled(tag="userService.validateAdminRegistration")
    @PreAuthorize("isAnonymous()")
    void validateAdminRegistration(String username, String code) throws UserManagementException {
        this.validateAdminRegistration(username, code, null)
    }

    @PostLogging(LoggingEventType.UPDATE)
    @Profiled(tag="userService.requestPassword")
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

    @PostLogging(LoggingEventType.UPDATE)
    @Profiled(tag="userService.resetPassword")
    @PreAuthorize("isAnonymous()")
    void resetPassword(String code, String username, String password) throws UserNotFoundException, UserCodeInvalidException, UserCodeExpiredException {
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

    @PostLogging(LoggingEventType.RETRIEVAL)
    @Profiled(tag="userService.getAllRoles")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    List<Role> getAllRoles() {
        return Role.listOrderById()
    }

    @PostLogging(LoggingEventType.RETRIEVAL)
    @Profiled(tag="userService.getRolesForUser")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    List<Role> getRolesForUser(Long id) {
        return Role.executeQuery("SELECT role FROM UserRole AS userRole JOIN userRole.role AS role JOIN userRole.user AS user WHERE user.id=:id ORDER BY role.id", [id: id])
    }

    @PostLogging(LoggingEventType.UPDATE)
    @Profiled(tag="userService.addRoleToUser")
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

    @PostLogging(LoggingEventType.UPDATE)
    @Profiled(tag="userService.removeRoleFromUser")
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

    @PostLogging(LoggingEventType.UPDATE)
    @Profiled(tag="userService.removeRoleFromUser")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    Role getRoleByAuthority(String authority) throws RoleNotFoundException {
        Role role = Role.findByAuthority(authority)
        if (!role) {
            throw new RoleNotFoundException(authority)
        }
        return role
    }

    boolean createAdmin(UserCommand user) {
        User person = new User()
        person.properties = user
        boolean userCreated = false
        if (ConfigurationHolder.config.jummp.security.ldap.enabled) {
            // no password for ldap
            person.password = "*"
        } else {
            person.password = springSecurityService.encodePassword(user.password)
        }
        person.enabled = true
        person.accountExpired = false
        person.accountLocked = false
        person.passwordExpired = false
        if (person.validate()) {
            if (persistAdminWithRoles(person)) {
                userCreated = true
            } else {
                log.error("The initial user could not be created in the database. Is the database configured properly?")
                userCreated = false
            }
        } else {
            userCreated = false
        }
        return userCreated
    }

    boolean persistAdminWithRoles(User person) {
        boolean ok = true
        User.withTransaction { TransactionStatus status ->
            if (!person.save()) {
                ok = false
                status.setRollbackOnly()
            }
            if (!createRolesForAdmin(person)) {
                ok = false
                status.setRollbackOnly()
            }
        }
        return ok
    }

    boolean createRolesForAdmin(User user) {
        Role adminRole = new Role(authority: "ROLE_ADMIN")
        if (!adminRole.save(flush: true)) {
            return false
        }
        addRoleToUser(user.id, adminRole.id)
        Role userRole = new Role(authority: "ROLE_USER")
        if (!userRole.save(flush: true)) {
            return false
        }
        addRoleToUser(user.id, userRole.id)
        return true
    }
}