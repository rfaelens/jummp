package net.biomodels.jummp.webapp.miriam

import org.codehaus.groovy.grails.plugins.codecs.URLCodec

/**
 * Service for handling MIRIAM resources.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz.de>
 */
class MiriamService {

    static transactional = true
    /**
     * Hash of already resolved taxonomies. Key is the identifier, value the resolved name
     */
    private Map<String, String> taxonomies = [:]
    /**
     * Hash of already resolved gene ontologies. Key is the identifier, value the resolved name
     */
    private Map<String, String> geneOntologies = [:]

    /**
     * Helper method to be called from Bootstrap
     */
    public void init() {
        parseResourceXML()
    }

    /**
     *
     * @param uri The MIRIAM uri
     * @return The MiriamDatatype for the given MIRIAM uri
     */
    public MiriamDatatype resolveDatatype(String urn) {
        return MiriamDatatype.findByUrn(urn)
    }

    /**
     *
     * @param datatype The MiriamDatatype to check
     * @return The preferred Resource for the given datatype
     */
    public MiriamResource preferredResource(MiriamDatatype datatype) {
        return (MiriamResource)(datatype.resources.findAll { !it.obsolete }.sort { it.id }?.first())
    }

    /**
     * Tries to resolve a name for the given MIRIAM datatype.
     * Uses some well known web services to connect to.
     * If there is no well known web service the passed in id is cleaned for HTML output and returned.
     * @param miriam The datatype
     * @param id The identifier part of the URN.
     * @return Either the resolved name, or HTML cleaned id.
     */
    public String resolveName(MiriamDatatype miriam, String id) {
        switch (miriam.identifier) {
        // Taxonomy
        case "MIR:00000006":
            MiriamResource resource = (MiriamResource)miriam.resources.find { it.identifier == "MIR:00100019"}
            if (resource) {
                return resolveTaxonomy(resource, id)
            }
            break
        // Gene Ontology
        case "MIR:00000022":
            MiriamResource resource = (MiriamResource)miriam.resources.find { it.identifier == "MIR:00100012"}
            if (resource) {
                return resolveGeneOntology(resource, id)
            }
            break
        default:
            // nothing
            break
        }
        // fallback: to just cleaning for HTML
        return URLCodec.decode(id)
    }

    /**
     * Downloads the MIRIAM Resource description and parses it into MiriamDatatype domain objects.
     */
    private void parseResourceXML() {
        // TODO: URL should be read from configuration
        // TODO: move parsing into a Thread
        String xml = new URL("http://www.ebi.ac.uk/miriam/main/export/xml/").getText()
        def rootNode = new XmlSlurper().parseText(xml)
        rootNode.datatype.each { datatype ->
            MiriamDatatype miriam = new MiriamDatatype()
            miriam.identifier = datatype.@id.text()
            miriam.name = datatype.name.text()
            miriam.pattern = datatype.@pattern.text()
            miriam.urn = datatype.uris.uri.find { it.@type.text() == "URN" }.text()
            datatype.synonyms.synonym.each { synonym ->
                miriam.synonyms << synonym.text()
            }
            datatype.resources.resource.each { resource ->
                MiriamResource res = new MiriamResource()
                res.identifier = resource.@id.text()
                res.location = resource.dataResource.text()
                res.action = resource.dataEntry.text()
                if (resource.@obsolete?.text() == "true") {
                    res.obsolete = true
                }
                miriam.addToResources(res)
            }
            miriam.save()
        }
    }

    /**
     * Resolves the name for the given taxonomy by downloading the RDF provided by the
     * MIRIAM resource. If the name could be resolved it is added to a hash for further
     * fast lookup.
     *
     * @param resource The MIRIAM resource to use for downloading the RDF
     * @param id The id of the taxonomy
     * @return The resolved taxonomy, or the passed in id.
     */
    private String resolveTaxonomy(MiriamResource resource, String id) {
        if (taxonomies.containsKey(id)) {
            return taxonomies[id]
        }
        String xml = new URL("${resource.action.replace('$id', id)}.rdf").getText()
        def rootNode = new XmlSlurper().parseText(xml)
        String text = rootNode.Description.scientificName.text()
        if (text == "") {
            return URLCodec.decode(id)
        } else {
            taxonomies.put(id, text)
            return text
        }
    }

    /**
     * Resolves the name for the given gene ontology by downloading the obo xml provided by the
     * MIRIAM resource. If the name could be resolved it is added to a hash for further
     * fast lookup.
     *
     * @param resource The MIRIAM resource to use for downloading the obo xml
     * @param id The id of the taxonomy
     * @return The resolved taxonomy, or the passed in id.
     */
    private String resolveGeneOntology(MiriamResource resource, String id) {
        if (geneOntologies.containsKey(id)) {
            return geneOntologies[id]
        }
        String xml = new URL("${resource.action.replace('$id', id)}&format=oboxml").getText()
        def rootNode = new XmlSlurper().parseText(xml)
        String text = rootNode.term.name.text()
        if (text == "") {
            return URLCodec.decode(id)
        } else {
            geneOntologies.put(id, text)
            return text
        }

    }
}
