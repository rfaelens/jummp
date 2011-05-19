package net.biomodels.jummp.core

import grails.test.*
import org.springframework.security.authentication.BadCredentialsException
import net.biomodels.jummp.plugins.security.Role
import net.biomodels.jummp.plugins.security.User
import net.biomodels.jummp.plugins.security.UserRole
import org.springframework.security.access.AccessDeniedException
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import net.biomodels.jummp.core.user.RegistrationException
import net.biomodels.jummp.core.user.RoleNotFoundException
import net.biomodels.jummp.core.user.UserCodeExpiredException
import net.biomodels.jummp.core.user.UserCodeInvalidException
import net.biomodels.jummp.core.user.UserNotFoundException

class UserServiceTests extends JummpIntegrationTestCase {
    def userService
    protected void setUp() {
        super.setUp()
        createUserAndRoles()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testChangePassword() {
        authenticateAsTestUser()
        shouldFail(BadCredentialsException) {
            userService.changePassword("wrong", "secure")
        }
        userService.changePassword("secret", "secure")
        shouldFail(BadCredentialsException) {
            authenticateAsTestUser()
        }
        authenticate("testuser", "secure")
    }

    void testEditUser() {
        authenticateAsTestUser()
        User user = User.findByUsername("user")
        user.userRealName = "Changed Name"
        shouldFail(AccessDeniedException) {
            userService.editUser(user)
        }
        authenticateAsUser()
        userService.editUser(user)
        authenticateAsAdmin()
        userService.editUser(user)
        userService.editUser(user)
    }

    void testGetCurrentUser() {
        authenticateAnonymous()
        shouldFail(AccessDeniedException) {
            userService.getCurrentUser()
        }
        authenticateAsUser()
        User user = userService.getCurrentUser()
        assertEquals("user", user.username)
    }

    void testGetUser() {
        authenticateAnonymous()
        shouldFail(AccessDeniedException) {
            userService.getUser("user")
        }
        authenticateAsTestUser()
        shouldFail(AccessDeniedException) {
            userService.getUser("user")
        }
        authenticateAsUser()
        User user = userService.getUser("user")
        assertEquals("user", user.username)
        assertNull(user.password)
        assertNull(user.enabled)
        assertNull(user.accountExpired)
        assertNull(user.accountLocked)
        assertNull(user.passwordExpired)
        authenticateAsAdmin()
        userService.getUser("user")
        shouldFail(UserNotFoundException) {
            userService.getUser("noSuchUser")
        }
    }

    void testGetAllUsers() {
        authenticateAnonymous()
        shouldFail(AccessDeniedException) {
            userService.getAllUsers(0, 0)
        }
        authenticateAsUser()
        shouldFail(AccessDeniedException) {
            userService.getAllUsers(0, 0)
        }
        authenticateAsAdmin()
        List<User> users = userService.getAllUsers(0, 10)
        assertEquals(3, users.size())
        // TODO: add tests for the size - requires creation of more users
    }

    void testEnableUser() {
        authenticateAnonymous()
        shouldFail(AccessDeniedException) {
            userService.enableUser(1, true)
        }
        authenticateAsUser()
        shouldFail(AccessDeniedException) {
            userService.enableUser(1, true)
        }
        authenticateAsAdmin()
        shouldFail(UserNotFoundException) {
            userService.enableUser(0, true)
        }
        assertFalse(userService.enableUser(User.findByUsername("testuser").id, true))
        assertTrue(userService.enableUser(User.findByUsername("testuser").id, false))
    }

    void testLockAccount() {
        authenticateAnonymous()
        shouldFail(AccessDeniedException) {
            userService.lockAccount(1, true)
        }
        authenticateAsUser()
        shouldFail(AccessDeniedException) {
            userService.lockAccount(1, true)
        }
        authenticateAsAdmin()
        shouldFail(UserNotFoundException) {
            userService.lockAccount(0, true)
        }
        assertFalse(userService.lockAccount(User.findByUsername("testuser").id, false))
        assertTrue(userService.lockAccount(User.findByUsername("testuser").id, true))
    }

    void testExpireAccount() {
        authenticateAnonymous()
        shouldFail(AccessDeniedException) {
            userService.expireAccount(1, true)
        }
        authenticateAsUser()
        shouldFail(AccessDeniedException) {
            userService.expireAccount(1, true)
        }
        authenticateAsAdmin()
        shouldFail(UserNotFoundException) {
            userService.expireAccount(0, true)
        }
        assertFalse(userService.expireAccount(User.findByUsername("testuser").id, false))
        assertTrue(userService.expireAccount(User.findByUsername("testuser").id, true))
    }

    void testExpirePassword() {
        authenticateAnonymous()
        shouldFail(AccessDeniedException) {
            userService.expirePassword(1, true)
        }
        authenticateAsUser()
        shouldFail(AccessDeniedException) {
            userService.expirePassword(1, true)
        }
        authenticateAsAdmin()
        shouldFail(UserNotFoundException) {
            userService.expirePassword(0, true)
        }
        assertFalse(userService.expirePassword(User.findByUsername("testuser").id, false))
        assertTrue(userService.expirePassword(User.findByUsername("testuser").id, true))
    }

    void testRegister() {
        User user = new User(username: "register", password: "test", userRealName: "Test Name", email: "test@example.com")
        authenticateAsUser()
        shouldFail(AccessDeniedException) {
            userService.register(user)
        }
        boolean anonymousRegistration = ConfigurationHolder.config.jummp.security.anonymousRegistration
        ConfigurationHolder.config.jummp.security.anonymousRegistration = false
        authenticateAnonymous()
        shouldFail(AccessDeniedException) {
            userService.register(user)
        }
        ConfigurationHolder.config.jummp.security.anonymousRegistration = true
        Long id = userService.register(user)
        shouldFail(RegistrationException) {
            userService.register(user)
        }
        User registeredUser = User.findByUsername("register")
        assertEquals(registeredUser.id, id)
        assertFalse(registeredUser.enabled)
        assertFalse(registeredUser.accountLocked)
        assertFalse(registeredUser.accountExpired)
        assertFalse(registeredUser.passwordExpired)
        assertNotNull(registeredUser.registrationCode)
        assertNotNull(registeredUser.registrationInvalidation)
        GregorianCalendar calendar = new GregorianCalendar()
        calendar.add(GregorianCalendar.DAY_OF_MONTH, 1)
        GregorianCalendar validateCal = new GregorianCalendar()
        validateCal.setTime(registeredUser.registrationInvalidation)
        assertEquals(calendar.get(GregorianCalendar.DAY_OF_MONTH), validateCal.get(GregorianCalendar.DAY_OF_MONTH))
        // try another user as amdin
        authenticateAsAdmin()
        ConfigurationHolder.config.jummp.security.anonymousRegistration = false
        user.username = "register2"
        id = userService.register(user)
        User adminRegisteredUser = User.findByUsername("register2")
        assertEquals(adminRegisteredUser.id, id)
        assertTrue(adminRegisteredUser.enabled)
        assertFalse(adminRegisteredUser.accountLocked)
        assertFalse(adminRegisteredUser.accountExpired)
        assertTrue(adminRegisteredUser.passwordExpired)
        assertNotNull(adminRegisteredUser.registrationCode)
        assertNotNull(adminRegisteredUser.registrationInvalidation)
        assertEquals(adminRegisteredUser.password, "*")
        assertFalse(registeredUser.registrationCode == adminRegisteredUser.registrationCode)
        ConfigurationHolder.config.jummp.security.anonymousRegistration = anonymousRegistration
    }

    void testValidateRegistration() {
        authenticateAsUser()
        shouldFail(AccessDeniedException) {
            userService.validateRegistration("user", "1234")
        }
        authenticateAnonymous()
        shouldFail(UserNotFoundException) {
            userService.validateRegistration("notExistingUser", "1234")
        }
        // first register a user
        User user = new User(username: "register", password: "test", userRealName: "Test Name", email: "test@example.com")
        userService.register(user)
        User registeredUser = User.findByUsername("register")
        // exception with wrong registration code
        shouldFail(UserCodeInvalidException) {
            userService.validateRegistration("register", "1234")
        }
        // change the registrationInvalidation to be in the past
        GregorianCalendar validateCal = new GregorianCalendar()
        validateCal.setTime(registeredUser.registrationInvalidation)
        validateCal.add(GregorianCalendar.DAY_OF_MONTH, -1)
        registeredUser.registrationInvalidation = validateCal.getTime()
        registeredUser.save(flush: true)
        shouldFail(UserCodeExpiredException) {
            userService.validateRegistration("register", registeredUser.registrationCode)
        }
        validateCal.add(GregorianCalendar.DAY_OF_MONTH, 1)
        registeredUser.registrationInvalidation = validateCal.getTime()
        registeredUser.save(flush: true)
        // with correct validation code it should work
        userService.validateRegistration("register", registeredUser.registrationCode)
        // refresh
        registeredUser = User.get(registeredUser.id)
        assertTrue(registeredUser.enabled)
        assertNull(registeredUser.registrationCode)
        assertNull(registeredUser.registrationInvalidation)
        // trying to validate again should fail
        shouldFail(RegistrationException) {
            userService.validateRegistration("register", registeredUser.registrationCode)
        }
    }

    void testGetAllRoles() {
        authenticateAnonymous()
        shouldFail(AccessDeniedException) {
            userService.getAllRoles()
        }
        authenticateAsUser()
        shouldFail(AccessDeniedException) {
            userService.getAllRoles()
        }
        authenticateAsAdmin()
        List<Role> roles = userService.getAllRoles()
        assertArrayEquals([Role.findByAuthority("ROLE_USER"), Role.findByAuthority("ROLE_ADMIN")].toArray(), roles.toArray())
    }

    void testGetRolesForUser() {
        User user = User.findByUsername("testuser")
        authenticateAnonymous()
        shouldFail(AccessDeniedException) {
            userService.getRolesForUser(user.id)
        }
        authenticateAsUser()
        shouldFail(AccessDeniedException) {
            userService.getRolesForUser(user.id)
        }
        authenticateAsAdmin()
        assertTrue(userService.getRolesForUser(0).isEmpty())
        assertArrayEquals([Role.findByAuthority("ROLE_USER")].toArray(), userService.getRolesForUser(user.id).toArray())
        User admin = User.findByUsername("admin")
        assertArrayEquals([Role.findByAuthority("ROLE_USER"), Role.findByAuthority("ROLE_ADMIN")].toArray(), userService.getRolesForUser(admin.id).toArray())
    }

    void testAddRoleToUser() {
        authenticateAnonymous()
        shouldFail(AccessDeniedException) {
            userService.addRoleToUser(0, 0)
        }
        authenticateAsUser()
        shouldFail(AccessDeniedException) {
            userService.addRoleToUser(0, 0)
        }
        authenticateAsAdmin()
        shouldFail(UserNotFoundException) {
            userService.addRoleToUser(0, 0)
        }
        User user = User.findByUsername("testuser")
        shouldFail(RoleNotFoundException) {
            userService.addRoleToUser(user.id, 0)
        }
        Role role = Role.findByAuthority("ROLE_USER")
        Role adminRole = Role.findByAuthority("ROLE_ADMIN")
        // adding a role to a user who already has the role should not change anything
        assertNotNull(UserRole.get(user.id, role.id))
        userService.addRoleToUser(user.id, role.id)
        assertNotNull(UserRole.get(user.id, role.id))
        // adding a role not yet added to the user should change it
        assertNull(UserRole.get(user.id, adminRole.id))
        userService.addRoleToUser(user.id, adminRole.id)
        assertNotNull(UserRole.get(user.id, adminRole.id))
    }

    void testRemoveRoleFromUser() {
        authenticateAnonymous()
        shouldFail(AccessDeniedException) {
            userService.removeRoleFromUser(0, 0)
        }
        authenticateAsUser()
        shouldFail(AccessDeniedException) {
            userService.removeRoleFromUser(0, 0)
        }
        authenticateAsAdmin()
        shouldFail(UserNotFoundException) {
            userService.removeRoleFromUser(0, 0)
        }
        User user = User.findByUsername("testuser")
        shouldFail(RoleNotFoundException) {
            userService.removeRoleFromUser(user.id, 0)
        }
        Role role = Role.findByAuthority("ROLE_USER")
        Role adminRole = Role.findByAuthority("ROLE_ADMIN")
        // removing a role from a user who does not have the role should not change anything
        assertNull(UserRole.get(user.id, adminRole.id))
        userService.removeRoleFromUser(user.id, adminRole.id)
        assertNull(UserRole.get(user.id, adminRole.id))
        // removing a role the user has should change it
        assertNotNull(UserRole.get(user.id, role.id))
        userService.removeRoleFromUser(user.id, role.id)
        assertNull(UserRole.get(user.id, role.id))
        // just once - add it and remove it
        userService.addRoleToUser(user.id, role.id)
        assertNotNull(UserRole.get(user.id, role.id))
        userService.removeRoleFromUser(user.id, role.id)
        assertNull(UserRole.get(user.id, role.id))
    }
}