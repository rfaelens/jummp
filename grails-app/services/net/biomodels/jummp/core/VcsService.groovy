package net.biomodels.jummp.core

import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.ApplicationContext
import net.biomodels.jummp.core.vcs.Vcs
import net.biomodels.jummp.core.vcs.VcsManager
import net.biomodels.jummp.core.vcs.VcsNotInitedException

/**
 * @short Service providing access to the version control system.
 *
 * This service allows the core to access the version control system provided by a plugin.
 * The service is only for internal use in the core by other services and should not be
 * made part of the external API.
 * @internal
 * @see VcsManager
 * @see Vcs
 * @author  Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class VcsService implements InitializingBean {
    static transactional = true
    private VcsManager vcsManager = null
    def grailsApplication

    void afterPropertiesSet() {
        String pluginServiceName = ConfigurationHolder.config.jummp.vcs.pluginServiceName
        if (pluginServiceName) {
            ApplicationContext ctx = (ApplicationContext)ApplicationHolder.getApplication().getMainContext()
            Vcs vcs = (Vcs)ctx.getBean(pluginServiceName)
            try {
                vcsManager = vcs.vcsManager()
            } catch (VcsNotInitedException e) {
                log.error(e.getMessage())
                e.printStackTrace()
            }
        }
    }

    // TODO: implement required methods
}
