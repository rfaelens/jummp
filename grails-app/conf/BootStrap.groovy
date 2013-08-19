import net.biomodels.jummp.model.ModelFormat
import org.codehaus.groovy.grails.commons.ApplicationAttributes
import net.biomodels.jummp.plugins.security.User
import org.codehaus.groovy.grails.plugins.springsecurity.acl.AclSid
import net.biomodels.jummp.plugins.security.Role
import net.biomodels.jummp.plugins.security.UserRole


class BootStrap {
    def springSecurityService
    def wcmSecurityService

    def init = { servletContext ->
        ModelFormat format = ModelFormat.findByIdentifierAndFormatVersion("UNKNOWN", "")
        if (!format) {
            format = new ModelFormat(identifier: "UNKNOWN", name: "Unknown format", formatVersion: "")
            format.save(flush: true)
        }
        def ctx = servletContext.getAttribute(ApplicationAttributes.APPLICATION_CONTEXT) 
        def service = ctx.getBean("modelFileFormatService")
        def modelFormat = service.registerModelFormat("UNKNOWN", "UNKNOWN")
        service.handleModelFormat(modelFormat, "unknownFormatService", "unknown")
        /* ONLY NEEDED FOR USER ACCOUNT CREATION.
        if (!User.findByUsername("user")) {
           def user = new User(username: "user",
                    password: springSecurityService.encodePassword("secret"),
                    userRealName: "user",
                    email: "user@test.com",
                    enabled: true,
                    accountExpired: false,
                    accountLocked: false,
                    passwordExpired: false)
            user.save(flush: true)
            new AclSid(sid: user.username, principal: true).save(flush: true)
            if (!Role.findByAuthority("ROLE_USER")) {
                new Role(authority: "ROLE_USER").save(flush: true)
            }
            Role userRole = Role.findByAuthority("ROLE_USER")
            UserRole.create(user, userRole, true)
        } 
        */

        // custom mapping for weceem as it fails to work with an LDAPUserDetailsImpl
        wcmSecurityService.securityDelegate = [
            getUserName : { ->
                if (springSecurityService.isLoggedIn()) {
                    return springSecurityService.principal.username
                } else {
                    return null
                }
            },
            getUserEmail : { ->
                return null
            },
            getUserRoles : { ->
                if (springSecurityService.isLoggedIn()) {
                    return springSecurityService.principal.authorities
                } else {
                    return ['ROLE_GUEST']
                }
            },
            getUserPrincipal : { ->
                def principal = springSecurityService.getPrincipal()
                if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                    return new org.springframework.security.core.userdetails.UserDetails() {
                        Collection<org.springframework.security.core.GrantedAuthority> getAuthorities() {
                            return principal.authorities
                        }
                        String getPassword() {
                            return principal.password
                        }
                        String getUsername() {
                            return principal.username
                        }
                        boolean isAccountNonExpired() {
                            return principal.isAccountNonExpired()
                        }
                        boolean isAccountNonLocked() {
                            return principal.isAccountNonLocked()
                        }
                        boolean isCredentialsNonExpired() {
                            return principal.isCredentialsNonExpired()
                        }
                        boolean isEnabled() {
                            return principal.isEnabled()
                        }
                        String getEmail() {
                            return null
                        }
                        String getFirstName() {
                            return null
                        }
                        String getLastName() {
                            return null
                        }
                    }
                } else {
                    return principal
                }
            }
        ]
        
    }
    def destroy = {
    }
}
