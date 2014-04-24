/**
* Copyright (C) 2010-2014 EMBL-European Bioinformatics Institute (EMBL-EBI),
* Deutsches Krebsforschungszentrum (DKFZ)
*
* This file is part of Jummp.
*
* Jummp is free software; you can redistribute it and/or modify it under the
* terms of the GNU Affero General Public License as published by the Free
* Software Foundation; either version 3 of the License, or (at your option) any
* later version.
*
* Jummp is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
* details.
*
* You should have received a copy of the GNU Affero General Public License along
* with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
**/





package net.biomodels.jummp.core.miriam

import net.biomodels.jummp.model.Revision

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
    static hasMany = [revisions: Revision, relationships: GeneOntologyRelationship]
    static mappedBy = [relationships: "from"]
    /**
     * The MiriamIdentifier describing this Gene Ontology (e.g. the name)
     */
    MiriamIdentifier description

    static constraints = {
        description(unique: true)
    }
}
