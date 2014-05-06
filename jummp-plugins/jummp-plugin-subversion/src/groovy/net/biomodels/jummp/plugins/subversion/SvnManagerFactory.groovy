/**
* Copyright (C) 2010-2014 EMBL-European Bioinformatics Institute (EMBL-EBI),
* Deutsches Krebsforschungszentrum (DKFZ)
*
* This file is part of Jummp.
*
* Jummp is free software; you can redistribute it and/or modify it under the
* terms of the GNU Affero General Public License as published by the Free
* Software Foundation; either version 3 of the License, or (at your option) any
* later version.
*
* Jummp is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
* details.
*
* You should have received a copy of the GNU Affero General Public License along
* with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
*
* Additional permission under GNU Affero GPL version 3 section 7
*
* If you modify Jummp, or any covered work, by linking or combining it with
* Apache Commons, Log4j (or a modified version of that library), containing parts
* covered by the terms of Apache License v2.0, the licensors of this
* Program grant you additional permission to convey the resulting work.
* {Corresponding Source for a non-source form of such a combination shall
* include the source code for the parts of Apache Commons, Log4j used as well as
* that of the covered work.}
**/





package net.biomodels.jummp.plugins.subversion

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
    /**
     * Dependency injection of grailsApplication
     */
    def grailsApplication

    SvnManager getInstance() throws Exception {
        if (svn) {
            return svn
        }
        ConfigObject config = grailsApplication.config
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
