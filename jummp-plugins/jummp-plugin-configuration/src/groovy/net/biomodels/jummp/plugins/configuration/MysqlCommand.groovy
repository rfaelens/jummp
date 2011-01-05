package net.biomodels.jummp.plugins.configuration

/**
 * Command Object for validating MySQL settings.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class MysqlCommand implements Serializable {
    String username
    String password
    String server
    Integer port
    String database

    static constraints = {
        username(nullable: false, blank: false)
        password(nullable: false, blank: true)
        // TODO: add constraints for a fqdn or IP address
        server(nullable: false, blank: false)
        port(nullable: false, range: 0..65535)
        database(nullable: false, blank: false)
    }
}
