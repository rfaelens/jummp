package net.biomodels.jummp.webapp.miriam

/**
 * Domain object to store one resolved id.
 * @author Martin Gräßlin <m.graesslin@dkfz.de>
 */
class MiriamIdentifier implements Serializable {
    /**
     * The Identifier of the MIRIAM URN (part after last colon.
     */
    String identifier
    /**
     * The MIRIAM Datatype
     */
    MiriamDatatype datatype
    /**
     * The resolved name for this identifier.
     */
    String name

    static mapping = {
        id composite: ['identifier', 'datatype']
    }

    static constraints = {
    }
}
