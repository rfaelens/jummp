package grails.plugin.jms

import javax.jms.Destination
import javax.jms.Topic

import grails.plugin.jms.listener.GrailsMessagePostProcessor
import org.apache.commons.logging.LogFactory
import org.springframework.jms.core.JmsTemplate

/**
 * @short Synchronous version of JmsService.
 *
 * An addition to the JmsService to easily allow synchronous sending of messages.
 * This class is just an internal helper to access JMS. Do not use directly.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class JmsSynchronousService {

    static transactional = false
    static final LOG = LogFactory.getLog(JmsSynchronousService)
    static final DEFAULT_JMS_TEMPLATE_BEAN_NAME = "standard"

    def jmsService
    @SuppressWarnings("GrailsStatelessService")
    def grailsApplication

    def send(destination, message, callback) {
        if (grailsApplication.config.jms.disabled) {
            log.warn "not sending message [$message] to [$destination] because JMS is disabled in config"
            return
        }

        String jmsTemplateBeanName = DEFAULT_JMS_TEMPLATE_BEAN_NAME + "JmsTemplate"
        def jmsTemplate = grailsApplication.mainContext.getBean(jmsTemplateBeanName)
        if (jmsTemplate == null) {
            throw new Error("Could not find bean with name '${jmsTemplateBeanName}' to use as a JmsTemplate")
        }

        def isTopic
        if (destination instanceof javax.jms.Destination) {
            isTopic = destination instanceof javax.jms.Topic
        } else {
            def destinationMap = jmsService.convertToDestinationMap(destination)
            isTopic = destinationMap.containsKey("topic")
            jmsTemplate.pubSubDomain = isTopic
            destination = (isTopic) ? destinationMap.topic : destinationMap.queue
        }

        if (LOG.infoEnabled) {
            def topicOrQueue = (isTopic) ? "topic" : "queue"
            def logMsg = "Sending JMS message '$message' to $topicOrQueue '$destination'"
            if (jmsTemplateBeanName != DEFAULT_JMS_TEMPLATE_BEAN_NAME)
                logMsg += " using template '$jmsTemplateBeanName'"
            LOG.info(logMsg)
        }

        GrailsMessagePostProcessor processor = new GrailsMessagePostProcessor(jmsService: this, jmsTemplate: jmsTemplate, processor: {
            it.JMSReplyTo = createDestination(callback)
            it
        })
        jmsTemplate.convertAndSend(destination, message, processor)
        jmsTemplate.setReceiveTimeout(5000)
        return jmsTemplate.receiveAndConvert(processor.createDestination(callback))
    }

    def convertToDestinationMap(destination) {
        jmsService.convertToDestinationMap(destination)
    }
}
