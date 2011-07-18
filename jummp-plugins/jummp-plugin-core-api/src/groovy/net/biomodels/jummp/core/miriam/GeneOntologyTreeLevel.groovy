package net.biomodels.jummp.core.miriam

import net.biomodels.jummp.core.model.RevisionTransportCommand

/**
 * @short Class representing one level of the Gene Ontology tree.
 *
 * Primarily used to pass the GO Tree information from core to web application.
 * The level contains information on all the child gene ontologies (internal id,
 * name and identifier) as well as a list of Revisions using connected to the
 * parent ontology this level derives from.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz.de>
 */
class GeneOntologyTreeLevel implements Serializable {
    private static final long serialVersionUID = 1L

    /**
     * internal class to represent one child Gene Ontology.
     */
    class GeneOntologyInformation implements Serializable {
        private final long serialVersionUID = 1L
        /**
         * internal id
         */
        Long id
        /**
         * Gene Ontology identifier, e.g. GO:0005575
         */
        String identifier
        /**
         * The human readable name of the Gene Ontology.
         */
        String name

        GeneOntologyInformation(Long id, String identifier, String name) {
            this.id = id
            this.identifier = identifier
            this.name = name
        }
    }

    /**
     * List of child ontologies in this level
     */
    List<GeneOntologyInformation> ontologies = []
    /**
     * List of revisions in this level
     */
    List<RevisionTransportCommand> revisions = []

    /**
     * Adds another Gene Ontology to this level if not already present.
     * @param id The internal id
     * @param identifier The Gene Ontolog identifier
     * @param name The human readable name
     * @throws IllegalArgumentException thrown in case any of the arguments are not valid
     */
    public void addOntology(Long id, String identifier, String name) throws IllegalArgumentException {
        if (!id || !identifier || !name) {
            throw new IllegalArgumentException("Id, identifier and Name have to be set")
        }
        for (GeneOntologyInformation info in ontologies) {
            if (info.id == id) {
                return
            }
        }
        ontologies << new GeneOntologyInformation(id, identifier, name)
    }

    /**
     * Adds another Revision to this level, if not already present.
     * @param revision The Revision to add
     * @throws IllegalArgumentException Thrown if the revision has no id.
     */
    public void addRevision(RevisionTransportCommand revision) throws IllegalArgumentException {
        if (!revision.id) {
            throw new IllegalArgumentException("Revision has no Id")
        }
        for (RevisionTransportCommand rev in revisions) {
            if (rev.id == revision.id) {
                return
            }
        }
        revisions << revision
    }
}
