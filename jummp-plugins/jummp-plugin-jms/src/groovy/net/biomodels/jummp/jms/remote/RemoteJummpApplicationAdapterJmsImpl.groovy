package net.biomodels.jummp.jms.remote

import net.biomodels.jummp.core.JummpException
import org.perf4j.aop.Profiled
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import net.biomodels.jummp.remote.RemoteJummpApplicationAdapter

/**
 * @short Service delegating to Application specific methods of the core via synchronous JMS
 *
 * This service communicates with ApplicationJmsAdapterService in core through JMS. The
 * service takes care of wrapping parameters into messages and evaluating returned
 * values.
 *
 * Any other Grails artifact can use this service as any other service. The fact that
 * it uses JMS internally is completely transparent to the users of this service.
 *
 * This service provides methods which are specific to the specific application instance, such
 * as retrieving the configuration or authenticating a user.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class RemoteJummpApplicationAdapterJmsImpl extends AbstractJmsRemoteAdapter implements RemoteJummpApplicationAdapter {

    private static final String ADAPTER_SERVICE_NAME = "jmsAdapter"

    protected String getAdapterServiceName() {
        return ADAPTER_SERVICE_NAME
    }

    /**
     * Retrieves the externalized configuration of the core application.
     * @param appToken The unique application token
     * @return The core's configuration
     */
    @Profiled(tag="RemoteJummpApplicationAdapterJmsImpl.getJummpConfig")
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
    @Profiled(tag="RemoteJummpApplicationAdapterJmsImpl.authenticate")
    Authentication authenticate(Authentication authentication) throws AuthenticationException, JummpException {
        def retVal = send("authenticate", authentication, false)
        validateReturnValue(retVal, Authentication)
        return (Authentication)retVal
    }

}
