import org.codehaus.groovy.grails.commons.ConfigurationHolder

class BootStrap {
    /**
     * Dependency injection of remoteJummpApplicationAdapter
     */
    def remoteJummpApplicationAdapter

    def init = { servletContext ->
        // TODO: proper app token
        ConfigurationHolder.config.jummpCore = remoteJummpApplicationAdapter.getJummpConfig("web application")
    }
    def destroy = {
    }
}
