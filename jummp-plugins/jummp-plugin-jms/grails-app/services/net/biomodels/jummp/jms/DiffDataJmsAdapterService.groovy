/**
* Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI),
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
import net.biomodels.jummp.plugins.bives.DiffDataService;
import net.biomodels.jummp.webapp.ast.JmsAdapter
import net.biomodels.jummp.webapp.ast.JmsQueueMethod

/**
 * @short Wrapper class around the {@link DiffDataService} exposed to JMS.
 * @author Robert Haelke, robert.haelke@googlemail.com
 * @date 04.07.2011
 * @year 2011
 */
@JmsAdapter
class DiffDataJmsAdapterService extends AbstractJmsAdapter {

	@SuppressWarnings("GrailsStatelessService")
	static exposes = ['jms']
	@SuppressWarnings("GrailsStatelessService")
	static destination = "jummpDiffDataJms"
	static transactional = false
	
	/**
	 * Dependency injection of the {@link DiffDataService}
	 */
	def diffDataService

	/**
	* Wrapper for DiffDataService.generateDiff
	* @param message List with model id, a predecessor and a successor revision number
	* @return List of diff operations
	*/
	@Queue
	@JmsQueueMethod(isAuthenticate=true, arguments=[Long, Integer, Integer])
	def generateDiffData(def message) {
		return diffDataService.generateDiffData(message[1] as Long, message[2] as Integer, message[3] as Integer)
	}
}
