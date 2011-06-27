package net.biomodels.jummp.webapp.miriam

import org.codehaus.groovy.grails.plugins.codecs.URLCodec
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.TransactionStatus

/**
 * Service for handling MIRIAM resources.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz.de>
 */
class MiriamService {

    static transactional = false

    /**
     * Updates the MIRIAM Resources in the database from the XML specified in @p url.
     * @param url The URL to the MIRIAM Resource XML
     * @param force If @c true previously fetched data will be discarded, if @c false only new entries are added
     * @throws MiriamUpdateException In case an error occurs while downloading or parsing the XML
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void updateMiriamResources(String url, boolean force) throws MiriamUpdateException {
        // TODO: move into thread
        // we cannot throw the exception from inside the closure
        // therefore we store it in the variable and throw it after the withTransaction closure
        MiriamUpdateException exception = null
        MiriamResource.withTransaction { TransactionStatus status ->
            if (force) {
                // delete all MIRIAM data
                MiriamDatatype.list().each {
                    it.delete()
                }
            }
            try {
                parseResourceXML(url)
                status.flush()
            } catch (Exception e) {
                status.setRollbackOnly()
                log.info(e.getMessage(), e)
                exception = new MiriamUpdateException(e)
            }
        }
        if (exception) {
            throw exception
        }
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
        if (ConfigurationHolder.config.jummp.webapp.miriam.prefered.containsKey(datatype.identifier)) {
            // try to find the resource
            MiriamResource resource = (MiriamResource)datatype.resources.find{ it.identifier == ConfigurationHolder.config.jummp.webapp.miriam.prefered[datatype.identifier] }
            if (resource) {
                return resource
            }
        }
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
        MiriamIdentifier identifier = MiriamIdentifier.findByDatatypeAndIdentifier(miriam, id)
        if (identifier) {
            return identifier.name
        }
        String name = null
        try {
            switch (miriam.identifier) {
            // UniProt
            case "MIR:00000005":
                MiriamResource resource = (MiriamResource)miriam.resources.find { it.identifier == "MIR:00100134"}
                if (resource) {
                    name = resolveUniProt(resource, id)
                }
                break
            // Taxonomy
            case "MIR:00000006":
                MiriamResource resource = (MiriamResource)miriam.resources.find { it.identifier == "MIR:00100019"}
                if (resource) {
                    name = resolveTaxonomy(resource, id)
                }
                break
            // Gene Ontology
            case "MIR:00000022":
                MiriamResource resource = (MiriamResource)miriam.resources.find { it.identifier == "MIR:00100012"}
                if (resource) {
                    name = resolveGeneOntology(resource, id)
                }
                break
            default:
                // nothing
                break
            }
        } catch (IOException e) {
            // an IOException if thrown if the service we use to resolve the name is currently down
            log.debug(e.getMessage())
        }
        if (name && name != id) {
            identifier = new MiriamIdentifier(identifier: id, datatype: miriam, name: name)
            identifier.save(flush: true)
            return name
        }
        // fallback: to just cleaning for HTML
        return URLCodec.decode(id)
    }

    /**
     * Downloads the MIRIAM Resource description and parses it into MiriamDatatype domain objects.
     * @param url The URL to the XML file describing the MIRIAM resources
     */
    private void parseResourceXML(String url) {
        // TODO: move parsing into a Thread
        String xml = new URL(url).getText()
        def rootNode = new XmlSlurper().parseText(xml)
        rootNode.datatype.each { datatype ->
            MiriamDatatype miriam = MiriamDatatype.findByIdentifier(datatype.@id.text())
            if (!miriam) {
                miriam = new MiriamDatatype()
            }
            miriam.identifier = datatype.@id.text()
            miriam.name = datatype.name.text()
            miriam.pattern = datatype.@pattern.text()
            miriam.urn = datatype.uris.uri.find { it.@type.text() == "URN" }.text()
            datatype.synonyms.synonym.each { synonym ->
                if (!miriam.synonyms.contains(synonym.text())) {
                    miriam.synonyms << synonym.text()
                }
            }
            datatype.resources.resource.each { resource ->
                MiriamResource res = MiriamResource.findByIdentifier(resource.@id.text())
                if (!res) {
                    res = new MiriamResource()
                }
                res.identifier = resource.@id.text()
                res.location = resource.dataResource.text()
                res.action = resource.dataEntry.text()
                if (resource.@obsolete?.text() == "true") {
                    res.obsolete = true
                }
                if (res.id == null || !miriam.resources.contains(res)) {
                    miriam.addToResources(res)
                } else {
                    res.save()
                }
            }
            miriam.save()
        }
    }

    /**
     * Resolves the name for the given taxonomy by downloading the RDF provided by the
     * MIRIAM resource.
     *
     * @param resource The MIRIAM resource to use for downloading the RDF
     * @param id The id of the taxonomy
     * @return The resolved taxonomy if it could be resolved, if not @c null
     */
    private String resolveTaxonomy(MiriamResource resource, String id) {
        String xml = new URL("${resource.action.replace('$id', id)}.rdf").getText()
        def rootNode = new XmlSlurper().parseText(xml)
        String text = rootNode.Description.scientificName.text()
        if (text == "") {
            return null
        } else {
            return text
        }
    }

    /**
     * Resolves the name for the given gene ontology by downloading the obo xml provided by the
     * MIRIAM resource.
     *
     * @param resource The MIRIAM resource to use for downloading the obo xml
     * @param id The id of the taxonomy
     * @return The resolved gene ontology if it could be resolved, if not @c null
     */
    private String resolveGeneOntology(MiriamResource resource, String id) {
        String xml = new URL("${resource.action.replace('$id', id)}&format=oboxml").getText()
        def rootNode = new XmlSlurper().parseText(xml)
        String text = rootNode.term.name.text()
        if (text == "") {
            return null
        } else {
            return text
        }

    }

    /**
     * Resolves the name of the given protein by downloading the xml description from UniProt.
     *
     * @param resource The MIRIAM resource to use for downloading the xml description
     * @param id The UniProt id
     * @return The resolved protein name if it could be resolved, if not @c null
     */
    private String resolveUniProt(MiriamResource resource, String id) {
        String xml = new URL("${resource.action.replace('$id', id)}.xml").getText()
        def rootNode = new XmlSlurper().parseText(xml)
        String text = rootNode.entry.name.text()
        if (text == "") {
            return null
        } else {
            return text
        }
    }
}
