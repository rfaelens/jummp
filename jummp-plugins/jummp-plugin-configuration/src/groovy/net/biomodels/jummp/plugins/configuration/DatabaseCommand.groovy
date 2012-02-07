package net.biomodels.jummp.plugins.configuration

/**
 * Command Object for validating database settings.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class DatabaseCommand implements Serializable {
    private static final long serialVersionUID = 1L

    DatabaseType type
    String username
    String password
    String server
    Integer port
    String database

    static constraints = {
        type(nullable: false, blank: false)
        username(nullable: false, blank: false)
        password(nullable: false, blank: true)
        // TODO: add constraints for a fqdn or IP address
        server(nullable: false, blank: false)
        port(nullable: false, range: 0..65535)
        database(nullable: false, blank: false)
    }
}
