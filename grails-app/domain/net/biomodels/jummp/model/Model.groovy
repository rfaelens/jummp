package net.biomodels.jummp.model

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
class Model {
    /**
     * A Model has many Revision and many Publication
     * IMPORTANT: never access revisions directly as this circumvents the ACL!
     * Use ModelService.getAllRevisions()
     */
    static hasMany = [revisions: Revision, publications: Publication]
    /**
     * The name of the Model
     */
    String name
    /**
     * The name of the corresponding file in the Version Control System.
     */
    String vcsIdentifier
    // TODO: unique Identifier for the model? UML diagram lists an "accessionNumber"?

    static constraints = {
        vcsIdentifier(nullable: false, blank: false, unique: true)
        name(nullable: false, unique: true, blank: false)
        revisions(nullable: false, validator: { revs ->
            return !revs.isEmpty()
        })
    }
}
