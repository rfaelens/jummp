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

package net.biomodels.jummp.core

import grails.async.Promise
import grails.plugins.springsecurity.Secured
import java.util.concurrent.atomic.AtomicReference
import net.biomodels.jummp.core.events.LoggingEventType
import net.biomodels.jummp.core.events.PostLogging
import net.biomodels.jummp.core.model.ModelTransportCommand
import net.biomodels.jummp.core.model.RevisionTransportCommand
import net.biomodels.jummp.model.Model
import net.biomodels.jummp.model.Revision
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.response.QueryResponse
import org.apache.solr.common.SolrDocumentList
import org.apache.solr.common.SolrInputDocument
import org.perf4j.aop.Profiled
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder

/**
 * @short Singleton-scoped facade for interacting with a Solr instance.
 *
 * This service provides means of indexing and querying generic information about
 * models.
 *
 * @author Raza Ali, raza.ali@ebi.ac.uk
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 * @date   20141113
 */
class SearchService {
    /**
     * The class logger.
     */
    static final Log log = LogFactory.getLog(SearchService)
    /**
     * Flag indicating the logger's verbosity threshold.
     */
    static final boolean IS_DEBUG_ENABLED = log.isDebugEnabled()
    /**
     * Flag indicating the logger's verbosity threshold.
     */
    static final boolean IS_INFO_ENABLED = log.isInfoEnabled()
    /**
     * Disable default transactional behaviour.
     */
    static transactional = false
    /**
     * Dependency injection of ModelService.
     */
    def modelService
    /**
     * Dependency injection of ModelDelegateService.
     */
    def modelDelegateService
    /**
     * Dependency injection of SpringSecurityService.
     */
    def springSecurityService
    /**
     * Dependency injection of SolrServerHolder
     */
    def  solrServerHolder

    /**
     * Clears the index. Handle with care.
     */
    @Secured(['ROLE_ADMIN'])
    @PostLogging(LoggingEventType.DELETION)
    @Profiled(tag="searchService.clearIndex")
    void clearIndex() {
        if (IS_DEBUG_ENABLED) {
            log.debug "Clearing the search index."
        }
        solrServerHolder.server.deleteByQuery("*:*")
        log.info "Cleared the search index."
    }

    /**
     * Adds a revision to the index
     *
     * Adds the specified @param revision to the index.
     * @param revision The revision to be indexed
     **/
    @PostLogging(LoggingEventType.UPDATE)
    @Profiled(tag="searchService.updateIndex")
    void updateIndex(RevisionTransportCommand revision) {
        Authentication auth = springSecurityService.authentication
        AtomicReference<Authentication> authRef = new AtomicReference<>(auth)
        Promise p = Revision.async.task {
            SecurityContextHolder.context.authentication = authRef.get()
            if (IS_DEBUG_ENABLED) {
                log.debug "About to update index with revision ${revision.id}"
            }
            String name = revision.name ?: ""
            String description = revision.description ?: ""
            String content = modelDelegateService.getSearchIndexingContent(revision) ?: ""
            String submissionId = revision.model.submissionId
            String publicationId = revision.model.publicationId ?: ""
            int versionNumber = revision.revisionNumber
            final String uniqueId = "${submissionId}.${versionNumber}"
            SolrInputDocument doc = new SolrInputDocument()
            /*
             * Indexed fields
             */
            doc.addField('submissionId', submissionId)
            doc.addField('publicationId', publicationId)
            doc.addField("name", name)
            doc.addField("description", description)
            doc.addField("modelFormat", revision.format.name)
            doc.addField("levelVersion", revision.format.formatVersion)
            doc.addField("submitter", revision.owner)
            doc.addField("content", content)
            doc.addField("paperTitle", revision.model.publication ?
                    revision.model.publication.title : "")
            doc.addField("paperAbstract", revision.model.publication ?
                    revision.model.publication.synopsis : "")
            /*
             * Stored fields. Hopefully will be used to display the search results one day
             * instead of going to the database for each model. When we find a solution to needing to
             * look in the database to figure out if the user has access to a model.
             */
            doc.addField("model_id", revision.model.id)
            doc.addField("versionNumber", versionNumber)
            doc.addField("submissionDate", revision.model.submissionDate)
            doc.addField("uniqueId", uniqueId)
            solrServerHolder.server.add(doc)
        }
        p.onComplete {
            if (IS_DEBUG_ENABLED) {
                log.debug "Finished indexing revision ${revision.id}"
            }
        }
        p.onError { Throwable t ->
            log.error("Could not index revision ${revision.id} due to ${t.message}", t)
        }
    }

    /**
     * Clears the existing index and then regenerates it.
     *
     * This method requires ROLE_ADMIN permissions.
     **/
    @Secured(['ROLE_ADMIN'])
    @PostLogging(LoggingEventType.CREATION)
    @Profiled(tag="searchService.regenerateIndices")
    void regenerateIndices() {
        clearIndex()
        List<RevisionTransportCommand> revisions = Revision.list(fetch: [model: "eager"]).collect { r ->
            r.toCommandObject()
        }
        if (IS_DEBUG_ENABLED) {
            log.debug "Indexing ${revisions.size()} revisions."
        }
        Authentication auth = springSecurityService.authentication
        AtomicReference<Authentication> authRef = new AtomicReference<>(auth)
        Promise p = Revision.async.task {
            SecurityContextHolder.context.authentication = authRef.get()
            revisions.each {
                updateIndex(it)
            }
        }
        p.onComplete {
            if (IS_INFO_ENABLED) {
                log.info "Finished regenerating the index."
            }
        }
        p.onError { Throwable e ->
            log.error("Error regenerating the index: ${e.message}", e)
        }
    }

    /**
    * Returns search results for query restricted Models the user has access to.
    *
    * Executes the @p query, restricting results to Models the current user has access to.
    * @param query freetext search on models
    * @return Collection of ModelTransportCommand of relevant models available to the user.
    **/
    @PostLogging(LoggingEventType.RETRIEVAL)
    @Profiled(tag="searchService.searchModels")
    public Collection<ModelTransportCommand> searchModels(String query) {
        long start = System.currentTimeMillis();
    	SolrDocumentList results = search(query)
    	System.out.println("Solr returned in "+(System.currentTimeMillis() - start));
        final int COUNT = results.size()
        Map<String, ModelTransportCommand> returnVals = new LinkedHashMap<>(COUNT + 1, 1.0f)
        results.each {
        	start = System.currentTimeMillis();
        	final String thisSubmissionId = it.get("submissionId")
            if (!returnVals.containsKey(thisSubmissionId)) {
                String perennialField = it.get("publicationId") ?: thisSubmissionId
                Model returned = Model.findByPerennialIdentifier(perennialField)
                if (returned && !returned.deleted &&
                        modelService.getLatestRevision(returned, false)) {
                    returnVals.put(thisSubmissionId, returned.toCommandObject())
                }
            }
            System.out.println("Processing took "+(System.currentTimeMillis() - start));
        }
        return returnVals.values()
    }

    /**
     * Internal method to execute a query.
     *
     * Queries Solr and returns the results.
     *
     * @param q The query to be executed
     * @return A list of documents corresponding to revisions that match @param q.
     **/
    @PostLogging(LoggingEventType.RETRIEVAL)
    @Profiled(tag="searchService.search")
    private SolrDocumentList search(String q) {
        SolrQuery query = new SolrQuery()
        /*TODO optimise this*/
        query.setQuery("*${q}*")
        QueryResponse response = solrServerHolder.server.query(query)
        SolrDocumentList docs = response.getResults()
        return docs
    }
}
