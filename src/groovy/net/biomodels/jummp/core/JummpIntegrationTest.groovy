/**
* Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI), Deutsches Krebsforschungszentrum (DKFZ)
*
* This file is part of Jummp.
*
* Jummp is free software; you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as
* published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
*
* Jummp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License along with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
*
* Additional permission under GNU Affero GPL version 3 section 7
*
* If you modify Jummp, or any covered work, by linking or combining it with [name of library] (or a modified version of that
* library), containing parts covered by the terms of [name of library's license], the licensors of this Program grant you additional
* permission to convey the resulting work. {Corresponding Source for a non-source form of such a combination shall include the source
* code for the parts of [name of library] used as well as that of the covered work.}
**/


package net.biomodels.jummp.core

import static org.junit.Assert.*
import net.biomodels.jummp.plugins.security.User
import org.codehaus.groovy.grails.plugins.springsecurity.acl.AclSid
import net.biomodels.jummp.plugins.security.Role
import net.biomodels.jummp.plugins.security.UserRole
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.authority.GrantedAuthorityImpl

/**
 * Base class for Integration tests providing useful methods for creating users and authentication.
 * @author  Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class JummpIntegrationTest {
    def authenticationManager
    def springSecurityService

    def shouldFail = { exception, code ->
        try {
            code.call()
            fail("Exception of type ${exception} was expected")
        } catch (Exception e) {
            if (!exception.isAssignableFrom(e.class)) {
                fail("Exception of type ${exception} expected but got ${e.class}")
            }
        }
    }

    /**
     * Creates three users and their roles:
     * @li testuser with password secret and role ROLE_USER
     * @li user with password verysecret and role ROLE_USER
     * @li admin with password 1234 and ROLE_ADMIN and ROLE_USER
     */
    protected void createUserAndRoles() {
        User user, user2, admin, curator
        if (!User.findByUsername("testuser")) {
            user = new User(username: "testuser",
                    password: springSecurityService.encodePassword("secret"),
                    userRealName: "Test",
                    email: "test@test.com",
                    enabled: true,
                    accountExpired: false,
                    accountLocked: false,
                    passwordExpired: false)
            assertNotNull(user.save())
            assertNotNull(new AclSid(sid: user.username, principal: true).save(flush: true))
        } else {
            user = User.findByUsername("testuser")
        }
        if (!User.findByUsername("username")) {
            user2 = new User(username: "username",
                    password: springSecurityService.encodePassword("verysecret"),
                    userRealName: "Test2",
                    email: "test2@test.com",
                    enabled: true,
                    accountExpired: false,
                    accountLocked: false,
                    passwordExpired: false)
            assertNotNull(user2.save())
            assertNotNull(new AclSid(sid: user2.username, principal: true).save(flush: true))
        } else {
            user2 = User.findByUsername("username")
        }
        if (!User.findByUsername("admin")) {
            admin = new User(username: "admin",
                    password: springSecurityService.encodePassword("1234"),
                    userRealName: "Administrator",
                    email: "admin@test.com",
                    enabled: true,
                    accountExpired: false,
                    accountLocked: false,
                    passwordExpired: false)
            assertNotNull(admin.save())
            assertNotNull(new AclSid(sid: admin.username, principal: true).save(flush: true))
        } else {
            admin = User.findByUsername("admin")
        }
        if (!User.findByUsername("curator")) {
            curator = new User(username: "curator",
                    password: springSecurityService.encodePassword("extremelysecret"),
                    userRealName: "Curator",
                    email: "curator@test.com",
                    enabled: true,
                    accountExpired: false,
                    accountLocked: false,
                    passwordExpired: false)
            assertNotNull(curator.save())
            assertNotNull(new AclSid(sid: curator.username, principal: true).save(flush: true))
        } else {
            curator = User.findByUsername("curator")
        }
        ensureRoleExists("ROLE_USER")
        Role userRole = Role.findByAuthority("ROLE_USER")
        UserRole.create(user, userRole, false)
        UserRole.create(user2, userRole, false)
        UserRole.create(admin, userRole, false)
        UserRole.create(curator, userRole, false)
        ensureRoleExists("ROLE_ADMIN")
        Role adminRole = Role.findByAuthority("ROLE_ADMIN")
        UserRole.create(admin, adminRole, false)
        ensureRoleExists("ROLE_CURATOR")
        Role curatorRole = Role.findByAuthority("ROLE_CURATOR")
        UserRole.create(curator, curatorRole, false)
    }

    private def ensureRoleExists(String _authority) {
        if (!Role.findByAuthority(_authority)) {
            new Role(authority: _authority).save()
        }
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
        return authenticate("username", "verysecret")
    }

    /**
     * Sets the current authentication to admin and models as admin user.
     * @return The admin authentication
     */
    protected def authenticateAsAdmin() {
        modelAdminUser(true)
        return authenticate("admin", "1234")
    }

     /**
     * Sets an anonymous authentication and does not model as admin user.
     * @return The anonymous authentication
     */
    protected def authenticateAnonymous() {
        modelAdminUser(false)
        def auth = new AnonymousAuthenticationToken("test", "Anonymous", [ new GrantedAuthorityImpl("ROLE_ANONYMOUS")])
        SecurityContextHolder.getContext().setAuthentication(auth)
        return auth
    }

    /**
     * Sets the current authentication to curator and does not models as admin user.
     * @return The curator authentication
     */
    protected def authenticateAsCurator() {
        modelAdminUser(false)
        return authenticate("curator", "extremelysecret")
    }
}
