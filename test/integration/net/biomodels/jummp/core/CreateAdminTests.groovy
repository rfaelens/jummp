package net.biomodels.jummp.core

import net.biomodels.jummp.plugins.security.User
import net.biomodels.jummp.plugins.security.UserRole
import net.biomodels.jummp.plugins.security.Role

class CreateAdminTests extends JummpIntegrationTestCase {
    def userService
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testCreateAdmin() {
        User user = new User(username: "admin", password: "1234", userRealName: "Administrator", email: "admin@test.com")
        authenticateAnonymous()
        user.enabled = false
        assertFalse(user.validate())
        user.enabled = true
        // Other required values are not set
        assertFalse(user.validate())
        user.accountExpired = false
        user.accountLocked = false
        user.passwordExpired = false
        assertTrue(user.validate())
        assertTrue(userService.persistAdminWithRoles(user))
        assertFalse(userService.createRolesForAdmin(user))
        //To be sure, the user cannot be created twice
        assertFalse(userService.persistAdminWithRoles(user))
    }

    void testCreateRolesForAdmin() {
        User user = new User(username: "admin", password: "1234", userRealName: "Administrator", email: "admin@test.com")
        authenticateAnonymous()
        user.enabled = true
        user.accountExpired = false
        user.accountLocked = false
        user.passwordExpired = false
        assertNotNull(user.save())
        boolean ok = userService.createRolesForAdmin(user)
        assertTrue(ok)
        Role adminRole = Role.findByAuthority("ROLE_ADMIN")
        UserRole adminUserRole = UserRole.findByUserAndRole(user, adminRole)
        assertNotNull(adminUserRole)
        Role userRole = Role.findByAuthority("ROLE_USER")
        UserRole userUserRole = UserRole.findByUserAndRole(user, userRole)
        assertNotNull(userUserRole)
    }
}
