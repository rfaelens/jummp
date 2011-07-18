package net.biomodels.jummp.core

import net.biomodels.jummp.core.miriam.GeneOntology
import net.biomodels.jummp.core.miriam.GeneOntologyRelationship
import net.biomodels.jummp.core.miriam.GeneOntologyTreeLevel

/**
 * @short Service to retrieve Gene Ontology Tree information.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz.de>
 */
class GeneOntologyTreeService {

    static transactional = true

    /**
     * Retrieves the next GO tree level under the Gene Ontology with the internal @p goId.
     *
     * The returned GeneOntologyTreeLevel contains all the information about the child
     * Gene Ontologies and the Revisions linked to this GeneOntology identified by @p goId.
     *
     * In case @p goId is @c null or @c 0, the root level is retrieved.
     *
     * @param goId The internal id, may be @c null or @c 0 to retrieve the Root level
     * @return Information about the next tree level.
     */
    GeneOntologyTreeLevel treeLevel(Long goId) {
        GeneOntology geneOntology = GeneOntology.get(goId)
        List<GeneOntology> geneOntologies
        if (geneOntology) {
            geneOntologies = nextLevel(geneOntology)
        } else {
            geneOntologies = rootLevel()
        }
        GeneOntologyTreeLevel level = new GeneOntologyTreeLevel()
        geneOntologies.each { go ->
            level.addOntology(go.id, go.description.identifier, go.description.name)
        }
        if (geneOntology) {
            geneOntology.revisions.each {
                level.addRevision(it.toCommandObject())
            }
        }
        return level
    }

    /**
     * Retrieves the root level
     * @return
     */
    private List<GeneOntology> rootLevel() {
        return GeneOntologyRelationship.createCriteria().list {
            to {
                isEmpty('relationships')
            }
            projections {
                distinct("to")
            }
        }
    }

    /**
     * Retrieves all GeneOntologies which have @p go as a parent.
     * @param go The parent GeneOntology
     * @return List of child GeneOntology
     */
    private List<GeneOntology> nextLevel(GeneOntology go) {
        if (!go) {
            return []
        }
        return GeneOntology.executeQuery("SELECT DISTINCT rel.from FROM GeneOntologyRelationship rel WHERE rel.to=:go", [go: go])
    }
}
