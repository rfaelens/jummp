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
 **/

package net.biomodels.jummp.search

import grails.util.Environment
import grails.util.Metadata
import groovy.transform.CompileStatic
import org.apache.commons.io.FileUtils
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.apache.solr.client.solrj.SolrServer
import org.apache.solr.client.solrj.impl.HttpSolrServer
import org.apache.solr.client.solrj.response.SolrPingResponse
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.perf4j.aop.Profiled
import org.springframework.core.io.Resource

/**
 * @short Simple holder of the reference to the Solr server.
 *
 * This singleton-scoped bean creates and maintains a reference to the Solr server.
 */
@CompileStatic
class SolrServerHolder {
    /**
     * The class logger.
     */
    static final Log log = LogFactory.getLog(SolrServerHolder)
    /**
     * Flag indicating the logger's verbosity threshold.
     */
    static final boolean IS_DEBUG_ENABLED = log.isDebugEnabled()
    /**
     * Flag indicating the logger's verbosity threshold.
     */
    static final boolean IS_INFO_ENABLED = log.isInfoEnabled()
    /**
     * The application name.
     */
    static final String APP_NAME = ((String) Metadata.current.get('app.name')).toLowerCase()
    /**
     * The default name for the Solr core used by the application.
     */
    static final String DEFAULT_CORE_NAME = "${APP_NAME}_core"
    /**
     * The name for the Solr core used by the application during testing.
     */
    static final String TEST_CORE_NAME = "test_core"
    /**
     * The location of the application's configuration folder.
     */
    static final String GRAILS_CONF_LOCATION = "grails-app/conf"
    /**
     * The path, relative to the classpath, that contains the folder defining configuration
     * settings for a Solr core that is used by the application.
     */
    static final String SOLR_CONFIG_LOCATION = "solr"
    /**
     * Dependency injection of Grails Application.
     */
    GrailsApplication grailsApplication
    /**
     * Singleton instance of SolrServer.
     */
    SolrServer server

    @Profiled(tag="solrServerHolder.init")
    void init() {
        if (IS_DEBUG_ENABLED) {
            log.debug "Initialising solrServerHolder..."
        }
        final Map grailsConfig = grailsApplication.config.flatten()
        final String SOLR_URL = grailsConfig.get("jummp.search.url")
        if (!SOLR_URL) {
            throw new IllegalStateException("""\
URL of Solr server not found. Please check the setting jummp.search.url in the config file.""")
        }
        String coreName = Environment.current == Environment.TEST ? TEST_CORE_NAME :
                DEFAULT_CORE_NAME
        prepareSolrCoreSetup(SOLR_URL, coreName, grailsConfig)

        final String SOLR_CORE_URL = getSolrCoreUrl(SOLR_URL)
        server = new HttpSolrServer(SOLR_CORE_URL)
        if (IS_INFO_ENABLED) {
            log.info "Connected to Solr instance $SOLR_CORE_URL."
        }
        SolrPingResponse response = server.ping()
        if (IS_DEBUG_ENABLED) {
            log.debug "Solr instance response time: ${response.getQTime()}ms."
        }
        log.info "Solr server is $server"
        if (IS_DEBUG_ENABLED) {
            log.debug "... finished initialising solrServerHolder."
        }
    }

    private void prepareSolrCoreSetup(String baseUrl, String name, Map cfg) {
        final File SOLR_CORE_FOLDER = getSolrCoreFolder(cfg)
        if (SOLR_CORE_FOLDER.list().length == 0) {
            if (IS_DEBUG_ENABLED) {
                log.debug "Creating Solr core $name."
            }
            final File SOLR_CONF_FOLDER = getSolrConfigFolder()
            FileUtils.copyDirectory(SOLR_CONF_FOLDER, SOLR_CORE_FOLDER, false)
            //Cannot use Solr's CoreAdminHandler because it's in solr-core.
            String coresPage = "/admin/cores"
            Map paramsMap = ["action" : "CREATE",
                    "name" : name,
                    "instanceDir" : name,
                    "config" : "solrconfig.xml",
                    "schema" : "schema.xml",
                    "dataDir" : "data"
            ]
            String params = "?" + paramsMap.collect{k, v -> "$k=$v"}.join("&")
            String url = baseUrl + coresPage + params

            String response = new URL(url).getText("UTF-8")
            if (IS_INFO_ENABLED) {
                log.info "Response from Solr core creation request: $response"
            }
        } else {
            if (IS_DEBUG_ENABLED) {
                log.debug "Reusing existing Solr core $name."
            }
        }
    }

    private String getSolrCoreUrl(String solrUrl) {
        if (!solrUrl?.trim()) {
            throw new IllegalArgumentException("""\
Unable to work out the URL of the Solr core because the base Solr URL is undefined.""")
        }
        final String SOLR_CORE_URL
        if (Environment.current == Environment.TEST) {
            SOLR_CORE_URL = "$solrUrl/$TEST_CORE_NAME"
        } else {
            SOLR_CORE_URL = "$solrUrl/$DEFAULT_CORE_NAME"
        }
        return SOLR_CORE_URL
    }

    private void ensureSolrCoreFolderExists(File core) {
        if (core.exists() && core.canWrite()) {
            return
        }
        boolean solrCoreCreated = core.mkdirs()
        if (!solrCoreCreated) {
            throw new IllegalStateException("Cannot create folder $core to store search index.")
        }
        if (!core.canWrite()) {
            throw new IllegalStateException("Missing write permission on $core.")
        }
    }

    private File getSolrHomeFolder(Map appConfig) {
        final String SOLR_HOME
        final File SOLR_HOME_FOLDER
        if (Environment.current == Environment.TEST) {
            SOLR_HOME = "target/search"
            SOLR_HOME_FOLDER = new File(SOLR_HOME)
            if (SOLR_HOME_FOLDER.exists()) {
                FileUtils.deleteQuietly(SOLR_HOME_FOLDER)
            }
            SOLR_HOME_FOLDER.mkdirs()
        } else {
            SOLR_HOME = appConfig.get("jummp.search.folder") ?:
                    System.getenv("SOLR_HOME")
            if (!SOLR_HOME) {
                throw new IllegalStateException("""\
The location of your Solr installation was not found. Please either set the $SOLR_HOME \
environment variable, or specify the setting jummp.search.folder in the config file.""")
            }
            SOLR_HOME_FOLDER = new File(SOLR_HOME)
            // need to check the canonical version to resolve symlinks
            boolean isFolder = SOLR_HOME_FOLDER.getCanonicalFile().isDirectory()
            if (!isFolder) {
                throw new IllegalStateException("Location $SOLR_HOME_FOLDER must be a folder.")
            }
            if (!SOLR_HOME_FOLDER.exists()) {
                throw new IllegalStateException("""\
Location $SOLR_HOME_FOLDER does not exist. If you do not want to create it, you can either edit \
the setting jummp.search.folder in the config file, or change the SOLR_HOME environment \
variable. If the former setting is specified,the environment variable is ignored.""")
            }
        }
        return SOLR_HOME_FOLDER
    }

    private File getSolrCoreFolder(Map cfg) {
        File parent = getSolrHomeFolder(cfg)
        final File RESULT
        if (Environment.current == Environment.TEST) {
            RESULT = new File(parent, TEST_CORE_NAME)
        } else {
            RESULT = new File(parent, DEFAULT_CORE_NAME)
        }
        ensureSolrCoreFolderExists(RESULT)
        return RESULT
    }

    private File getSolrConfigFolder() {
        File result
        if (Environment.isWarDeployed()) {
            Resource resource = grailsApplication.mainContext.getResource(SOLR_CONFIG_LOCATION)
            result = resource.getFile()
        } else {
            result = new File("$GRAILS_CONF_LOCATION/$SOLR_CONFIG_LOCATION")
        }
        if (!result.exists()) {
            throw new IllegalArgumentException("Missing schema and configuration file for Solr core.")
        }
        if (IS_DEBUG_ENABLED) {
            log.debug "Solr core configuration templates are located in $result"
        }
        return result
    }

    @Profiled(tag="solrServerHolder.destroy")
    void destroy() {
        server = null
    }
}
