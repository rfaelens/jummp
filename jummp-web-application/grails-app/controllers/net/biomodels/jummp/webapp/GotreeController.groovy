package net.biomodels.jummp.webapp

import grails.converters.JSON

/**
 * @short Controller to render the Gene Ontology Tree.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz.de>
 */
class GotreeController {

    /**
     * Dependency Injection
     */
    def remoteGeneOntologyTreeService

    /**
     * Default action, just rendering the div where the JavaScript will create the tree.
     */
    def index = {
        render {
            div(id:"gotree")
        }
    }

    /**
     * Retrieves the next level from the core application and generates the nodes for the Dynatree jquery plugin.
     */
    def level = {
        def nodes
        if (!params.id) {
            nodes = remoteGeneOntologyTreeService.treeLevel(0L)
        } else {
            nodes = remoteGeneOntologyTreeService.treeLevel(params.id as Long)
        }
        List jsonNodes = []
        nodes.ontologies.each {
            jsonNodes << [isLazy: true, title: "${it.identifier} - ${it.name}", goid: it.id, isFolder: true]
        }
        nodes.revisions.each {
            jsonNodes << [isLazy: false, title: it.model.name, revid: it.id, isFolder: false]
        }
        render jsonNodes as JSON
    }
}
