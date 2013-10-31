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
        return (Boolean)jmsSynchronousService.send([app: "jummp", service: "applicationJmsAdapter", method: "isAuthenticated"], ((JummpAuthentication)authentication).getAuthenticationHash(), [service: "applicationJmsAdapter", method: "isAuthenticated.response"])
    }
}
