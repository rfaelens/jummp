package net.biomodels.jummp.model

import net.biomodels.jummp.core.model.AuthorTransportCommand

/**
 * @short Representation of an author of a publication.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class Author implements Serializable {
    String lastName
    String firstName
    String initials

    static constraints = {
        lastName(nullable: false)
        firstName(nullable: true)
        initials(nullable: true)
    }

    AuthorTransportCommand toCommandObject() {
        return new AuthorTransportCommand(lastName: lastName, firstName: firstName, initials: initials)
    }
}
