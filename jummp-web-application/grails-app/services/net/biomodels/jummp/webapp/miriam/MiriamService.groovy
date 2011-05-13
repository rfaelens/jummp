package net.biomodels.jummp.webapp.miriam

/**
 * Service for handling MIRIAM resources.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz.de>
 */
class MiriamService {

    static transactional = true

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
}
