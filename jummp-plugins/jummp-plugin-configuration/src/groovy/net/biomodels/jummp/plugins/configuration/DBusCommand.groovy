package net.biomodels.jummp.plugins.configuration

import grails.validation.Validateable

/**
 * Command Object for validating DBus settings (system or session).
 * @author Jochen Schramm <j.schramm@dkfz-heidelberg.de>
 */
@Validateable
class DBusCommand implements Serializable {
    private static final long serialVersionUID = 1L
    Boolean systemBus

    static constraints = {
        systemBus(blank: false, nullable: false)
        }
}
