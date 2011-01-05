package net.biomodels.jummp.plugins.configuration

/**
 * Command Object for validating firstRun choice.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class FirstRunCommand implements Serializable {
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
