package net.biomodels.jummp.core

import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.context.ApplicationContext
import org.springframework.security.access.prepost.PreAuthorize
import net.biomodels.jummp.core.vcs.Vcs
import net.biomodels.jummp.core.vcs.VcsException
import net.biomodels.jummp.core.vcs.VcsManager
import net.biomodels.jummp.model.Model
import net.biomodels.jummp.model.Revision

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
        // config option is a map in case it is not defined and a String if it is defined
        // because of that we need to use a def and do an instance of test
        def pluginServiceName = ConfigurationHolder.config.jummp.vcs.pluginServiceName
        if (pluginServiceName instanceof String && !pluginServiceName.isEmpty()) {
            try {
                ApplicationContext ctx = (ApplicationContext)ApplicationHolder.getApplication().getMainContext()
                Vcs vcs = (Vcs)ctx.getBean(pluginServiceName)
                if (vcs.isValid()) {
                    vcsManager = vcs.vcsManager()
                } else {
                    log.error("Vcs service ${pluginServiceName} is not valid, disabling VcsService")
                }
            } catch(NoSuchBeanDefinitionException e) {
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
    * @return Revision number of updated file.
    * @throws VcsException passes along the VcsException thrown by VcsManager
    **/
    @PreAuthorize("hasPermission(#model, write) or hasRole('ROLE_ADMIN')")
    String updateFile(final Model model, final File file, final String commitMessage) throws VcsException {
        if (!isValid()) {
            throw new VcsException("Version Control System is not valid")
        }

        if (commitMessage == null || commitMessage.isEmpty()) {
            return vcsManager.updateFile(file, model.vcsIdentifier)
        } else {
            return vcsManager.updateFile(file, model.vcsIdentifier, commitMessage)
        }
    }
    /**
     * Imports a new Model file into the VCS.
     * Copies @p file into the working copy of the VCS and performs an initial import.
     * Use this method if the file has not been imported previously.
     * @param model The Model representing the new file in the VCS
     * @param file The file to import
     * @return Revision number of imported file.
     * @throws VcsException passes along the VcsException thrown by VcsManager
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    String importFile(final Model model, final File file) throws VcsException {
        if (!isValid()) {
            throw new VcsException("Version Control System is not valid")
        }

        return vcsManager.importFile(file, model.vcsIdentifier)
    }

    /**
     * Retrieves Model File from VCS.
     * @param revision The Revision for which the Model file should be retrieved
     * @return File Handler to the retrieved file in the exchange directory.
     * It's the responsibility of the caller to delete the file when it is not needed any more.
     * @throws VcsException passes along the VcsException thrown by VcsManager
     */
    @PreAuthorize("hasPermission(#revision, read) or hasRole('ROLE_ADMIN')")
    File retrieveFile(final Revision revision) throws VcsException {
        if (!isValid()) {
            throw new VcsException("Version Control System is not valid")
        }

        return vcsManager.retrieveFile(revision.model.vcsIdentifier, revision.vcsId)
    }
    // TODO: implement required methods
}
