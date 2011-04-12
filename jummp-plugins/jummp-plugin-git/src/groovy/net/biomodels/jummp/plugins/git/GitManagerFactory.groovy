package net.biomodels.jummp.plugins.git

import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.codehaus.groovy.grails.web.context.ServletContextHolder
import net.biomodels.jummp.core.vcs.VcsException
import net.biomodels.jummp.core.vcs.VcsNotInitedException

class GitManagerFactory {
    static transactional = true
    @SuppressWarnings('GrailsStatelessService')
    GitManager git

    GitManager getInstance() throws Exception {
        if (git) {
            return git
        }
        ConfigObject config = ConfigurationHolder.config
        File workingDirectory
        if (config.jummp.vcs.workingDirectory) {
            workingDirectory = new File(config.jummp.vcs.workingDirectory)
        } else {
            log.error("No working directory set, cannot enable git")
            throw new VcsNotInitedException()
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
            throw new VcsNotInitedException()
        }
        return git
    }
}
