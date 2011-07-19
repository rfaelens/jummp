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
class DiffJmsAdapterService extends AbstractJmsAdapter {

	@SuppressWarnings("GrailsStatelessService")
	static exposes = ['jms']
	@SuppressWarnings("GrailsStatelessService")
	static destination = "jummpDiffJms"
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
		println(diffDataService.generateDiffData(message[1] as Long, message[2] as Integer, message[3] as Integer))
		return diffDataService.generateDiffData(message[1] as Long, message[2] as Integer, message[3] as Integer)
	}
}
