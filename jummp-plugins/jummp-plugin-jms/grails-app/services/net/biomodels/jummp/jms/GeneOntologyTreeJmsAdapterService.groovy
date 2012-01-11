package net.biomodels.jummp.jms

import grails.plugin.jms.Queue
import net.biomodels.jummp.webapp.ast.JmsAdapter
import net.biomodels.jummp.webapp.ast.JmsQueueMethod

@JmsAdapter
class GeneOntologyTreeJmsAdapterService  extends AbstractJmsAdapter {

    @SuppressWarnings("GrailsStatelessService")
    static exposes = ['jms']
    @SuppressWarnings("GrailsStatelessService")
    static destination = "jummpGeneOntologyTreeJms"
    static transactional = true

    /**
     * Dependency Injection of Gene Ontology Tree Service
     */
    def geneOntologyTreeService

    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[Long])
    def treeLevel(def message) {
        return geneOntologyTreeService.treeLevel(message[1] as Long)
    }
}
