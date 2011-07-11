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
     * Dependency Injection of the Miriam Datatype identifier this resolver can handle
     */
    String dataTypeIdentifier
    /**
     * Dependency Injection of the Miriam Resource identifier this resolver uses to resolve a name
     */
    String resourceIdentifier
    /**
     * The parsed xml file downloaded during resolving.
     */
    private def parsedXML = null

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
        parsedXML = new XmlSlurper().parseText(getOboXML(resource.action, id))
        String text = parsedXML.term.name.text()
        if (text == "") {
            return null
        } else {
            return text
        }
    }

    void resolveRelationship(GeneOntology ontology) {
        if (!parsedXML) {
            MiriamResource resource = MiriamResource.findByIdentifierAndDatatype(resourceIdentifier, ontology.description.datatype)
            if (!resource) {
                return
            }
            parsedXML = new XmlSlurper().parseText(getOboXML(resource.action, ontology.description.identifier))
        }
        parsedXML.term.is_a.each {
            addRelationship(ontology, GeneOntologyRelationshipType.IsA, URLEncoder.encode(it.text().trim()))
        }
        parsedXML.term.relationship.each { relationship ->
            String id = URLEncoder.encode(relationship.to.text().trim())
            switch (relationship.type.text().trim()) {
            case "part_of":
                addRelationship(ontology, GeneOntologyRelationshipType.PartOf, id)
                break
            default:
                addRelationship(ontology, GeneOntologyRelationshipType.Other, id)
                break
            }
        }
    }

    private String getOboXML(String url, String id) {
        return new URL("${url.replace('$id', id)}&format=oboxml").getText()
    }

    private void addRelationship(GeneOntology ontology, GeneOntologyRelationshipType type, String id) {
        MiriamIdentifier miriamIdentifier = MiriamIdentifier.findByIdentifier(id)
        if (!miriamIdentifier) {
            GeneOntologyResolver resolver = ApplicationHolder.application.mainContext.getBean("geneOntologyResolver") as GeneOntologyResolver
            MiriamDatatype datatype = MiriamDatatype.findByIdentifier(dataTypeIdentifier)
            if (!datatype) {
                return
            }
            String resolvedName = resolve(datatype, id)
            if (!resolvedName) {
                return
            }

            miriamIdentifier = new MiriamIdentifier(identifier: id, datatype: datatype, name: resolvedName)
            miriamIdentifier = miriamIdentifier.save(flush: true)
            GeneOntology geneOntology = new GeneOntology(description: miriamIdentifier)
            geneOntology.save(flush: true)
            resolver.resolveRelationship(geneOntology)
        }
        GeneOntology toOntology = GeneOntology.findByDescription(miriamIdentifier, [lock:true])
        if (!toOntology) {
            return
        }
        GeneOntologyRelationship relationship = new GeneOntologyRelationship(from: ontology, to: toOntology, type: type)
        ontology.addToRelationships(relationship)
        relationship.save(flush: true)
    }
}
