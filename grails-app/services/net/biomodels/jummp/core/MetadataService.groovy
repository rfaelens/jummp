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
 */

package net.biomodels.jummp.core

import eu.ddmore.metadata.service.ValidationError
import eu.ddmore.metadata.service.ValidationException
import net.biomodels.jummp.annotation.CompositeValueContainer
import net.biomodels.jummp.annotation.PropertyContainer
import net.biomodels.jummp.annotation.SectionContainer
import net.biomodels.jummp.annotation.ValueContainer
import net.biomodels.jummp.core.adapters.DomainAdapter
import net.biomodels.jummp.annotationstore.*
import net.biomodels.jummp.core.util.JummpXmlUtils
import net.biomodels.jummp.model.Revision
import net.biomodels.jummp.model.Model
import net.biomodels.jummp.core.model.*
import net.biomodels.jummp.core.annotation.*
import eu.ddmore.metadata.service.MetadataWriterImpl
import net.biomodels.jummp.plugins.pharmml.AbstractPharmMlHandler
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.apache.jena.riot.RDFFormat
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.perf4j.aop.Profiled
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.acls.domain.BasePermission

/**
 * Service class that facilitates interaction with model metadata.
 *
 * The service currently provides means of querying the metadata related to a particular
 * revision of a model.
 *
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
class MetadataService {
    private static final Log log = LogFactory.getLog(this)
    private static final boolean IS_DEBUG_ENABLED = log.isDebugEnabled()
    static final String DEFAULT_PHARMML_NAMESPACE = "http://www.pharmml.org/2013/10/PharmMLMetadata"
    static final String DEFAULT_DDMORE_WAT_NAMESPACE =
            "http://www.ddmore.org/ontologies/webannotationtool#"
    static final String DEFAULT_PKPD_NAMESPACE =
            "http://www.ddmore.org/ontologies/ontology/pkpd-ontology#"
    /**
     * Dependency injection for Grails Application.
     */
    def grailsApplication
    /**
     * Dependency injection for Model Service.
     */
    def modelService
    /**
     * Dependency injection for Model Format Service.
     */
    def modelFileFormatService
    def pharmMlService

    def metadataValidator

    /**
     * Fetches any ResourceReferences defined for a given qualifier from a revision.
     *
     * Convenience method for fetching statements that match qualifier @p qName in revision
     * @p rev.
     *
     * @param rev the identifier of an existing revision.
     * @param qName the accession of the desired qualifier.
     * @return a list of ResourceReferences that represent the objects of the statements from
     * the revision denoted by @param rev.
     * @see net.biomodels.jummp.annotationstore.ResourceReference
     * @see net.biomodels.jummp.annotationstore.Statement
     */
    @Profiled(tag = "metadataService.findAllResourceReferencesForQualifier")
    List<ResourceReference> findAllResourceReferencesForQualifier(Long rev, String qName) {
        if (IS_DEBUG_ENABLED) {
            log.debug "Finding cross references with qualifier $qName for revision $rev."
        }
        Revision revision = Revision.load(rev)
        List<ResourceReference> result = ResourceReference.executeQuery('''
                select statement.object
                from Statement statement
                    join statement.qualifier qualifier
                where
                    statement.annotation.revision = ? and
                    qualifier.accession = ?''', [revision, qName])
        if (IS_DEBUG_ENABLED) {
            log.debug "Found ${result.size()} matching annotations."
        }
        return result
    }

    /**
     * Fetches all Statements containing a given qualifier from a revision.
     *
     * Convenience method for fetching statements that match qualifier @p qualifier
     * in revision @p $revisionId.
     *
     * @param revisionId the identifier of an existing revision.
     * @param qualifier the accession of the desired qualifier.
     * @return a list of matching Statements from the revision denoted by @param revisionId.
     * @see net.biomodels.jummp.annotationstore.Statement
     */
    @Profiled(tag = "metadataService.findAllStatementsForQualifier")
    List<Statement> findAllStatementsForQualifier(Long revisionId, String qualifier) {
        if (IS_DEBUG_ENABLED) {
            log.debug "Finding annotations with qualifier $qualifier for revision $revisionId."
        }
        Revision revision = Revision.load(revisionId)
        List<Statement> result = Statement.executeQuery('''select statement
                from Statement statement
                    join statement.qualifier qualifier
                where
                    statement.annotation.revision = :revision and
                    qualifier.accession = :qName''', [revision: revision, qName: qualifier])
        if (IS_DEBUG_ENABLED) {
            log.debug "Found ${result.size()} matching annotations."
        }
        return result
    }

    /**
     * Fetches all Statements containing a given RDF subject from a revision.
     *
     * Convenience method for fetching statements that match subject @p subject
     * in revision @p revisionId.
     *
     * @param revisionId the identifier of an existing revision.
     * @param subject the accession of the desired subject.
     * @return a list of matching Statements from the revision denoted by @param revisionId.
     * @see net.biomodels.jummp.annotationstore.Statement
     */
    @Profiled(tag = "metadataService.findAllStatementsForSubject")
    List<Statement> findAllStatementsForSubject(Long revisionId, String subject) {
        if (IS_DEBUG_ENABLED) {
            log.debug "Finding annotations with subject $subject for revision $revisionId."
        }
        Revision revision = Revision.load(revisionId)
        List<Statement> result = Statement.executeQuery('''
                from Statement statement
                where
                    statement.annotation.revision = :revision and
                    statement.subjectId = :subject''', [revision: revision, subject: subject])
        if (IS_DEBUG_ENABLED) {
            log.debug "Found ${result.size()} matching annotations."
        }
        return result
    }

    /**
     * Fetches any ResourceReferences defined for a given RDF subject from a revision.
     *
     * Convenience method for fetching statements that match subject @p subject in revision
     * @p revisionId.
     *
     * @param revision the identifier of an existing revision.
     * @param subject the accession of the desired subject.
     * @return a list of ResourceReferences that represent the objects of the statements from
     * the revision denoted by @param revisionId.
     * @see net.biomodels.jummp.annotationstore.ResourceReference
     */
    @Profiled(tag = "metadataService.findAllResourceReferencesForSubject")
    List<ResourceReference> findAllResourceReferencesForSubject(Long revisionId, String subject) {
        if (IS_DEBUG_ENABLED) {
            log.debug "Finding cross references with subject $subject for revision $revisionId."
        }
        Revision revision = Revision.load(revisionId)
        List<ResourceReference> result = ResourceReference.executeQuery('''select reference
                from Statement statement
                    join statement.object reference
                where
                    statement.annotation.revision = :revision and
                    statement.subjectId = :subject''', [revision: revision, subject: subject])
        if (IS_DEBUG_ENABLED) {
            log.debug "Found ${result.size()} matching annotations."
        }
        return result
    }

    @Profiled(tag = "metadataService.saveMetadata")
    boolean saveMetadata(String model, List<StatementTransportCommand> statements,
            boolean isUpdate = false) {
        Model theModel = Model.findBySubmissionIdOrPublicationId(model, model)
        Revision baseRevision = modelService.getLatestRevision(theModel, false)
        RevisionTransportCommand newRevision = DomainAdapter.getAdapter(baseRevision).toCommandObject()
        newRevision.comment = "Updated model annotations."

        try {
            def pharmMlMetadataWriter = createMetadataWriter(model,statements)
            List<RepositoryFileTransportCommand> files = newRevision.files
            String annoFilePath
            File annoFile
            RepositoryFileTransportCommand rf
            if (!isUpdate) {
                String fileBase = System.properties['java.io.tmpdir']
                String fileName = "${model}.rdf"
                annoFile = new File(fileBase, fileName)
                annoFilePath = annoFile.absolutePath
                rf = new RepositoryFileTransportCommand( path: annoFilePath, description:
                        "annotation file")
                files.add rf
            } else {
                rf = files.find {
                    it.path?.endsWith(".rdf")
                }
                annoFilePath = rf.path
                annoFile = new File(annoFilePath)
            }
            pharmMlMetadataWriter.writeRDFModel(annoFilePath, RDFFormat.RDFXML)

            boolean preProcessingOK = pharmMlService.doBeforeSavingAnnotations(annoFile, newRevision)
            if (!preProcessingOK) {
                log.error """\
Metadata ${pharmMlMetadataWriter.dump()} based on revision ${baseRevision.id} will not save cleanly."""
            }

            def result = modelService.addValidatedRevision(files, [], newRevision)
            if (!result) {
                log.error """\
Could not update revision ${baseRevision.id} with annotations ${pharmMlMetadataWriter.dump()}"""
            }//else{ result.save(flush: true) }
            return result != null
        } catch(Exception e) {
            log.error(e.message, e)
            return false
        }
    }

    private MetadataWriterImpl createMetadataWriter(String model, List<StatementTransportCommand> statements){
        def subject = "${grailsApplication.config.grails.serverURL}/model/${model}"
        def rdfTypeProperty = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"
        def pharmmlModelOntologyTerm = "http://www.pharmml.org/ontology/PHARMMLO_0000001"

        def pharmMlMetadataWriter = new MetadataWriterImpl()
        statements.each { StatementTransportCommand statement ->
            String predicate = statement.predicate.uri
            ResourceReferenceTransportCommand xref = statement.object
            String object = xref.uri ?: xref.name
            boolean isLiteralTriple = xref.uri ? false : true
            if (isLiteralTriple) {
                pharmMlMetadataWriter.generateLiteralTriple(subject, predicate, object)
            } else {
                pharmMlMetadataWriter.generateTriple(subject, predicate, object)
            }
        }
        pharmMlMetadataWriter.generateTriple(subject, rdfTypeProperty, pharmmlModelOntologyTerm)

        return pharmMlMetadataWriter
    }


    @Profiled(tag="metadataService.validateModelRevision")
    public void validateModelRevision(Revision revision, String model, List<StatementTransportCommand> statements){

        if (!revision) {
            throw new IllegalArgumentException("Revision may not be null")
        }

        def pharmMlMetadataWriter = createMetadataWriter(model, statements )

        StringBuffer validationReport = new StringBuffer();


        metadataValidator.validate(pharmMlMetadataWriter.model)

        for(ValidationError validationError: metadataValidator.validationHandler.getValidationList()){
            validationReport.append(Qualifier.findByUri(validationError.qualifier).accession)
            validationReport.append(validationError.getMessage())
            validationReport.append("<br>")

        }

        revision.validationReport = validationReport.toString();
        revision.validationLevel = metadataValidator.getValidationErrorStatus();

    }


    /**
     * Dumb cache of the annotation schema.
     *
     * Persists in the database the qualifiers and corresponding cross-references that make
     * up the fields and the values rendered by the annotation editor.
     */
    @Profiled(tag = "metadataService.persistAnnotationSchema")
    boolean persistAnnotationSchema(List<SectionContainer> sections) {
        def result = false
        def promise = Qualifier.async.task {
            sections.each { SectionContainer s ->
                def qualifiers = s.annotationProperties
                qualifiers.each { PropertyContainer p ->
                    String uri = p.uri
                    String name = p.value
                    def refreshedQualifier = saveOrUpdateQualifier(uri, name)
                    if (!refreshedQualifier) {
                        result = false
                    }
                    if (!p.values) {
                        return
                    }

                    List<ValueContainer> values = p.values
                    values.each { ValueContainer v ->
                        def xref = saveOrUpdateResourceReference(v)
                        if (!xref) {
                            result = false
                        }
                    }
                }
            }
            return result
        }
        promise.onComplete {
            result = true
        }
        promise.onError { Throwable t ->
            log.error("Failed to persist annotation schema in database: ${t.message}", t)
            result = false
        }
        promise.get()
        return result
    }

    /*
     * Saves a new qualifier in the database or updates the existing one.
     *
     * In the case of existing qualifiers, this triggers an update if the qualifier's label
     * does not match @p name.
     * @param uri the URI of the qualifier that should be saved.
     * @param name the label that the qualifier with URI @p uri should have
     * @return the updated Qualifier, or null if there was a validation error.
     */
    private Qualifier saveOrUpdateQualifier(String uri, String name) {
        Qualifier.withTransaction {
            def existingQualifier = Qualifier.findByUri(uri)
            if (!existingQualifier) {
                def q = new Qualifier(accession: name, uri: uri)
                if (uri.startsWith(DEFAULT_PHARMML_NAMESPACE) ||
                        uri.startsWith(DEFAULT_DDMORE_WAT_NAMESPACE)) {
                    q.namespace = DEFAULT_PHARMML_NAMESPACE
                    q.qualifierType = 'pharmML'
                }
                if (!q.save(flush: true)) {
                    log.error "Could not save new qualifier with uri $uri: ${q.errors.allErrors}"
                }
                existingQualifier = q
            } else {
                if (existingQualifier.accession != name) {
                    existingQualifier.accession = name
                    existingQualifier.save(flush: true)
                    if (existingQualifier.hasErrors()) {
                        log.error "Could not update qualifier $existingQualifier with new name $name"
                    }
                }
            }
            return existingQualifier
        }
    }

    /*
     * Turns a ValueContainer into the appropriate ResourceReference(s) in the database.
     *
     * If there is no corresponding ResourceReference in the database, a new one is created.
     * CompositeValueContainers are converted to nested ResourceReferences.
     *
     * Otherwise, if there is already a ResourceReference with the same URI as @p v, then this
     * method checks that the value of @p v matches the name of the ResourceReference and that
     * all members of @p parents are included in the set of parents of the corresponding
     * ResourceReference, triggering a database update if necessary.
     * @param v the ValueContainer that should be stored in the database
     * @param parents a collection of ResourceReferences that should be stored as parents
     * @return the saved ResourceReference or null if there was an error.
     */
    private ResourceReference saveOrUpdateResourceReference(ValueContainer v,
            List<ResourceReference> parents = []) {
        ResourceReference.withTransaction {
            String uri = v.uri
            String name = v.value
            def existing = ResourceReference.findByUri(uri)
            if (!existing) {
                existing = new ResourceReference(uri: uri, shortName: name, name: name)
                // work out the accession from the presence of the hash (#) or the last /
                int accessionDelim
                int hash = uri.indexOf('#')
                if (-1 != hash) {
                    accessionDelim = hash
                } else {
                    accessionDelim = uri.lastIndexOf('/')
                }
                String accession = uri.substring(accessionDelim + 1)
                existing.accession = accession
                if (uri.startsWith(DEFAULT_PKPD_NAMESPACE)) {
                    existing.datatype = 'pkpd'
                }
                if (parents) {
                    existing.parents.addAll(parents)
                }
                def savedXref = existing.save(flush: true)
                if (!savedXref) {
                    log.error """\
Unable to save xref with uri ${v.uri}: ${existing?.errors?.allErrors?.inspect()} - will not \
attempt to save any of its children"""
                    return savedXref
                }
                if (v instanceof CompositeValueContainer && v.children) {
                    v.children.each { ValueContainer child ->
                        saveOrUpdateResourceReference(child, parents << existing)
                    }
                }
                return existing
            } else {
                if (existing.name != name) {
                    existing.name = name
                    return existing.save(flush: true)
                }
                if (existing.parents != parents) {
                    existing.parents.addAll(parents)
                    return existing.save(flush: true)
                }
                return existing
            }
        }
    }
}
