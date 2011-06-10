package net.biomodels.jummp.jms

import grails.plugin.jms.Queue
import net.biomodels.jummp.webapp.ast.JmsAdapter
import net.biomodels.jummp.webapp.ast.JmsQueueMethod
import net.biomodels.jummp.core.ISbmlService

/**
 * @short JMS Wrapper for SbmlService.
 *
 * Please note that most of the method code is generated using a AST Transformation
 * triggered by @link JmsAdapter.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz.de>
 */
@JmsAdapter
class SbmlJmsAdapterService extends AbstractJmsAdapter {

    @SuppressWarnings("GrailsStatelessService")
    static exposes = ['jms']
    @SuppressWarnings("GrailsStatelessService")
    static destination = "sbmlJmsAdapter"
    static transactional = false
    /**
     * Dependency injection of sbmlService
     */
    ISbmlService sbmlService
    /**
     * Dependency Injection of modelDelegateService
     */
    def modelDelegateService

    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[Long, Integer])
    def getMetaId(def message) {
        return sbmlService.getMetaId(modelDelegateService.getRevision((Long)message[1], (Integer)message[2]))
    }

    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[Long, Integer])
    def getVersion(def message) {
        return sbmlService.getVersion(modelDelegateService.getRevision((Long)message[1], (Integer)message[2]))
    }

    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[Long, Integer])
    def getLevel(def message) {
        return sbmlService.getLevel(modelDelegateService.getRevision((Long)message[1], (Integer)message[2]))
    }

    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[Long, Integer])
    def getNotes(def message) {
        return sbmlService.getNotes(modelDelegateService.getRevision((Long)message[1], (Integer)message[2]))
    }

    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[Long, Integer])
    def getAnnotations(def message) {
        return sbmlService.getAnnotations(modelDelegateService.getRevision((Long)message[1], (Integer)message[2]))
    }

    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[Long, Integer])
    def getParameters(def message) {
        return sbmlService.getParameters(modelDelegateService.getRevision((Long)message[1], (Integer)message[2]))
    }

    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[Long, Integer, String])
    def getParameter(def message) {
        return sbmlService.getParameter(modelDelegateService.getRevision((Long)message[1], (Integer)message[2]), (String)message[3])
    }

    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[Long, Integer])
    def getLocalParameters(def message) {
        return sbmlService.getLocalParameters(modelDelegateService.getRevision((Long)message[1], (Integer)message[2]))
    }

    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[Long, Integer])
    def getReactions(def message) {
        return sbmlService.getReactions(modelDelegateService.getRevision((Long)message[1], (Integer)message[2]))
    }

    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[Long, Integer, String])
    def getReaction(def message) {
        return sbmlService.getReaction(modelDelegateService.getRevision((Long)message[1], (Integer)message[2]), (String)message[3])
    }

    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[Long, Integer])
    def getEvents(def message) {
        return sbmlService.getEvents(modelDelegateService.getRevision((Long)message[1], (Integer)message[2]))
    }

    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[Long, Integer, String])
    def getEvent(def message) {
        return sbmlService.getEvent(modelDelegateService.getRevision((Long)message[1], (Integer)message[2]), (String)message[3])
    }

    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[Long, Integer])
    def getRules(def message) {
        return sbmlService.getRules(modelDelegateService.getRevision((Long)message[1], (Integer)message[2]))
    }

    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[Long, Integer, String])
    def getRule(def message) {
        return sbmlService.getRule(modelDelegateService.getRevision((Long)message[1], (Integer)message[2]), (String)message[3])
    }

    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[Long, Integer])
    def getFunctionDefinitions(def message) {
        return sbmlService.getFunctionDefinitions(modelDelegateService.getRevision((Long)message[1], (Integer)message[2]))
    }

    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[Long, Integer, String])
    def getFunctionDefinition(def message) {
        return sbmlService.getFunctionDefinition(modelDelegateService.getRevision((Long)message[1], (Integer)message[2]), (String)message[3])
    }

    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[Long, Integer])
    def getCompartments(def message) {
        return sbmlService.getCompartments(modelDelegateService.getRevision((Long)message[1], (Integer)message[2]))
    }

    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[Long, Integer, String])
    def getCompartment(def message) {
        return sbmlService.getCompartment(modelDelegateService.getRevision((Long)message[1], (Integer)message[2]), (String)message[3])
    }

    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[Long, Integer])
    def getAllSpecies(def message) {
        return sbmlService.getAllSpecies(modelDelegateService.getRevision((Long)message[1], (Integer)message[2]))
    }

    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[Long, Integer, String])
    def getSpecies(def message) {
        return sbmlService.getSpecies(modelDelegateService.getRevision((Long)message[1], (Integer)message[2]), (String)message[3])
    }
}
