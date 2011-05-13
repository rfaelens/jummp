import org.codehaus.groovy.grails.commons.ConfigurationHolder

class BootStrap {
    /**
     * Dependency injection of remoteJummpApplicationAdapter
     */
    def remoteJummpApplicationAdapter
    /**
     * Dependency Injection of miriamService in order to parse during bootstrap
     */
    def miriamService

    def init = { servletContext ->
        // TODO: proper app token
        ConfigurationHolder.config.jummpCore = remoteJummpApplicationAdapter.getJummpConfig("web application")
        miriamService.init()
    }
    def destroy = {
    }
}
