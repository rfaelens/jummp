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
}
