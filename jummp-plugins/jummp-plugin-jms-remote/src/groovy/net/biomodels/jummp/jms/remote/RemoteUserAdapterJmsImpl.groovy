/**
* Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI), Deutsches Krebsforschungszentrum (DKFZ)
*
* This file is part of Jummp.
*
* Jummp is free software; you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
*
* Jummp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License along with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
**/


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
