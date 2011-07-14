package net.biomodels.jummp.core.miriam

import org.codehaus.groovy.grails.commons.ApplicationHolder

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
