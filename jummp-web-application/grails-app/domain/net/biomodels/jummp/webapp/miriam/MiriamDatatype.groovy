package net.biomodels.jummp.webapp.miriam

/**
 * Domain object to describe a MIRIAM datatype.
 * @author Martin Gräßlin <m.graesslin@dkfz.de>
 */
class MiriamDatatype implements Serializable {
    static hasMany = [resources: MiriamResource, identifiers: MiriamIdentifier]
    /**
     * The identifier of the MIRIAM datatype. E.g. "MIR:00000014"
     */
    String identifier
    /**
     * The name of the MIRIAM datatype. E.g. "KEGG Reaction"
     */
    String name
    /**
     * List of synonym names for the MIRIAM datatype.
     */
    List<String> synonyms = []
    /**
     * The pattern associated with the datatype. E.g. "^R\d+$"
     */
    String pattern
    /**
     * The URN namespace of the MIRIAM datatype. E.g. urn:miriam:kegg.reaction
     */
    String urn

    static constraints = {
    }

    static mapping = {
        version false
        identifier unique: true
    }

    public String toString() {
        return "Id: ${identifier}, Name: ${name}, Pattern: ${pattern}, URI: ${uri}, Synonyms: ${synonyms}, Resources: ${resources}"
    }
}
