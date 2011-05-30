package net.biomodels.jummp.jms.remote

import net.biomodels.jummp.remote.RemoteUserAdapter
import net.biomodels.jummp.webapp.ast.RemoteJmsAdapter

/**
 * @short Service delegating to UserService of the core via synchronous JMS
 *
 * This service communicates with UserJmsAdapterService in core through JMS. The
 * service takes care of wrapping parameters into messages and evaluating returned
 * values.
 *
 * Any other Grails artifact can use this service as any other service. The fact that
 * it uses JMS internally is completely transparent to the users of this service.
 *
 * Important: The methods of this adapter are auto-generated through an AST transformation.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
@RemoteJmsAdapter("RemoteUserAdapter")
class RemoteUserAdapterJmsImpl extends AbstractJmsRemoteAdapter implements RemoteUserAdapter {

    static transactional = false
    private static final String ADAPTER_SERVICE_NAME = "userJmsAdapter"

    protected String getAdapterServiceName() {
        return ADAPTER_SERVICE_NAME
    }
}
