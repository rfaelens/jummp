import net.biomodels.jummp.model.ModelFormat
import org.codehaus.groovy.grails.commons.ApplicationAttributes

class BootStrap {
    def springSecurityService
    def wcmSecurityService

    def init = { servletContext ->
        ModelFormat format = ModelFormat.findByIdentifier("UNKNWON")
        if (!format) {
            format = new ModelFormat(identifier: "UNKNOWN", name: "Unknown format")
            format.save(flush: true)
        }
        def ctx = servletContext.getAttribute(ApplicationAttributes.APPLICATION_CONTEXT) 
        def service = ctx.getBean("modelFileFormatService")
        def modelFormat = service.registerModelFormat("UNKNOWN", "UNKNOWN")
        service.handleModelFormat(modelFormat, "unknownFormatService")
        
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
        println "Thank you for calling BootStrap:destroy(). Wait 5 seconds."
        Thread.sleep(5000)
    }
}
