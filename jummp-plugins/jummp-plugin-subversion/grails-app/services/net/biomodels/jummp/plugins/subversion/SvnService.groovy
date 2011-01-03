package net.biomodels.jummp.plugins.subversion

import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.springframework.beans.factory.InitializingBean
import net.biomodels.jummp.core.vcs.Vcs
import net.biomodels.jummp.core.vcs.VcsException
import net.biomodels.jummp.core.vcs.VcsNotInitedException
import net.biomodels.jummp.core.vcs.VcsManager
import org.apache.commons.io.FileUtils

class SvnService implements InitializingBean, Vcs {
    static transactional = true
    @SuppressWarnings('GrailsStatelessService')
    SvnManager svn

    void afterPropertiesSet() {
        ConfigObject config = ConfigurationHolder.config
        if (!config.jummp.plugins.subversion.enabled) {
            log.debug("Subversion service not enabled")
            return
        }
        if (!config.jummp.plugins.subversion.localRepository) {
            log.debug("No checkout repository set - Subversion service not enabled")
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
            FileUtils.deleteDirectory(workingDirectory)
            workingDirectory.mkdirs()
            if (!exchangeDirectory.exists()) {
                exchangeDirectory.mkdirs()
            }
            svn = new SvnManager(localRepository)
            svn.init(workingDirectory, exchangeDirectory)
        } catch (VcsException e) {
            svn = null
            log.error(e.getMessage())
            e.printStackTrace()
        }
    }

    VcsManager vcsManager() throws VcsNotInitedException {
        if (svn) {
            return svn
        } else {
            throw new VcsNotInitedException()
        }
    }

    boolean isValid() {
        return (svn != null)
    }
}
