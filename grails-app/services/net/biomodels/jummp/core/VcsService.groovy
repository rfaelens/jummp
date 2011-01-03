package net.biomodels.jummp.core

import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.NoSuchBeanDefinitionException
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
    @SuppressWarnings('GrailsStatelessService')
    private VcsManager vcsManager = null
    @SuppressWarnings('GrailsStatelessService')
    def grailsApplication

    void afterPropertiesSet() {
        String pluginServiceName = ConfigurationHolder.config.jummp.vcs.pluginServiceName
        if (pluginServiceName) {
            ApplicationContext ctx = (ApplicationContext)ApplicationHolder.getApplication().getMainContext()
            try {
                Vcs vcs = (Vcs)ctx.getBean(pluginServiceName)
                if (vcs.isValid()) {
                    vcsManager = vcs.vcsManager()
                } else {
                    log.error("Vcs service ${pluginServiceName} is not valid, disabling VcsService")
                }
            } catch(NoSuchBeanDefinitionException e) {
                log.error(e.getMessage())
                e.printStackTrace()
            } catch (VcsNotInitedException e) {
                vcsManager = null
                log.error(e.getMessage())
                e.printStackTrace()
            }
        } else {
            log.error("No vcs plugin service specified")
        }
    }

    /**
     * Checks whether the Version Control System is configured properly
     * @return @c true if the vcs system is configured properly, @c false otherwise
     */
    boolean isValid() {
        return (vcsManager != null)
    }

    // TODO: implement required methods
}
