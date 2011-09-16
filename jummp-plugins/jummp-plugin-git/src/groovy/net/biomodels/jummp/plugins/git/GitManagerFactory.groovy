package net.biomodels.jummp.plugins.git

import net.biomodels.jummp.core.vcs.VcsException
import net.biomodels.jummp.core.vcs.VcsNotInitedException
import org.apache.log4j.Logger

/**
 * @short Factory Class for GitManager.
 *
 * The factory takes care of creating the GitManager and all the required directories based on
 * the current configuration. If the configuration does not satisfy the factory's need it will
 * throw a VcsException.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class GitManagerFactory {
    static transactional = true
    GitManager git
    Logger log = Logger.getLogger(GitManagerFactory)
    /**
     * Dependency Injection of Servlet Context
     */
    def servletContext
    /**
     * Dependency Injection of grailsApplication
     */
    def grailsApplication

    GitManager getInstance() throws Exception {
        if (git) {
            return git
        }
        ConfigObject config = grailsApplication.config
        File workingDirectory
        if (config.jummp.vcs.workingDirectory instanceof String && !config.jummp.vcs.workingDirectory.isEmpty()) {
            workingDirectory = new File(config.jummp.vcs.workingDirectory)
        } else {
            log.error("No working directory set, cannot enable git")
            throw new VcsNotInitedException()
        }
        File exchangeDirectory
        if (config.jummp.vcs.exchangeDirectory instanceof String && !config.jummp.vcs.exchangeDirectory.isEmpty()) {
            exchangeDirectory = new File(config.jummp.vcs.exchangeDirectory)
        } else {
            exchangeDirectory = new File(servletContext.getRealPath("/resource/exchangeDir"))
        }
        try {
            if (!exchangeDirectory.exists()) {
                exchangeDirectory.mkdirs()
            }
            git = new GitManager()
            git.init(workingDirectory, exchangeDirectory)
        } catch (VcsException e) {
            git = null
            log.error(e.getMessage())
            e.printStackTrace()
            throw new VcsNotInitedException()
        }
        return git
    }
}
