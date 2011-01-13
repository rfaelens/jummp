package net.biomodels.jummp.core

import junit.framework.AssertionFailedError
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.context.ApplicationContext
import net.biomodels.jummp.core.vcs.Vcs
import net.biomodels.jummp.core.vcs.VcsManager
import net.biomodels.jummp.core.vcs.VcsNotInitedException
import net.biomodels.jummp.core.vcs.VcsException
import org.springframework.security.access.prepost.PreAuthorize
import net.biomodels.jummp.model.Model

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
            try {
                ApplicationContext ctx = (ApplicationContext)ApplicationHolder.getApplication().getMainContext()
                Vcs vcs = (Vcs)ctx.getBean(pluginServiceName)
                if (vcs.isValid()) {
                    vcsManager = vcs.vcsManager()
                } else {
                    log.error("Vcs service ${pluginServiceName} is not valid, disabling VcsService")
                }
            } catch (AssertionFailedError e) {
                log.debug("Assertion during integration test - this is expected and does not matter")
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

    /**
    * Updates a Model file previously imported to the VCS.
    * Copies @p file into the working copy of the VCS and updates the existing file in the
    * VCS and the remote location of the VCS.
    * Use this method if the file had been imported previously.
    * @param model The Model representing the file in the VCS.
    * @param file The file to update
    * @param commitMessage The commit message to be used for the update.
    * @return Revision number of updated file, @c null if error occurred
    * @throws VcsException passes along the VcsException thrown by VcsManager
    **/
    @PreAuthorize("hasPermission(#model, write) or hasRole('ROLE_ADMIN')")
    String updateFile(Model model, File file, String commitMessage) throws VcsException {
        if (!isValid()) {
            throw new VcsException("Version Control System is not valid")
        }

        String revision = ''
        if (!commitMessage || commitMessage.isEmpty()) {
            revision = vcsManager.updateFile(file, model.vcsIdentifier)
        } else {
            revision = vcsManager.updateFile(file, model.vcsIdentifier, commitMessage)
        }
        return revision
    }
    // TODO: implement required methods
}
