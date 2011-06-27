package net.biomodels.jummp.webapp.miriam

/**
 * Domain object to describe a MIRIAM resource provider.
 * Linked to MiriamDatatype.
 */
class MiriamResource {
    static belongsTo = [datatype: MiriamDatatype]
    /**
     * The identifier of the resource. E.g. "MIR:00100022"
     */
    String identifier
    /**
     * The location of the resource. In general a URL
     */
    String location
    /**
     * The URL to resolve one MIRIAM URI. $Id in the URL needs to be replaced.
     */
    String action
    /**
     * Whether the resource is obsoleted.
     */
    boolean obsolete = false

    static constraints = {
    }

    static mapping = {
        version false
        identifier unique: true
    }

    public String toString() {
        return "Id: ${identifier}, Location: ${location}, Action: ${action}, Obsolete: ${obsolete}"
    }
}
