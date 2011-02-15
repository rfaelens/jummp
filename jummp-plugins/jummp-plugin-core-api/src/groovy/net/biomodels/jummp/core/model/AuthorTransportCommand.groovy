package net.biomodels.jummp.core.model

/**
 * @short Wrapper for an Author to be transported through JMS.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class AuthorTransportCommand implements Serializable {
    private static final long serialVersionUID = 1L
    String lastName
    String firstName
    String initials
}
