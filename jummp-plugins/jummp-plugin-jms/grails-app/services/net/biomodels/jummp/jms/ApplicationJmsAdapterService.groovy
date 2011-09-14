package net.biomodels.jummp.jms

import grails.plugin.jms.Queue
import net.biomodels.jummp.plugins.security.User
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.perf4j.aop.Profiled
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl
import org.springframework.security.authentication.BadCredentialsException
import net.biomodels.jummp.core.IAuthenticationHashService
import net.biomodels.jummp.core.user.JummpAuthenticationImpl

/**
 * @short Wrapper class around the ModelService exposed to JMS.
 *
 * This service is mostly just a wrapper around the ModelService exposed as a JMS interface.
 * Each method takes the Authentication as a parameter and sets it in the SecurityContext of the
 * current thread. Before the method returns the SecurityContext is cleared again.
 *
 * If an exception is thrown in the execution of a method the calling service method will time out.
 * Because of that each method has to catch the possible exception and return them. This implies that each
 * method needs to return a def.
 *
 * Returning @c null results in a time out as @c null stands for no return value. Because of that no method
 * may return @c null. In case a @c null value should be returned it has to be wrapped in a different type,
 * e.g. an Exception.
 *
 * In case a method is invoked with the wrong number of arguments an IllegalArgumentException is returned.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class ApplicationJmsAdapterService extends AbstractJmsAdapter {

    @SuppressWarnings("GrailsStatelessService")
    static exposes = ['jms']
    @SuppressWarnings("GrailsStatelessService")
    static destination = "jummpJms"
    static transactional = false
    /**
     * Dependency injection of authentication Manager
     */
    @SuppressWarnings("GrailsStatelessService")
    def authenticationManager
    /**
     * Dependency injection of grailsApplication
     */
    def grailsApplication

    private IAuthenticationHashService authenticationHashService

    /**
     * Retrieves the externalized Jummp configuration.
     * @param appToken A unique token to verify that the remote application is allowed to retrieve the configuration.
     * @return ConfigObject for jummp config
     * @todo Implement appToken verification
     */
    @grails.plugin.jms.Queue
    @Profiled(tag="jmsAdapterService.getJummpConfig")
    def getJummpConfig(String appToken) {
        return grailsApplication.config.jummp
    }

    /**
     * Performs an Authentication.
     * @param message The Authentication, mostly a UsernamePasswordAuthenticationToken
     * @return A fully propagated Authentication or an AuthenticationException or an IllegalArgumentException if @p message is not an Authentication
     */
    @grails.plugin.jms.Queue
    @Profiled(tag="jmsAdapterService.authenticate")
    def authenticate(def message) {
        if (message instanceof Authentication) {
            try {
                Authentication auth = authenticationManager.authenticate(message)
                if (auth.principal instanceof LdapUserDetailsImpl) {
                    // verify that we have a user with same name in database
                    if (!User.findByUsername(auth.principal.getUsername())) {
                        throw new BadCredentialsException("User does not have an account in the database")
                    }
                }
                String hash = authenticationHashService.hashAuthentication(auth)
                return JummpAuthenticationImpl.fromAuthentication(auth, hash)
            } catch (AuthenticationException e) {
                return e
            }
        }
        return new IllegalArgumentException("Did not receive an authentication")
    }

    /**
     * Retrieves a boolean from the core, indicating if a user's authentication still is valid.
     * @param message The authentication hash of the actual user
     * @return True if user is valid, false otherwise
     */
    @grails.plugin.jms.Queue
    @Profiled(tag="jmsAdapterService.isAuthenticated")
    def isAuthenticated(def message) {
        return authenticationHashService.isAuthenticated(message)
    }

    /**
     * Setter for Dependency Injection of AuthenticationHashService.
     * @param authenticationHashService
     */
    public void setAuthenticationHashService(IAuthenticationHashService authenticationHashService) {
        this.authenticationHashService = authenticationHashService
    }
}
