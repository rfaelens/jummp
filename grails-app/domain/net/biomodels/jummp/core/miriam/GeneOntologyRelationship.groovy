package net.biomodels.jummp.core.miriam

/**
 * @short Domain class representing the relationship between two Gene Ontologies.
 *
 * @see GeneOntology
 * @author Martin Gräßlin <m.graesslin@dkfz.de>
 */
class GeneOntologyRelationship implements Serializable {

    /**
     * The Gene Ontology this relationship points from
     */
    GeneOntology from
    /**
     * The Gene Ontology this relationship points to
     */
    GeneOntology to
    /**
     * The type of the relationship
     */
    GeneOntologyRelationshipType type

    static constraints = {
    }
}
