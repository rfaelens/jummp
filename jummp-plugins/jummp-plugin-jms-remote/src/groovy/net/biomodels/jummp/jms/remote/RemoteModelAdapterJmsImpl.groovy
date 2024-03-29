/**
* Copyright (C) 2010-2014 EMBL-European Bioinformatics Institute (EMBL-EBI),
* Deutsches Krebsforschungszentrum (DKFZ)
*
* This file is part of Jummp.
*
* Jummp is free software; you can redistribute it and/or modify it under the
* terms of the GNU Affero General Public License as published by the Free
* Software Foundation; either version 3 of the License, or (at your option) any
* later version.
*
* Jummp is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
* details.
*
* You should have received a copy of the GNU Affero General Public License along
* with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
*
* Additional permission under GNU Affero GPL version 3 section 7
*
* If you modify Jummp, or any covered work, by linking or combining it with
* Perf4j (or a modified version of that library), containing parts
* covered by the terms of Apache License v2.0, the licensors of this
* Program grant you additional permission to convey the resulting work.
* {Corresponding Source for a non-source form of such a combination shall
* include the source code for the parts of Perf4j used as well as
* that of the covered work.}
**/





package net.biomodels.jummp.jms.remote

import org.perf4j.aop.Profiled
import net.biomodels.jummp.core.model.ModelListSorting
import net.biomodels.jummp.core.model.ModelTransportCommand
import net.biomodels.jummp.core.model.ModelFormatTransportCommand
import net.biomodels.jummp.core.model.RevisionTransportCommand
import net.biomodels.jummp.remote.RemoteModelAdapter
import net.biomodels.jummp.webapp.ast.RemoteJmsAdapter

/**
 * @short Service delegating to ModelService of the core via synchronous JMS
 *
 * This service communicates with ModelJmsAdapterService in core through JMS. The
 * service takes care of wrapping parameters into messages and evaluating returned
 * values.
 *
 * Any other Grails artifact can use this service as any other service. The fact that
 * it uses JMS internally is completely transparent to the users of this service.
 *
 * Important: The methods of this adapter are auto-generated through an AST transformation.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
@RemoteJmsAdapter("RemoteModelAdapter")
class RemoteModelAdapterJmsImpl extends AbstractJmsRemoteAdapter implements RemoteModelAdapter {

    static transactional = false
    private static final String ADAPTER_SERVICE_NAME = "modelJmsAdapter"

    protected String getAdapterServiceName() {
        return ADAPTER_SERVICE_NAME
    }
    @Profiled(tag="RemoteModelAdapterJmsImpl.getAllModels")
    public List<ModelTransportCommand> getAllModels(int offset, int count, boolean sortOrder, ModelListSorting sort) {
        def retVal = send("getAllModels", [offset, count, sortOrder, sort])
        validateReturnValue(retVal, List)
        return (List)retVal
    }
    @Profiled(tag="RemoteModelAdapterJmsImpl.getAllModels")
    public List<ModelTransportCommand> getAllModels(int offset, int count, ModelListSorting sort) {
        def retVal = send("getAllModels", [offset, count, sort])
        validateReturnValue(retVal, List)
        return (List)retVal
    }
    @Profiled(tag="RemoteModelAdapterJmsImpl.getAllModels")
    public List<ModelTransportCommand> getAllModels(ModelListSorting sort) {
        def retVal = send("getAllModels", [sort])
        validateReturnValue(retVal, List)
        return (List)retVal
    }

    @Profiled(tag="RemoteModelAdapterJmsImpl.addRevision")
    RevisionTransportCommand addRevision(long modelId, byte[] file, ModelFormatTransportCommand format, String comment) {
        def returnValue = send("addRevision", [modelId, file, format, comment])
        validateReturnValue(returnValue, RevisionTransportCommand)
        return (RevisionTransportCommand) returnValue
    }
}
