package net.biomodels.jummp.plugins.configuration

/**
 * Command Object for validating the SBML settings.
 * @author Jochen Schramm <j.schramm@dkfz-heidelberg.de>
 */
class SBMLCommand implements Serializable {
    private static final long serialVersionUID = 1L
    Boolean validation

    static constraints = {
        validation(blank: false)
    }
}
