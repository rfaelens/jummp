import net.biomodels.jummp.model.ModelFormat

class BootStrap {
    def springSecurityService
    def wcmSecurityService

    def init = { servletContext ->
        ModelFormat format = ModelFormat.findByIdentifier("UNKNWON")
        if (!format) {
            format = new ModelFormat(identifier: "UNKNOWN", name: "Unknown format")
            format.save(flush: true)
        }

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
                if (principal instanceof org.springframework.security.ldap.userdetails.LdapUserDetailsImpl) {
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
