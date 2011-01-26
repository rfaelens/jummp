package net.biomodels.jummp.core.model

/**
 * @short Wrapper for a Model to be transported through JMS.
 *
 * Small wrapper class to decouple the Model from the Database.
 * Changes to instances of this class are not populated to the database.
 * The object does not contain references to the Revisions. Use the
 * service methods to retrieve Revisions of this Model.
 *
 * The object can also be used as a command object for the web interface.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class ModelTransportCommand implements Serializable {
    private static final long serialVersionUID = 1L
    Long id
    String name
    ModelState state
    /**
     * Only needed to upload new Models. When retrieving Models it is unset.
     */
    ModelFormat format = null
    /**
     * Only needed to upload new Models. When retrieving Models it is unset.
     */
    String comment = null
}
