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
     * Only needed to upload new Models. When retrieving Models set to format of latest revision.
     */
    ModelFormatTransportCommand format = null
    /**
     * Only needed to upload new Models. When retrieving Models it is unset.
     */
    String comment = null
    /**
     * The date of the latest revision the user has access to.
     */
    Date lastModifiedDate
    /**
     * Information about the Publication.
     */
    PublicationTransportCommand publication
    /**
     * The original submitter of the first revision.
     */
    String submitter
    /**
     * The date when the Model was uploaded first to the instance.
     */
    Date submissionDate
    /**
     * The names of all users who have worked on this Model.
     */
    Set<String> creators
}
