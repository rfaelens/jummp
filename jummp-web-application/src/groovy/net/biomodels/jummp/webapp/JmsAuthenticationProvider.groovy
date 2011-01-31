package net.biomodels.jummp.webapp

import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken

/**
 * @short AuthenticationProvider authenticating against the core application.
 *
 * This AuthenticationProvider is able to authenticate a UsernamePasswordAuthenticationToken by
 * passing it to the core through JMS. If it receives back a fully populated Authentication this
 * can be used by Spring Security. If an exception is thrown the core signals that the credentials
 * are not valid.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de> 
 */
class JmsAuthenticationProvider implements AuthenticationProvider {
    /**
     * Dependency injection of the core adapter service
     */
    def coreAdapterService

    Authentication authenticate(Authentication authentication) {
        return coreAdapterService.authenticate(authentication)
    }

    boolean supports(Class<? extends Object> aClass) {
        return (aClass == UsernamePasswordAuthenticationToken)
    }
}
