package net.biomodels.jummp.plugins.configuration

/**
 * Command Object for validating server settings.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class ServerCommand implements Serializable {
    /**
     *  The server url. It may not be localhost, this fails the validation, though
     * the IP address 127.0.0.1 validates
     */
    String url

    static constraints = {
        url(blank: false, nullable: false, url: true)
    }
}
