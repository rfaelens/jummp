package net.biomodels.jummp.plugins.configuration

/**
 * Command Object for validating Remote settings (JMS and D-Bus).
 * @author Jochen Schramm <j.schramm@dkfz-heidelberg.de>
 */
class RemoteCommand implements Serializable {
    private static final long serialVersionUID = 1L
    String jummpRemote
    Boolean jummpExportDbus
    Boolean jummpExportJms

    static constraints = {
        jummpRemote(nullable: true, blank: false, inList: ['jms','dbus'])
        jummpExportDbus(blank: false, validator: { jummpExportDbus, cmd ->
            return  (jummpExportDbus || cmd.jummpExportJms )
        })
        jummpExportJms(blank: false, validator: { jummpExportJms, cmd ->
            return (cmd.jummpExportDbus || jummpExportJms)
        })
    }
}
