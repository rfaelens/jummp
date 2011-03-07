package net.biomodels.jummp.core

import grails.test.*
import org.springframework.security.core.AuthenticationException
import org.springframework.security.authentication.BadCredentialsException
import net.biomodels.jummp.plugins.security.User
import org.springframework.security.access.AccessDeniedException

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
        shouldFail(IllegalArgumentException) {
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
        shouldFail(IllegalArgumentException) {
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
        shouldFail(IllegalArgumentException) {
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
        shouldFail(IllegalArgumentException) {
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
        shouldFail(IllegalArgumentException) {
            userService.expirePassword(0, true)
        }
        assertFalse(userService.expirePassword(User.findByUsername("testuser").id, false))
        assertTrue(userService.expirePassword(User.findByUsername("testuser").id, true))
    }
}
