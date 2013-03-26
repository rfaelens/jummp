package net.biomodels.jummp.plugins.configuration

import grails.validation.Validateable

/**
 * Command Object for validating firstRun choice.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
@Validateable
class FirstRunCommand implements Serializable {
    private static final long serialVersionUID = 1L
    String firstRun

    static constraints = {
        firstRun(blank: false,
                nullable: false,
                validator: { firstRun, cmd ->
                    return (firstRun == "true" || firstRun == "false")
                }
        )
    }
}
