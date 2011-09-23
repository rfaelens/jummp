package net.biomodels.jummp.plugins.configuration

/**
 * Command Object for validating DBus settings (system or session).
 * @author Jochen Schramm <j.schramm@dkfz-heidelberg.de>
 */
class DBusCommand implements Serializable {
    private static final long serialVersionUID = 1L
    Boolean systemBus

    static constraints = {
        systemBus(blank: false, nullable: false)
        }
}
