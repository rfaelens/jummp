package net.biomodels.jummp.plugins.configuration

import grails.validation.Validateable

/**
 * Command Object for validating server settings.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
@Validateable
class ServerCommand implements Serializable {
    private static final long serialVersionUID = 1L
    /**
     *  The server url. It may not be localhost, this fails the validation, though
     * the IP address 127.0.0.1 validates
     */
    String url
    /**
     * The web server url. It may not be localhost, this fails the validation, though
     * the IP address 127.0.0.1 validates
     */
    String weburl
    /**
     * Whether all web pages are password protected.
     **/
    Boolean protectEverything

    static constraints = {
        url(blank: false, nullable: false, url: true)
        weburl(blank: false, nullable: false, url: true)
        protectEverything(nullable: false)
    }
}
