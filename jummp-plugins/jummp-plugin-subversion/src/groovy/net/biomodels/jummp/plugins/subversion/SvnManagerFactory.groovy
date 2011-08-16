package net.biomodels.jummp.plugins.subversion

import org.codehaus.groovy.grails.commons.ConfigurationHolder
import net.biomodels.jummp.core.vcs.VcsException
import net.biomodels.jummp.core.vcs.VcsNotInitedException
import org.apache.commons.io.FileUtils
import org.apache.log4j.Logger

/**
 * @short Factory Class for SvnManager.
 *
 * The factory takes care of creating the SvnManager and all the required directories based on
 * the current configuration. If the configuration does not satisfy the factory's need it will
 * throw a VcsException.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class SvnManagerFactory {
    static transactional = true
    SvnManager svn
    Logger log = Logger.getLogger(SvnManagerFactory)
    /**
     * Dependency injection of servlet context
     */
    def servletContext

    SvnManager getInstance() throws Exception {
        if (svn) {
            return svn
        }
        ConfigObject config = ConfigurationHolder.config
        if (!config.jummp.plugins.subversion.localRepository) {
            log.debug("No checkout repository set - Subversion service not enabled")
            throw new VcsNotInitedException()
        }
        File localRepository = new File(config.jummp.plugins.subversion.localRepository)
        File workingDirectory
        if (config.jummp.vcs.workingDirectory) {
            workingDirectory = new File(config.jummp.vcs.workingDirectory)
        } else {
            // config option not set - use resource/workingDirectory
            workingDirectory = new File(servletContext.getRealPath("/resource/workingDir"))
        }
        File exchangeDirectory
        if (config.jummp.vcs.exchangeDirectory) {
            exchangeDirectory = new File(config.jummp.vcs.exchangeDirectory)
        } else {
            exchangeDirectory = new File(servletContext.getRealPath("/resource/exchangeDir"))
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
            throw new VcsNotInitedException()
        }
        return svn
    }
}
