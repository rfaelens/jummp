package net.biomodels.jummp.plugins.configuration

/**
 * Command Object for validating the trigger settings.
 * @author Jochen Schramm <j.schramm@dkfz-heidelberg.de>
 */
class TriggerCommand implements Serializable {
    private static final long serialVersionUID = 1L
    Long startRemoveOffset
    Long removeInterval
    Long maxInactiveTime

    static constraints = {
        startRemoveOffset(nullable: false, blank: false, minSize: 4)
        removeInterval(nullable: false, blank: false, minSize: 4)
        maxInactiveTime(nullable: false, blank: false, minSize: 4)
    }
}
