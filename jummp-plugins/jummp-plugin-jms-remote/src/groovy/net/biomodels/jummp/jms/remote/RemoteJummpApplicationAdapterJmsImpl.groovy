package net.biomodels.jummp.jms.remote

import net.biomodels.jummp.core.JummpException
import org.perf4j.aop.Profiled
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import net.biomodels.jummp.remote.RemoteJummpApplicationAdapter
import net.biomodels.jummp.core.user.JummpAuthentication

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

    private static final String ADAPTER_SERVICE_NAME = "applicationJmsAdapter"

    protected String getAdapterServiceName() {
        return ADAPTER_SERVICE_NAME
    }

    @Profiled(tag="RemoteJummpApplicationAdapterJmsImpl.getJummpConfig")
    ConfigObject getJummpConfig(String appToken) {
        return (ConfigObject)jmsSynchronousService.send([app: "jummp", service: "applicationJmsAdapter", method: "getJummpConfig"], appToken, [service: "applicationJmsAdapter", method: "getJummpConfig.response"])
    }

    @Profiled(tag="RemoteJummpApplicationAdapterJmsImpl.authenticate")
    Authentication authenticate(Authentication authentication) throws AuthenticationException, JummpException {
        def retVal = send("authenticate", authentication, false)
        validateReturnValue(retVal, Authentication)
        return (Authentication)retVal
    }

    @Profiled(tag="RemoteJummpApplicationAdapterJmsImpl.isAuthenticated")
    boolean isAuthenticated(Authentication authentication) {
        def retVal = send("isAuthenticated", ((JummpAuthentication)authentication).getAuthenticationHash())
        validateReturnValue(retVal, Boolean)
        return (Boolean)retVal
    }
}
