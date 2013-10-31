/**
* Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI), Deutsches Krebsforschungszentrum (DKFZ)
*
* This file is part of Jummp.
*
* Jummp is free software; you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as
* published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
*
* Jummp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License along with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
*
* Additional permission under GNU Affero GPL version 3 section 7
*
* If you modify Jummp, or any covered work, by linking or combining it with [name of library] (or a modified version of that
* library), containing parts covered by the terms of [name of library's license], the licensors of this Program grant you additional
* permission to convey the resulting work. {Corresponding Source for a non-source form of such a combination shall include the source
* code for the parts of [name of library] used as well as that of the covered work.}
**/


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
