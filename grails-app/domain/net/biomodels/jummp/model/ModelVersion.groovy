package net.biomodels.jummp.model

import net.biomodels.jummp.plugins.security.User
import net.biomodels.jummp.core.model.ModelVersionTransportCommand

/**
 * @short A ModelVersion represents one version of a model.
 * The ModelVersion is stored in the Version Control System (VCS) and is linked
 * to the VCS through a list of Revisions. The ModelVersion is required to retrieve files corresponding to a version from
 * the VCS and to store new files.
 * A ModelVersion is linked to one Model and each Model has several ModelVersions, but
 * at least one.
 * @see Model
 * @author Raza Ali <raza.ali@ebi.ac.uk>
 */
class ModelVersion implements Serializable {
    private static final long serialVersionUID = 1L
    /**
     * The revision belongs to one Model
     */
    static belongsTo = [model: Model]
    /**
     * The revision number in the version control system.
     * E.g. in Subversion the global revision number of the
     * commit which stored this Model Revision or in git the
     * Sha1-sum of the commit which stored this Model Revision.
     *
    String vcsId*/
    /**
     * Revision number in reference to the Model and not to the VCS.
     */
    Integer versionNumber
    /**
     * The user who uploaded the Version
     */
    User owner
    /**
     * Whether the version is a minor change or not.
     */
    Boolean minorVersion
    /**
     * The "commit message" of this revision. Needs to be handled better
     */
    String comment
    /**
     * The date when the version was last updated.
     */
    Date uploadDate
    /**
     * The model the revision belongs to
     */
    Model model
    /**
     * The format of the file in the VCS.
     * Kept in the Revision and not in the Model to make it possible to upload a new Revision in a different format.
     */
    ModelFormat format
    /*
     * Indicates whether this version is marked as deleted.
     */
    Boolean deleted = false
    // TODO: UML diagram lists a "format" and a "state". Do these belong here? What is the type of them?

    static constraints = {
        model(nullable: false)
        versionNumber(nullable: false, unique: 'model')
        owner(nullable: false)
        minorVersion(nullable: false)
        uploadDate(nullable: false)
        comment(nullable: false, blank: true, maxSize: 1000)
        format(nullable: false)
        deleted(nullable: false)
    }

    ModelVersionTransportCommand toCommandObject() {
        return new ModelVersionTransportCommand(
                id: id,
                versionNumber: versionNumber,
                owner: owner.userRealName,
                minorVersion: minorVersion,
                comment: comment,
                uploadDate: uploadDate,
                format: format.toCommandObject(),
                model: model.toCommandObject())
    }
}
