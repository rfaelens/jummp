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
    def geneOntologyTreeService

    /**
     * Default action, just rendering the div where the JavaScript will create the tree.
     */
    def index = {
    }

    /**
     * Retrieves the next level from the core application and generates the nodes for the Dynatree jquery plugin.
     */
    def level = {
        def nodes
        if (!params.id || params.id == "0") {
            nodes = geneOntologyTreeService.treeLevel(-1L)
        } else {
            nodes = geneOntologyTreeService.treeLevel(params.id as Long)
        }
        List jsonNodes = []
        nodes.ontologies.each {
            String icon = null
            if (it.type) {
                switch (it.type) {
                case "IsA":
                    icon = "go_isa.gif"
                    break
                case "PartOf":
                    icon = "go_partof.gif"
                    break
                case "DevelopFrom":
                    icon = "go_devfrom.gif"
                    break
                case "Other":
                default:
                    icon = "go_other.gif"
                    break
                }
            }
            jsonNodes << [isLazy: true, title: "${it.identifier} - ${it.name}", goid: it.id, isFolder: true, icon: icon]
        }
        nodes.revisions.each {
            jsonNodes << [isLazy: false, title: it.model.name, modelId: it.model.id, revisionNumber: it.revisionNumber, isFolder: false]
        }
        if (jsonNodes.isEmpty()) {
            jsonNodes << [isLazy: false, title: "No Model found", isFolder: false]
        }
        render jsonNodes as JSON
    }
}
