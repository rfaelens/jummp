package net.biomodels.jummp.jms

import grails.plugin.jms.Queue
import net.biomodels.jummp.webapp.ast.JmsAdapter
import net.biomodels.jummp.webapp.ast.JmsQueueMethod

/**
 * 
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
	def diffDataService

	@Queue
	@JmsQueueMethod(isAuthenticate=true, arguments=[Long, Integer, Integer])
	def generateDiffData(def message) {
		println("<<< calling DiffJmsAdapterService >>>")
		println(diffDataService.generateDiffData(message[1] as Long, message[2] as Integer, message[3] as Integer))
		return diffDataService.generateDiffData(message[1] as Long, message[2] as Integer, message[3] as Integer)
	}
}
