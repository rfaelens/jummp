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

    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[Long, Integer, String, String, List, List, List, List, List])
    def triggerSubmodelGeneration(def message) {
        return sbmlService.triggerSubmodelGeneration(modelDelegateService.getRevision((Long)message[1], (Integer)message[2]), (String)message[3], (String)message[4], (List) message[5], (List)message[6], (List)message[7], (List)message[8], (List)message[9])
    }

    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[Long, Integer])
    def generateSvg(def message) {
        return sbmlService.generateSvg(modelDelegateService.getRevision((Long)message[1], (Integer)message[2]))
    }

    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[Long, Integer])
    def generateOctave(def message) {
        return sbmlService.generateOctave(modelDelegateService.getRevision((Long)message[1], (Integer)message[2]))
    }

    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[Long, Integer])
    def generateBioPax(def message) {
        return sbmlService.generateOctave(modelDelegateService.getRevision((Long)message[1], (Integer)message[2]))
    }
}
