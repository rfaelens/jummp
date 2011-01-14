package net.biomodels.jummp.core

import grails.test.GrailsUnitTestCase
import net.biomodels.jummp.plugins.security.User
import org.codehaus.groovy.grails.plugins.springsecurity.acl.AclSid
import net.biomodels.jummp.plugins.security.Role
import net.biomodels.jummp.plugins.security.UserRole
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

/**
 * Base class for Integration tests providing useful methods for creating users and authentication.
 * @author  Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class JummpIntegrationTestCase  extends GrailsUnitTestCase {
    def authenticationManager
    def springSecurityService

    /**
     * Creates three users and their roles:
     * @li testuser with password secret and role ROLE_USER
     * @li user with password verysecret and role ROLE_USER
     * @li admin with password 1234 and ROLE_ADMIN and ROLE_USER
     */
    protected void createUserAndRoles() {
        User user = new User(username: "testuser",
                password: springSecurityService.encodePassword("secret"),
                userRealName: "Test",
                email: "test@test.com",
                enabled: true,
                accountExpired: false,
                accountLocked: false,
                passwordExpired: false)
        assertNotNull(user.save())
        assertNotNull(new AclSid(sid: user.username, principal: true).save(flush: true))
        User user2 = new User(username: "user",
                password: springSecurityService.encodePassword("verysecret"),
                userRealName: "Test2",
                email: "test2@test.com",
                enabled: true,
                accountExpired: false,
                accountLocked: false,
                passwordExpired: false)
        assertNotNull(user2.save())
        assertNotNull(new AclSid(sid: user2.username, principal: true).save(flush: true))
        User admin = new User(username: "admin",
                password: springSecurityService.encodePassword("1234"),
                userRealName: "Administrator",
                email: "admin@test.com",
                enabled: true,
                accountExpired: false,
                accountLocked: false,
                passwordExpired: false)
        assertNotNull(admin.save())
        assertNotNull(new AclSid(sid: admin.username, principal: true).save(flush: true))
        Role userRole = new Role(authority: "ROLE_USER")
        assertNotNull(userRole.save())
        UserRole.create(user, userRole, false)
        UserRole.create(user2, userRole, false)
        UserRole.create(admin, userRole, false)
        Role adminRole = new Role(authority: "ROLE_ADMIN")
        assertNotNull(adminRole.save())
        UserRole.create(admin, adminRole, false)
    }

    /**
     * Sets and authentication based on username and password.
     * @param username The name of the user
     * @param password The password of the user.
     * @return The Authentication object
     */
    protected def authenticate(String username, String password) {
        def authToken = new UsernamePasswordAuthenticationToken(username, password)
        def auth = authenticationManager.authenticate(authToken)
        SecurityContextHolder.getContext().setAuthentication(auth)
        return auth
    }

    /**
     * Modifies the ifAnyGranted method of SpringSecurityUtils to return @p admin value.
     * @param admin if @c true, all access to ifAnyGranted returns @c true, @c false otherwise
     */
    protected void modelAdminUser(boolean admin) {
        SpringSecurityUtils.metaClass.'static'.ifAnyGranted = { String parameter ->
            return admin
        }
    }

    /**
     * Sets the current authentication to testuser and does not model as admin user.
     * @return The testusers authentication
     */
    protected def authenticateAsTestUser() {
        modelAdminUser(false)
        return authenticate("testuser", "secret")
    }

    /**
     * Sets the current authentication to user and does not models as admin user.
     * @return The users authentication
     */
    protected def authenticateAsUser() {
        modelAdminUser(false)
        return authenticate("user", "verysecret")
    }

    /**
     * Sets the current authentication to admin and models as admin user.
     * @return The admin authentication
     */
    protected def authenticateAsAdmin() {
        modelAdminUser(true)
        return authenticate("admin", "1234")
    }
}
