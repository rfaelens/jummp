package net.biomodels.jummp.jms

import grails.plugin.jms.Queue
import net.biomodels.jummp.webapp.ast.JmsAdapter
import net.biomodels.jummp.webapp.ast.JmsQueueMethod

@SuppressWarnings("UnusedMethodParameter")
@JmsAdapter
class ModelHistoryJmsAdapterService  extends AbstractJmsAdapter {
    @SuppressWarnings("GrailsStatelessService")
    static exposes = ['jms']
    @SuppressWarnings("GrailsStatelessService")
    static destination = "jummpModelHistoryJms"
    static transactional = false
    /**
     * Dependency Injection of Model History Service
     */
    def modelHistoryService

    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[])
    def history(def message) {
        return modelHistoryService.history()
    }

    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[])
    def lastAccessedModel(def message) {
        return modelHistoryService.lastAccessedModel()
    }
}
