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
import org.apache.commons.logging.LogFactory
import org.apache.commons.logging.Log
import org.apache.solr.client.solrj.SolrServer
import org.apache.solr.client.solrj.impl.HttpSolrServer
import org.apache.solr.client.solrj.response.SolrPingResponse
import org.perf4j.aop.Profiled

/**
 * @short Simple holder of the reference to the Solr server.
 *
 * This singleton-scoped bean creates and maintains a reference to the Solr server.
 * An EmbeddedSolrServer is used for testing, while a HttpSolrServer is used otherwise.
 */
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
     * Dependency injection of Grails Application.
     */
    def grailsApplication
    /**
     * Singleton instance of SolrServer.
     */
    SolrServer server

    @Profiled(tag="solrServerHolder.init")
    void init() {
        if (IS_DEBUG_ENABLED) {
            log.debug "Initialising solrServerHolder..."
        }
        final String url = grailsApplication.config.jummp.search.url
        if (!url) {
            throw new IllegalStateException("""\
URL of Solr server not found. Please check the setting jummp.server.url in the config file.""")
        }
        if (Environment.current == Environment.TEST) {
            //TODO
        } else {
            server = new HttpSolrServer(url)
        }
        if (IS_INFO_ENABLED) {
            log.info "Connected to Solr instance $url."
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

    @Profiled(tag="solrServerHolder.destroy")
    void destroy() {
        server = null
    }
}
