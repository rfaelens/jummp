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
* Grails (or a modified version of that library), containing parts
* covered by the terms of Apache License v2.0, the licensors of this
* Program grant you additional permission to convey the resulting work.
* {Corresponding Source for a non-source form of such a combination shall
* include the source code for the parts of Grails used as well as
* that of the covered work.}
**/





package net.biomodels.jummp.jms

import grails.plugin.jms.Queue
import net.biomodels.jummp.core.miriam.IMiriamService
import net.biomodels.jummp.webapp.ast.JmsAdapter
import net.biomodels.jummp.webapp.ast.JmsQueueMethod

    @SuppressWarnings("UnusedMethodParameter")
/**
 * @short Wrapper class around MiriamService exposed to JMS.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
@JmsAdapter
class MiriamJmsAdapterService extends AbstractJmsAdapter {

    @SuppressWarnings("GrailsStatelessService")
    static exposes = ['jms']
    @SuppressWarnings("GrailsStatelessService")
    static destination = "jummpMiriamJms"
    static transactional = false
    IMiriamService miriamService

    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[String, Boolean])
    def updateMiriamResources(def message) {
        miriamService.updateMiriamResources((String)message[1], (Boolean)message[2])
        return true
    }

    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[String])
    def miriamData(def message) {
        miriamService.miriamData((String)message[1])
    }

    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[])
    def updateAllMiriamIdentifiers(def message) {
        miriamService.updateAllMiriamIdentifiers()
        return true
    }

    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[])
    def updateModels(String authenticationHash) {
        miriamService.updateModels()
        return true
    }
}
