package net.biomodels.jummp.plugins.configuration

/**
 * Command Object for validating branding settings.
 * @author Jochen Schramm <j.schramm@dkfz-heidelberg.de>
 */
class BrandingCommand implements Serializable {
    private static final long serialVersionUID = 1L

    String internalColor
    String externalColor

    static constraints = {
        internalColor(nullable: false, blank: false)
        externalColor(nullable: true, blank: true)
    }
}
