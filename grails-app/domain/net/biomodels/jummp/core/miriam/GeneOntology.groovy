package net.biomodels.jummp.core.miriam

import net.biomodels.jummp.model.ModelVersion

/**
 * @short Domain class representing a Gene Ontology term.
 *
 * The Gene Ontology term has one to many relationships to other Gene Ontologies.
 * For these relationships a special class is used: @link GeneOntologyRelationship
 * which also contains the type of the relationship.
 *
 * @see GeneOntologyRelationship
 * @see MiriamIdentifier
 * @author Martin Gräßlin <m.graesslin@dkfz.de>
 */
class GeneOntology implements Serializable {
    static hasMany = [versions: ModelVersion, relationships: GeneOntologyRelationship]
    static mappedBy = [relationships: "from"]
    /**
     * The MiriamIdentifier describing this Gene Ontology (e.g. the name)
     */
    MiriamIdentifier description

    static constraints = {
        description(unique: true)
    }
}
