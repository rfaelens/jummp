package net.biomodels.jummp.model

/**
 * @short Representation for a Publication.
 * A publication is used by a Model to reference the meta information
 * about the paper the Model belongs to.
 * @see Model
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class Publication {
    /**
     * A Publication is part of a Model.
     */
    static belongsTo = [model: Model]
    // TODO: can a publication have several models?
    /**
     * Name of the journal where the publication has been published
     */
    String journal
    /**
     * The title of the publication.
     */
    String title
    /**
     * The named author(s) of the publication.
     */
    String authors
    /**
     * The authors' affiliation.
     */
    String affiliation
    /**
     * The abstract of the publication.
     */
    String synopsis

    static constraints = {
        // TODO: do we need more than 250 characters?
        journal(nullable: false, blank: false)
        // TODO: do we need more than 250 characters?
        title(nullable: false, blank: false)
        // TODO: do we need more than 250 characters?
        authors(nullable: false, blank: false)
        // TODO: do we need more than 250 characters?
        affiliation(nullable: false, blank: false)
        // TODO: How long can an abstract be? Are 1000 characters sufficient?
        synopsis(nullable: false, blank: true, maxSize: 1000)
    }
}
