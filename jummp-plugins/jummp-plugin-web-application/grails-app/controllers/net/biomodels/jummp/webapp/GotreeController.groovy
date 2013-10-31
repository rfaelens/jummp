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
            jsonNodes << [isLazy: true, title: "${it.identifier} - ${it.name}", goid: it.id, isFolder: true, icon: icon, key: "${it.id}"]
        }
        nodes.revisions.each {
            jsonNodes << [isLazy: false, title: it.model.name, modelId: it.model.id, revisionNumber: it.revisionNumber, isFolder: false]
        }
        if (jsonNodes.isEmpty()) {
            jsonNodes << [isLazy: false, title: "No Model found", isFolder: false]
        }
        render jsonNodes as JSON
    }

    /**
     * Searches Gene Ontologies for matching elements.
     **/
    def search = {
        List ontologies = geneOntologyTreeService.searchOntologies(params.id)
        List jsonElements = []
        ontologies.each {
            jsonElements << [
                value: "${it.description.identifier} - ${it.description.name}",
                id: it.id,
                goId: it.description.identifier,
                goTerm: it.description.name]
        }
        render jsonElements as JSON
    }

    /**
     * Provides the path of the gene ontology.
     **/
    def path = {
        Map data = [path: geneOntologyTreeService.findPath(params.id as Long)]
        render data as JSON
    }
}
