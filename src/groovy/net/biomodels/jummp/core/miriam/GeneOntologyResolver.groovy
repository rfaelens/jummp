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

/**
 * @short NameResolver able to resolve GeneOntology.
 *
 * Needs to be configured as a Spring Bean.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz.de>
 */
class GeneOntologyResolver implements NameResolver {
    /**
     * Dependency injection
     */
    def miriamService
    /**
     * Dependency Injection of the Miriam Datatype identifier this resolver can handle
     */
    String dataTypeIdentifier
    /**
     * Dependency Injection of the Miriam Resource identifier this resolver uses to resolve a name
     */
    String resourceIdentifier

    boolean supports(MiriamDatatype datatype) {
        return datatype.identifier == dataTypeIdentifier && MiriamResource.findByIdentifier(resourceIdentifier)
    }

    String resolve(MiriamDatatype datatype, String id) {
        if (datatype.identifier != dataTypeIdentifier) {
            return null
        }
        MiriamResource resource = MiriamResource.findByIdentifierAndDatatype(resourceIdentifier, datatype)
        if (!resource) {
            return null
        }
        def parsedXML = new XmlSlurper().parseText(getOboXML(resource.action, id))
        String text = parsedXML.term.name.text()

        parsedXML.term.is_a.each {
            String toId = it.text().trim()
            miriamService.queueGeneOntologyRelationship(id, toId, GeneOntologyRelationshipType.IsA, datatype.urn + ":" + URLEncoder.encode(toId))
        }
        parsedXML.term?.relationship?.each { relationship ->
            String toId = relationship.to.text().trim()
            GeneOntologyRelationshipType type
            switch (relationship.type.text().trim()) {
            case "part_of":
                type = GeneOntologyRelationshipType.PartOf
                break
            default:
                type = GeneOntologyRelationshipType.Other
                break
            }
            miriamService.queueGeneOntologyRelationship(id, toId, type, datatype.urn + ":" + URLEncoder.encode(toId))
        }
        if (text == "") {
            return null
        } else {
            return text
        }
    }

    private String getOboXML(String url, String id) {
        return new URL("${url.replace('$id', id)}&format=oboxml").getText()
    }
}
