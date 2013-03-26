package net.biomodels.jummp.plugins.configuration

import grails.validation.Validateable

/**
 * Command Object for validating the trigger settings.
 * @author Jochen Schramm <j.schramm@dkfz-heidelberg.de>
 */
@Validateable
class TriggerCommand implements Serializable {
    private static final long serialVersionUID = 1L
    Long startRemoveOffset
    Long removeInterval
    Long maxInactiveTime

    static constraints = {
        startRemoveOffset(nullable: false, blank: false, min: 1000L)
        removeInterval(nullable: false, blank: false, min: 1000L)
        maxInactiveTime(nullable: false, blank: false, min: 1000L)
    }
}
