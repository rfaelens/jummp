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
import grails.util.Environment
import grails.util.Holders
import net.biomodels.jummp.core.events.LoggingEventType
import net.biomodels.jummp.core.events.PostLogging
import net.biomodels.jummp.core.model.ModelTransportCommand
import net.biomodels.jummp.core.model.RevisionTransportCommand
import net.biomodels.jummp.model.Model
import net.biomodels.jummp.search.StemmingAnalyzer
import org.apache.commons.logging.LogFactory
import org.apache.commons.logging.Log
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.queryParser.MultiFieldQueryParser
import org.apache.lucene.queryParser.QueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.TopDocs
import org.apache.lucene.store.Directory
import org.apache.lucene.store.FSDirectory
import org.perf4j.aop.Profiled

/**
 * @short Singleton-scoped facade for interacting with a Lucene instance.
 *
 * This service provides means of indexing and querying generic information about
 * models.
 *
 * @author Raza Ali, raza.ali@ebi.ac.uk
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 * @date   20141023
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
     * The location where the index is stored.
     */
    File location
    /**
     * The Lucene Directory used to contain the index.
     */
    Directory fsDirectory

    /**
     * Creates/opens a Lucene index based on the config properties (unless test,
     * otherwise a default location is used, to avoid corrupting the index).
     */
    @Profiled(tag="searchService.initialize")
    void initialize() {
        // Can't use dependency injection yet
        def grailsApplication = Holders.grailsApplication
        String path = grailsApplication.config.jummp.search.index
        if (Environment.current == Environment.TEST) {
            path = "target/search/index"
            File deleteMe = new File(path)
            deleteMe.deleteDir()
        }
        location = new File(path)
        location.mkdirs()
        //Create instance of Directory where index files will be stored
        fsDirectory = FSDirectory.getDirectory(location)
    }

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
        location.deleteDir()
        location.mkdirs()
        fsDirectory = FSDirectory.getDirectory(location)
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
        Promise p = Promises.task {
            Analyzer analyser = new StemmingAnalyzer()
            IndexWriter indexWriter = new IndexWriter(fsDirectory, analyser)
            indexWriter.setMaxFieldLength(25000)

            if (IS_DEBUG_ENABLED) {
                log.debug "About to update index with ${revision.properties}"
            }
            String name = revision.name ?: ""
            String description = revision.description ?: ""
            String content = modelDelegateService.getSearchIndexingContent(revision) ?: ""
            String submissionId = revision.model.submissionId
            String publicationId = revision.model.publicationId ?: ""
            Document doc = new Document()

            /*
             * Indexed fields
             */
            Field submissionIdField = new Field('submissionId', submissionId, Field.Store.YES,
                    Field.Index.ANALYZED)
            Field publicationIdField = new Field('publicationId', publicationId, Field.Store.YES,
                        Field.Index.ANALYZED)
            Field nameField = new Field("name", name, Field.Store.YES, Field.Index.ANALYZED)
            Field descriptionField = new Field("description", description, Field.Store.NO,
                        Field.Index.ANALYZED)
            Field formatField = new Field("modelFormat", revision.format.name, Field.Store.YES,
                        Field.Index.ANALYZED)
            Field levelVersionField = new Field("levelVersion", revision.format.formatVersion,
                        Field.Store.NO, Field.Index.ANALYZED)
            Field submitterField = new Field("submitter", revision.owner, Field.Store.YES,
                        Field.Index.ANALYZED)
            Field contentField = new Field("content", content, Field.Store.NO, Field.Index.ANALYZED)
            Field paperTitleField = new Field("paperTitle", revision.model.publication ?
                        revision.model.publication.title : "", Field.Store.NO, Field.Index.ANALYZED)
            Field paperAbstractField = new Field("paperAbstract", revision.model.publication ?
                        revision.model.publication.synopsis : "", Field.Store.NO, Field.Index.ANALYZED)

            doc.add(submissionIdField)
            doc.add(publicationIdField)
            doc.add(nameField)
            doc.add(descriptionField)
            doc.add(formatField)
            doc.add(levelVersionField)
            doc.add(submitterField)
            doc.add(contentField)
            doc.add(paperTitleField)
            doc.add(paperAbstractField)

            /*
             * Stored fields. Hopefully will be used to display the search results one day
             * instead of going to the database for each model. When we find a solution to needing to
             * look in the database to figure out if the user has access to a model.
             */
            Field idField = new Field("model_id", "${revision.model.id}", Field.Store.YES,
                        Field.Index.NO)
            Field versionField = new Field("versionNumber", "${revision.revisionNumber}",
                        Field.Store.YES, Field.Index.NO)
            Field submittedField = new Field("submissionDate", "${revision.model.submissionDate}",
                        Field.Store.YES, Field.Index.NO)
            doc.add(idField)
            doc.add(versionField)
            doc.add(submittedField)
            indexWriter.addDocument(doc)
            indexWriter.optimize()
            indexWriter.close()
        }
        p.onComplete {
            if (IS_DEBUG_ENABLED) {
                log.debug "Finished indexing ${revision.model.submissionId}"
            }
        }
        p.onError { Throwable t ->
            log.error("Could not index revision ${revision.properties} due to ${t.message}", t)
        }
    }

    /**
     * Returns search results for query restricted Models the user has access to.
     *
     * Executes the @p query, restricting results to Models the current user has access to, 
     * @param query freetext search on models
     * @return List of Models
     **/
    @Secured(['ROLE_ADMIN'])
    @PostLogging(LoggingEventType.CREATION)
    @Profiled(tag="searchService.regenerateIndices")
    void regenerateIndices() {
        Promise p = Promises.task {
            clearIndex()
            int offset = 0
            int count = 10
            while (true) {
                List<Model> models = modelService.getAllModels(offset, count)
                models.each {
                    updateIndex(modelService.getLatestRevision(it, false).toCommandObject())
                }
                if (models.size() < count) {
                    break
                }
                offset += count
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
    * @return List of ModelTransportCommand of relevant models available to the user.
    **/
    @PostLogging(LoggingEventType.RETRIEVAL)
    @Profiled(tag="searchService.searchModels")
    public Set<ModelTransportCommand> searchModels(String query) {
        String[] fields = ["submissionId", "publicationId", "name", "description", "content",
                    "modelFormat", "levelVersion", "submitter", "paperTitle", "paperAbstract"]
        Set<Document> results = performSearch(fields, query)

        Set<ModelTransportCommand> returnVals = new HashSet<ModelTransportCommand>()
        results.each {
            def existingModel = returnVals.find { prevs ->
                prevs.submissionId == it.get("submissionId")
            }
            if (!existingModel) {
                String perennialField= it.get("publicationId")
                if (perennialField.isEmpty()) {
                    perennialField= it.get("submissionId")
                }
                Model returned = Model.findByPerennialIdentifier(perennialField)
                if (returned && !returned.deleted && modelService.getLatestRevision(returned, false)) {
                    returnVals.add(returned.toCommandObject())
                }
            }
        }
        return returnVals
    }

    /**
     * Internal method to executes a query.
     *
     * Gets the searchermanager if necessary from the indexwriter,
     * executes the supplied @p query,
     * filters out duplicates (as we index revisions and return models),
     *
     * @param query The query to be executed
     * @return A set of documents corresponding to search results
     **/
    @PostLogging(LoggingEventType.RETRIEVAL)
    @Profiled(tag="searchService.search")
    private Set<Document> search(Query q) {
        Set<Document> docs = new HashSet<Document>()
        IndexSearcher indexSearcher = new IndexSearcher(fsDirectory)
        try {
            TopDocs topDocs = indexSearcher.search(q, 1000) //make less arbitrary
            for (int i = 0; i < topDocs.totalHits; i++) {
                docs.add(indexSearcher.doc(topDocs.scoreDocs[i].doc))
            }
        } finally {
            indexSearcher.close()
        }
        return docs
    }

    /**
     * Executes a search on the specified fields.
     *
     * Performs search on the @p query, using the supplied @p fields
     *
     * @param query The query to be executed
     * @param fields The ids of the fields in the documents
     * @return A set of documents corresponding to search results
     **/
    @PostLogging(LoggingEventType.RETRIEVAL)
    @Profiled(tag="searchService.performSearchOnMultipleFields")
    Set<Document> performSearch(String[] fields, String query) {
        QueryParser queryParser = new MultiFieldQueryParser(fields,
                new StemmingAnalyzer())
            return search(queryParser.parse(query))
    }

    /**
     * Executes a search on a single field
     *
     * Performs search on the @p query, using the supplied @p field
     *
     * @param query The query to be executed
     * @param field The ids of the field in the documents
     * @return A set of documents corresponding to search results
     **/
    @PostLogging(LoggingEventType.RETRIEVAL)
    @Profiled(tag="searchService.performSearchOnSingleField")
    Set<Document> performSearch(String field, String query) {
        QueryParser queryParser = new QueryParser(new StemmingAnalyzer())
        return search(queryParser.parse(query))
    }
}
