import org.codehaus.groovy.grails.commons.ConfigurationHolder

class BootStrap {
    /**
     * Dependency injection of jummpApplicationAdapterService
     */
    def jummpApplicationJmsRemoteAdapter

    def init = { servletContext ->
        // TODO: proper app token
        ConfigurationHolder.config.jummpCore = jummpApplicationJmsRemoteAdapter.getJummpConfig("web application")
    }
    def destroy = {
    }
}
