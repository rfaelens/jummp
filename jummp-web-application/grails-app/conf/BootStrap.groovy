import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.codehaus.groovy.grails.plugins.springsecurity.SecurityFilterPosition
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

class BootStrap {
    /**
     * Dependency injection of remoteJummpApplicationAdapter
     */
    def remoteJummpApplicationAdapter

    def init = { servletContext ->
        // TODO: proper app token
        ConfigurationHolder.config.jummpCore = remoteJummpApplicationAdapter.getJummpConfig("web application")
        SpringSecurityUtils.clientRegisterFilter('authenticationCheckFilter', SecurityFilterPosition.SECURITY_CONTEXT_FILTER.order + 10)
    }
    def destroy = {
    }
}
