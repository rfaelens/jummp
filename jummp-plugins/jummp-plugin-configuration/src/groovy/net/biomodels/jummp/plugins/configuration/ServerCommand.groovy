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
     * The server url. Use IP address 127.0.0.1 instead of localhost to avoid validation issues
     */
    String url
    /**
     * Whether all web pages are password protected.
     **/
    Boolean protectEverything

    static constraints = {
        url(blank: false, nullable: false, url: true)
        protectEverything(nullable: false)
    }
}
