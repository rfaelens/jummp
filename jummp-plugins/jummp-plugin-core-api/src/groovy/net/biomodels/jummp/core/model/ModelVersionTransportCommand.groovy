package net.biomodels.jummp.core.model

/**
 * @short Wrapper for a ModelVersion to be transported through JMS.
 *
 * Small wrapper class to decouple the ModelVersion from the Database.
 * Changes to instances of this class are not populated to the database.
 *
 * The object can also be used as a command object for the web interface.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class ModelVersionTransportCommand implements Serializable {
    private static final long serialVersionUID = 1L
    Long id
    /**
     * ModelVersion number in reference to the Model and not to the VCS.
     */
    Integer versionNumber
    /**
     * The real name of the user who uploaded the ModelVersion.
     */
    String owner
    /**
     * Whether the revision is a minor change or not.
     */
    Boolean minorVersion
    /**
     * The "commit message" of this revision.
     */
    String comment
    /**
     * The date when the ModelVersion was uploaded.
     */
    Date uploadDate
    /**
     * The format of the file in the VCS.
     */
    //ModelFormatTransportCommand format
    /**
     * The model the revision belongs to
     */
    ModelTransportCommand model
}
