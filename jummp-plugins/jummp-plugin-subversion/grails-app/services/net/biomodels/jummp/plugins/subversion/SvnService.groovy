package net.biomodels.jummp.plugins.subversion

import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.springframework.beans.factory.InitializingBean
import net.biomodels.jummp.core.vcs.Vcs
import net.biomodels.jummp.core.vcs.VcsException
import net.biomodels.jummp.core.vcs.VcsNotInitedException
import net.biomodels.jummp.core.vcs.VcsManager

class SvnService implements InitializingBean, Vcs {
    static transactional = true
    SvnManager svn

    void afterPropertiesSet() {
        ConfigObject config = ConfigurationHolder.config
        if (!config.jummp.plugins.subversion.enabled) {
            log.debug("Subversion service not enabled")
            return
        }
        File localRepository = new File(config.jummp.plugins.subversion.localRepository)
        File workingDirectory
        if (config.jummp.vcs.workingDirectory) {
            workingDirectory = new File(config.jummp.vcs.workingDirectory)
        } else {
            // config option not set - use resource/workingDirectory
            workingDirectory = new File(ServletContextHolder.servletContext.getRealPath("/resource/workingDir"))
        }
        File exchangeDirectory
        if (config.jummp.vcs.exchangeDirectory) {
            exchangeDirectory = new File(config.jummp.vcs.exchangeDirectory)
        } else {
            exchangeDirectory = new File(ServletContextHolder.servletContext.getRealPath("/resource/exchangeDir"))
        }
        try {
            svn = new SvnManager(localRepository)
            svn.init(workingDirectory, exchangeDirectory)
        } catch (VcsException e) {
            log.error(e.getMessage())
            e.printStackTrace()
        }
    }

    VcsManager vcsManager() throws VcsNotInitedException {
        if (svn) {
            return svn
        } else {
            throw new VcsNotInitedException("No SvnManager is setup")
        }
    }
}
