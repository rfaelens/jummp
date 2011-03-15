import org.codehaus.groovy.grails.commons.ConfigurationHolder

class BootStrap {
    /**
     * Dependency injection of jummpApplicationAdapterService
     */
    def jummpApplicationAdapterService

    def init = { servletContext ->
        // TODO: proper app token
        ConfigurationHolder.config.jummpCore = jummpApplicationAdapterService.getJummpConfig("web application")
    }
    def destroy = {
    }
}
