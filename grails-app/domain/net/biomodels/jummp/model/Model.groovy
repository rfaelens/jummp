package net.biomodels.jummp.model

import net.biomodels.jummp.core.model.ModelState
import net.biomodels.jummp.core.model.ModelTransportCommand

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
    private static final long serialVersionUID = 1L
    /**
     * A Model has many Revision
     * IMPORTANT: never access revisions directly as this circumvents the ACL!
     * Use ModelService.getAllRevisions()
     */
    static hasMany = [versions: ModelVersion]
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

    static mapping = {
        publication lazy: false
    }

    static constraints = {
        vcsIdentifier(nullable: false, blank: false, unique: true)
        name(nullable: false, unique: true, blank: false)
        versions(nullable: false, validator: { vers ->
            return !vers.isEmpty()
        })
        state(nullable: false)
        publication(nullable: true)
    }

    ModelTransportCommand toCommandObject() {
        // TODO: is it correct to show the latest upload date as the lastModifiedDate or does it need ACL restrictions?
        Set<String> creators = []
        if (revisions) {
            versions.each { version ->
                creators.add(version.owner.userRealName)
            }
        }
        return new ModelTransportCommand(id: id, name: name, state: state,
                lastModifiedDate: versions ? versions.sort{ it.versionNumber }.last().uploadDate : null,
                format: versions ? versions.sort{ it.versionNumber }.last().format.toCommandObject() : null,
                publication: publication ? publication.toCommandObject() : null,
                submitter: versions ? versions.sort{ it.versionNumber }.first().owner.userRealName : null,
                submissionDate: versions ? versions.sort{ it.versionNumber }.first().uploadDate : null,
                creators: creators
        )
    }
}
