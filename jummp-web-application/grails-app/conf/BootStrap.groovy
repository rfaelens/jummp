import org.codehaus.groovy.grails.commons.ConfigurationHolder

class BootStrap {
    /**
     * Dependency injection of core adapter service
     */
    def coreAdapterService

    def init = { servletContext ->
        // TODO: proper app token
        ConfigurationHolder.config.jummpCore = coreAdapterService.getJummpConfig("web application")
    }
    def destroy = {
    }
}
