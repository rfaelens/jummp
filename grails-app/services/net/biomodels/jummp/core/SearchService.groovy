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
import grails.async.Promises
import grails.plugins.springsecurity.Secured
import grails.util.Holders
import net.biomodels.jummp.core.events.LoggingEventType
import net.biomodels.jummp.core.events.PostLogging
import net.biomodels.jummp.core.model.ModelTransportCommand
import net.biomodels.jummp.core.model.RevisionTransportCommand
import net.biomodels.jummp.model.Model
import net.biomodels.jummp.model.Revision
import net.biomodels.jummp.search.SolrServerHolder
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.response.QueryResponse
import org.apache.solr.common.SolrDocumentList
import org.apache.solr.common.SolrInputDocument
import org.apache.solr.common.SolrInputField
import org.perf4j.aop.Profiled

/**
 * @short Singleton-scoped facade for interacting with a Solr instance.
 *
 * This service provides means of indexing and querying generic information about
 * models.
 *
 * @author Raza Ali, raza.ali@ebi.ac.uk
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 * @date   20141028
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
    def modelService = Holders.grailsApplication.mainContext.getBean("modelService")
    /**
     * Dependency injection of ModelDelegateService.
     */
    def modelDelegateService = Holders.grailsApplication.mainContext.getBean("modelDelegateService")
    /**
     * Dependency injection of SolrServerHolder
     */
    SolrServerHolder solrServerHolder

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
     * Adds the specified @param revision to the lucene index
     * @param revision The revision to be indexed
     **/
    @PostLogging(LoggingEventType.UPDATE)
    @Profiled(tag="searchService.updateIndex")
    void updateIndex(RevisionTransportCommand revision) {
        Promise p = Revision.async.task {
            if (IS_DEBUG_ENABLED) {
                log.debug "About to update index with revision ${revision.id}"
            }
            String name = revision.name ?: ""
            String description = revision.description ?: ""
            String content = modelDelegateService.getSearchIndexingContent(revision) ?: ""
            String submissionId = revision.model.submissionId
            String publicationId = revision.model.publicationId ?: ""
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
            doc.addField("versionNumber", revision.revisionNumber)
            doc.addField("submissionDate", revision.model.submissionDate)
            solrServerHolder.server.add(doc)
            solrServerHolder.server.commit()
            solrServerHolder.server.optimize()
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
        Promise p = Revision.async.task {
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
    * @return Set of ModelTransportCommand of relevant models available to the user.
    **/
    @PostLogging(LoggingEventType.RETRIEVAL)
    @Profiled(tag="searchService.searchModels")
    public Set<ModelTransportCommand> searchModels(String query) {
        SolrDocumentList results = search(query)

        Set<ModelTransportCommand> returnVals = new LinkedHashSet<ModelTransportCommand>()
        results.each {
            def existingModel = returnVals.find { prevs ->
                prevs.submissionId == it.get("submissionId")
            }
            if (!existingModel) {
                String perennialField = it.get("publicationId") ?: it.get("submissionId")
                Model returned = Model.findByPerennialIdentifier(perennialField)
                if (returned && !returned.deleted &&
                        modelService.getLatestRevision(returned, false)) {
                    returnVals.add(returned.toCommandObject())
                }
            }
        }
        return returnVals
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
        query.setQuery("{!edismax}*${q.encodeAsHTML()}*")
        String[] fields = [ "name" ,"description", "content", "submissionId", "publicationId",
                "modelFormat", "levelVersion", "submitter", "paperTitle", "paperAbstract"]
        query.setFields(fields)
        QueryResponse response = solrServerHolder.server.query(query)
        SolrDocumentList docs = response.getResults()
        return docs
    }
}
