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
