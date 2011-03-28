package net.biomodels.jummp.jms

import grails.plugin.jms.Queue
import net.biomodels.jummp.plugins.security.User
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import net.biomodels.jummp.plugins.security.SerializableGrailsUser
import org.codehaus.groovy.grails.plugins.springsecurity.GrailsUser
import org.perf4j.aop.Profiled
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl
import org.springframework.security.authentication.BadCredentialsException
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import net.biomodels.jummp.jms.AbstractJmsAdapter

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
     * Retrieves the externalized Jummp configuration.
     * @param appToken A unique token to verify that the remote application is allowed to retrieve the configuration.
     * @return ConfigObject for jummp config
     * @todo Implement appToken verification
     */
    @grails.plugin.jms.Queue
    @Profiled(tag="jmsAdapterService.getJummpConfig")
    def getJummpConfig(String appToken) {
        return ConfigurationHolder.config.jummp
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
                    return auth
                }
                // The authentication is propagated with an GrailsUser as principal
                // Unfortunately the GrailsUser class is not serializable.
                // Because of that a new Authentication is created using an own implementation of a serializable GrailsUser
                return new UsernamePasswordAuthenticationToken(SerializableGrailsUser.fromGrailsUser((org.codehaus.groovy.grails.plugins.springsecurity.GrailsUser)auth.principal),
                        auth.getCredentials(), auth.getAuthorities())
            } catch (AuthenticationException e) {
                // extraInformation is also a GrailsUser, so if it is set we need to create a new AuthenticationException
                // with a SerializableGrailsUser instead of the GrailsUser as extraInformation
                if (e.extraInformation) {
                    AuthenticationException exception = e.class.newInstance(e.message, SerializableGrailsUser.fromGrailsUser((org.codehaus.groovy.grails.plugins.springsecurity.GrailsUser)e.extraInformation))
                    exception.setAuthentication(e.authentication)
                    return exception
                } else {
                    return e
                }
            }
        }
        return new IllegalArgumentException("Did not receive an authentication")
    }
}
