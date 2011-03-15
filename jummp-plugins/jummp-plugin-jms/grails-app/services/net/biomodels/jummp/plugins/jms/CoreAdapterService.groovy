package net.biomodels.jummp.plugins.jms

import org.springframework.beans.factory.InitializingBean
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import net.biomodels.jummp.core.JummpException
import org.perf4j.aop.Profiled

/**
 * @short Service connecting to the core via synchronous JMS.
 *
 * This service can be used to connect to the core web application through JMS from
 * a different component such as the entry-point web application or web services.
 *
 * All methods are executed with synchronous JMS and it's taken care of the special situations.
 * This means the current Authentication is wrapped into each call and the return value is verified.
 * If an unexpected null value or an Exception is returned, an Exception will be re-thrown to be handled
 * by the application.
 *
 * The service provides access to all methods exported by the cores JmsAdapterService. All returned objects
 * are de-coupled from the database and any changes to the objects are not stored in the database. The appropriate
 * methods of this adapter have to be called to update objects in the database (and by that ensuring that the
 * business logic is used).
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class CoreAdapterService implements InitializingBean {
    def jmsSynchronousService

    static transactional = false
    protected String adapterServiceName = null

    void afterPropertiesSet() {
        adapterServiceName = "jmsAdapter"
    }

    /**
     * Retrieves the externalized configuration of the core application.
     * @param appToken The unique application token
     * @return The core's configuration
     */
    ConfigObject getJummpConfig(String appToken) {
        return (ConfigObject)jmsSynchronousService.send([app: "jummp", service: "jmsAdapter", method: "getJummpConfig"], appToken, [service: "jmsAdapter", method: "getJummpConfig.response"])
    }

    /**
     * Tries to authenticate the given @p Authentication in the core.
     * @param authentication The Authentication to test. In most cases a UsernamePasswordAuthenticationToken
     * @return An authenticated user
     * @throws AuthenticationException If the Authentication is not valid
     * @throws JummpException If an error occurred
     */
    @Profiled(tag="coreAdapterService.authenticate")
    Authentication authenticate(Authentication authentication) throws AuthenticationException, JummpException {
        def retVal = send("authenticate", authentication, false)
        validateReturnValue(retVal, Authentication)
        return (Authentication)retVal
    }

    /**
     * Validates the @p retVal. In case of a @c null value an JummpException is thrown, in case the
     * value is an Exception itself, the Exception gets re-thrown, in case the value is not an instance
     * of @p expectedType an JummpException is thrown.
     * @param retVal The return value to validate
     * @param expectedType The expected type of the value
     * @throws JummpException In case of @p retVal being @c null or not the expected type
     */
    protected void validateReturnValue(def retVal, Class expectedType) throws JummpException {
        if (retVal == null) {
            log.error("Received null value from core.")
            throw new JummpException("Received a null value from core")
        }
        if (retVal instanceof Exception) {
            throw retVal
        }
        if (!expectedType.isInstance(retVal)) {
            throw new JummpException("Expected a value of type ${expectedType.toString()} but received ${retVal.class}")
        }
    }

    /**
     * Convenient overwrite for case of no arguments except Authentication
     * @param method  The name of the method to invoke
     * @return Whatever the core returns
     */
    protected def send(String method) {
        return send(method, null, true)
    }

    /**
     * Convenient overwrite to default to authenticate
     * @param method The name of the method to invoke
     * @param message The arguments which are expected
     * @return Whatever the core returns
     */
    protected def send(String method, def message) {
        return send(method, message, true)
    }

    /**
     * Helper method to send a JMS message to core.
     * @param method The name of the method to invoke
     * @param message The arguments which are expected
     * @param authenticated Whether the Authentication should be prepended to the message
     * @return Whatever the core returns
     */
    protected def send(String method, def message, boolean authenticated) {
        if (authenticated && message) {
            Authentication auth = SecurityContextHolder.context.authentication
            if (message instanceof List) {
                ((List)message).add(0, auth)
            } else {
                message = [auth, message]
            }
        } else if (authenticated && !message) {
            message = SecurityContextHolder.context.authentication
        }
        return jmsSynchronousService.send([app: "jummp", service: adapterServiceName, method: method],message, [service: adapterServiceName, method: "${method}.response"])
    }
}
