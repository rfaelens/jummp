package net.biomodels.jummp.model

import net.biomodels.jummp.plugins.security.User
import net.biomodels.jummp.core.model.RevisionTransportCommand

/**
 * @short A Revision represents one version of a Model file.
 * The Revision is stored in the Version Control System (VCS) and is linked
 * to the VCS by the id. The Revision is required to retrieve any file from
 * the VCS and to store new files.
 * A Revision is linked to one Model and each Model has several Revision, but
 * at least one.
 * @see Model
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class Revision implements Serializable {
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
     */
    String vcsId
    /**
     * Revision number in reference to the Model and not to the VCS.
     */
    Integer revisionNumber
    /**
     * The user who uploaded the Revision.
     */
    User owner
    /**
     * Whether the revision is a minor change or not.
     */
    Boolean minorRevision
    /**
     * The "commit message" of this revision.
     */
    String comment
    /**
     * The date when the Revision was uploaded.
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
    /**
     * Indicates whether this Revision is marked as deleted.
     */
    Boolean deleted = false
    // TODO: UML diagram lists a "format" and a "state". Do these belong here? What is the type of them?

    static constraints = {
        model(nullable: false)
        vcsId(nullable: false, unique: true)
        revisionNumber(nullable: false, unique: 'model')
        owner(nullable: false)
        minorRevision(nullable: false)
        uploadDate(nullable: false)
        comment(nullable: false, blank: true, maxSize: 1000)
        format(nullable: false)
        deleted(nullable: false)
    }

    RevisionTransportCommand toCommandObject() {
        return new RevisionTransportCommand(
                id: id,
                revisionNumber: revisionNumber,
                owner: owner.userRealName,
                minorRevision: minorRevision,
                comment: comment,
                uploadDate: uploadDate,
                format: format.toCommandObject(),
                model: model.toCommandObject())
    }
}
