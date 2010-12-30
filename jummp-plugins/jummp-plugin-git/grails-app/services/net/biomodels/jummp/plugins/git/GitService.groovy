package net.biomodels.jummp.plugins.git

import net.biomodels.jummp.core.vcs.Vcs
import org.springframework.beans.factory.InitializingBean
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.codehaus.groovy.grails.web.context.ServletContextHolder
import net.biomodels.jummp.core.vcs.VcsException
import net.biomodels.jummp.core.vcs.VcsManager
import net.biomodels.jummp.core.vcs.VcsNotInitedException

class GitService implements InitializingBean, Vcs {
    static transactional = true
    GitManager git

    void afterPropertiesSet() {
        ConfigObject config = ConfigurationHolder.config
        if (!config.jummp.plugins.git.enabled) {
            log.debug("Git service not enabled")
            return
        }
        File workingDirectory
        if (config.jummp.vcs.workingDirectory) {
            workingDirectory = new File(config.jummp.vcs.workingDirectory)
        } else {
            log.error("No working directory set, cannot enable git")
            return
        }
        File exchangeDirectory
        if (config.jummp.vcs.exchangeDirectory) {
            exchangeDirectory = new File(config.jummp.vcs.exchangeDirectory)
        } else {
            exchangeDirectory = new File(ServletContextHolder.servletContext.getRealPath("/resource/exchangeDir"))
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
        }
    }

    VcsManager vcsManager() throws VcsNotInitedException {
        if (git) {
            return git
        } else {
            throw new VcsNotInitedException()
        }
    }

    boolean isValid() {
        return (git != null)
    }
}
