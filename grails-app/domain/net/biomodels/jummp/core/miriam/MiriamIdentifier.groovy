package net.biomodels.jummp.core.miriam

/**
 * Domain object to store one resolved id.
 * @author Martin Gräßlin <m.graesslin@dkfz.de>
 */
class MiriamIdentifier implements Serializable {
    static belongsTo = [datatype: MiriamDatatype]
    /**
     * The Identifier of the MIRIAM URN (part after last colon.
     */
    String identifier
    /**
     * The resolved name for this identifier.
     */
    String name

    static mapping = {
        identifier(unique: 'datatype')
    }

    static constraints = {
    }
}
