package net.biomodels.jummp.model

import net.biomodels.jummp.core.model.ModelState
import net.biomodels.jummp.core.model.ModelTransportCommand
import net.biomodels.jummp.core.model.AuthorTransportCommand

/**
 * @short Representation of one Model.
 * This class is the representation of one Model. It contains the reference
 * to the model file stored in the version control system and the references
 * to the meta information such as publications and the list of revisions of
 * the Model.
 * The Model is the central domain class of Jummp.
 * @see Revision
 * @see Publication
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class Model implements Serializable {
    /**
     * A Model has many Revision
     * IMPORTANT: never access revisions directly as this circumvents the ACL!
     * Use ModelService.getAllRevisions()
     */
    static hasMany = [revisions: Revision]
    /**
     * The name of the Model
     */
    String name
    /**
     * The name of the corresponding file in the Version Control System.
     */
    String vcsIdentifier
    /**
     * The state of the Model, by default UNPUBLISHED
     */
    ModelState state = ModelState.UNPUBLISHED
    /**
     * The Publication the model has been described in
     */
    Publication publication
    // TODO: unique Identifier for the model? UML diagram lists an "accessionNumber"?

    static constraints = {
        vcsIdentifier(nullable: false, blank: false, unique: true)
        name(nullable: false, unique: true, blank: false)
        revisions(nullable: false, validator: { revs ->
            return !revs.isEmpty()
        })
        state(nullable: false)
        publication(nullable: true)
    }

    ModelTransportCommand toCommandObject() {
        // TODO: is it correct to show the latest upload date as the lastModifiedDate or does it need ACL restrictions?
        Set<String> creators = []
        if (revisions) {
            revisions.each { revision ->
                creators.add(revision.owner.userRealName)
            }
        }
        return new ModelTransportCommand(id: id, name: name, state: state,
                lastModifiedDate: revisions ? revisions.sort{ it.revisionNumber }.last().uploadDate : null,
                format: revisions ? revisions.sort{ it.revisionNumber }.last().format.toCommandObject() : null,
                publication: publication ? publication.toCommandObject() : null,
                submitter: revisions ? revisions.sort{ it.revisionNumber }.first().owner.userRealName : null,
                submissionDate: revisions ? revisions.sort{ it.revisionNumber }.first().uploadDate : null,
                creators: creators
        )
    }
}
