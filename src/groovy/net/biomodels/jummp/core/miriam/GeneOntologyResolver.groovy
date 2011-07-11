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
        String xml = new URL("${resource.action.replace('$id', id)}&format=oboxml").getText()
        def rootNode = new XmlSlurper().parseText(xml)
        String text = rootNode.term.name.text()
        if (text == "") {
            return null
        } else {
            return text
        }
    }
}
