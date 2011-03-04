package net.biomodels.jummp.core

import grails.test.*
import org.springframework.security.core.AuthenticationException
import org.springframework.security.authentication.BadCredentialsException

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
}
