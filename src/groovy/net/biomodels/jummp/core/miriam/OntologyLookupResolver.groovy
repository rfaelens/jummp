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
 * @short NameResolver using the Ontology Lookup Service.
 *
 * Parses the HTML provided by the Ontology Lookup Service, at the time of the
 * writing located at http://www.ebi.ac.uk/ontology-lookup/
 *
 * Obviously the parser will fail as soon as the HTML page is fundamentally changed.
 * If in future there is a REST XML API it should be used.
 *
 * @auhtor Martin Gräßlin <m.graesslin@dkfz.de>
 */
class OntologyLookupResolver implements NameResolver {
    /**
     * Dependency injection of Map consisting of datatype identifiers (Key) and resource identifiers (value)
     */
    Map<String, String> supportedIdentifiers

    boolean supports(MiriamDatatype datatype) {
        return supportedIdentifiers.containsKey(datatype.identifier) && MiriamResource.findByIdentifier(supportedIdentifiers[datatype.identifier])
    }

    String resolve(MiriamDatatype datatype, String id) {
        if (!supportedIdentifiers.containsKey(datatype.identifier)) {
            return null
        }

        MiriamResource resource = MiriamResource.findByIdentifierAndDatatype(supportedIdentifiers[datatype.identifier], datatype)
        if (!resource) {
            return null
        }

        def slurper = new XmlSlurper(new org.ccil.cowan.tagsoup.Parser())
        def htmlParser = slurper.parse("${resource.action.replace('$id', id)}")

        String text = htmlParser.'**'.find { it.@id == 'termName'}?.@value?.text()
        if (!text || text.isEmpty()) {
            return null
        }
        return text

    }
}
