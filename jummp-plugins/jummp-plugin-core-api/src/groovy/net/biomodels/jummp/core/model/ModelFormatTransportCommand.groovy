package net.biomodels.jummp.core.model

/**
 * @short Wrapper for a ModelFormat to be transported through JMS.
 *
 * Small wrapper class to decouple the ModelFormat from the Database.
 * Changes to instances of this class are not populated to the database.
 *
 * The object can also be used as a command object for the web interface.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class ModelFormatTransportCommand implements Serializable {
    private static final long serialVersionUID = 1L
    /**
     * The id in the database
     */
    Long id
    /**
     * A machine readable format name, to be used in the application. E.g. SBML
     */
    String identifier
    /**
     * A human readable more spoken name. E.g. Systems Biology Markup Language
     */
    String name
}
