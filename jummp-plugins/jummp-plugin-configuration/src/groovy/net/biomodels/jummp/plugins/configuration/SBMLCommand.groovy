package net.biomodels.jummp.plugins.configuration

import grails.validation.Validateable

/**
 * Command Object for validating the SBML settings.
 * @author Jochen Schramm <j.schramm@dkfz-heidelberg.de>
 */
@Validateable
class SBMLCommand implements Serializable {
    private static final long serialVersionUID = 1L
    Boolean validation

    static constraints = {
        validation(nullable: false)
    }
}
