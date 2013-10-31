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


package net.biomodels.jummp.core.miriam

import net.biomodels.jummp.core.model.RevisionTransportCommand
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject
import net.biomodels.jummp.core.model.ModelTransportCommand

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
        /**
         * The relationship to the parent element
         */
        GeneOntologyRelationshipType type

        GeneOntologyInformation(Long id, String identifier, String name, GeneOntologyRelationshipType type) {
            this.id = id
            this.identifier = identifier
            this.name = name
            this.type = type
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
     * @param type The relationship type to the parent gene ontology, maybe @c null for toplevel ontologies
     * @throws IllegalArgumentException thrown in case any of the arguments are not valid
     */
    public void addOntology(Long id, String identifier, String name, GeneOntologyRelationshipType type) throws IllegalArgumentException {
        if (!id || !identifier || !name) {
            throw new IllegalArgumentException("Id, identifier and Name have to be set")
        }
        for (GeneOntologyInformation info in ontologies) {
            if (info.id == id) {
                return
            }
        }
        ontologies << new GeneOntologyInformation(id, identifier, name, type)
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

    public static GeneOntologyTreeLevel fromJSON(String json) {
        def parsedJSON = JSON.parse(json)
        GeneOntologyTreeLevel level = new GeneOntologyTreeLevel()
        parsedJSON.ontologies.each { ontology ->
            GeneOntologyRelationshipType type = null
            if (ontology.type != JSONObject.NULL) {
                type = GeneOntologyRelationshipType.valueOf(GeneOntologyRelationshipType.class, ontology.type.name)
            }
            level.addOntology(ontology.id, ontology.identifier, ontology.name, type)
        }
        parsedJSON.revisions.each { revision ->
            level.addRevision(new RevisionTransportCommand(id: revision.id, revisionNumber: revision.revisionNumber,
                    model: new ModelTransportCommand(id: revision.model.id, name: revision.model.name)))
        }
        return level
    }
}
