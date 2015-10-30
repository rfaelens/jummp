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

import eu.ddmore.metadata.service.*
import net.biomodels.jummp.annotationstore.ResourceReference
import net.biomodels.jummp.annotationstore.Statement
import net.biomodels.jummp.core.annotation.ResourceReferenceCategory
import net.biomodels.jummp.core.annotation.ResourceReferenceTransportCommand
import net.biomodels.jummp.core.annotation.StatementCategory
import net.biomodels.jummp.core.annotation.StatementTransportCommand
import net.biomodels.jummp.core.model.RevisionTransportCommand
import org.apache.jena.riot.RDFFormat
import org.perf4j.aop.Profiled

/**
 * Simple delegate for metadataService.
 *
 * This service is the point of contact for any class outside of Jummp's core
 * that wishes to interact with metadataService.
 *
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
class MetadataDelegateService implements IMetadataService {
    /**
     * Dependency injection for the metadata service.
     */
    MetadataService metadataService

    /**
     * {@inheritDoc}
     */
    @Profiled(tag = "metadataDelegateService.findAllResourceReferencesForQualifier")
    List<ResourceReferenceTransportCommand> findAllResourceReferencesForQualifier(
            RevisionTransportCommand revision, String qualifier) {
        List<ResourceReference> references = metadataService.
                findAllResourceReferencesForQualifier(revision.id, qualifier)
        wrapResourceReferences(references)
    }

    /**
     * {@inheritDoc}
     */
    @Profiled(tag = "metadataDelegateService.findAllResourceReferencesForSubject")
    List<ResourceReferenceTransportCommand> findAllResourceReferencesForSubject(
            RevisionTransportCommand revision, String subject) {
        List<ResourceReference> references = metadataService.
                findAllResourceReferencesForSubject(revision.id, subject)
        wrapResourceReferences(references)
    }

    /**
     * {@inheritDoc}
     */
    @Profiled(tag = "metadataDelegateService.findAllStatementsForQualifier")
    List<StatementTransportCommand> findAllStatementsForQualifier(RevisionTransportCommand
            revision, String qualifier) {
        List<Statement> statements = metadataService.findAllStatementsForQualifier(
                revision.id, qualifier)
        wrapStatements(statements)
    }

    /**
     * {@inheritDoc}
     */
    @Profiled(tag = "metadataDelegateService.findAllStatementsForSubject")
    List<StatementTransportCommand> findAllStatementsForSubject(RevisionTransportCommand
            revision, String subject) {
        List<Statement> statements = metadataService.findAllStatementsForSubject(
                revision.id, subject)
        wrapStatements(statements)
    }

    @Profiled(tag = "metadataDelegateService.wrapResourceReferences")
    private List<ResourceReferenceTransportCommand> wrapResourceReferences(
            List<ResourceReference> references) {
        use(ResourceReferenceCategory) {
            return references.collect { ResourceReference r ->
                r.toCommandObject()
            }
        }
    }

    @Profiled(tag = "metadataDelegateService.wrapStatements")
    private List<StatementTransportCommand> wrapStatements(List<Statement> statements) {
        use(StatementCategory) {
            return statements.collect { Statement s ->
                s.toCommandObject()
            }
        }
    }

    boolean updateModelMetadata(String model, List<StatementTransportCommand> statements) {
        def metadataWriter = new MetadataWriterImpl()
        //TODO fix me
        String subject = "http://localhost:8080/jummp/$model"
        statements.each { StatementTransportCommand statement ->
            String predicate = statement.predicate.uri
            ResourceReferenceTransportCommand xref = statement.object
            String object = xref.uri ?: xref.name
            boolean isLiteralTriple = xref.uri ? false : true
            if (isLiteralTriple) {
                metadataWriter.generateLiteralTriple(subject, predicate, object)
            } else {
                metadataWriter.generateTriple(subject, predicate, object)
            }
            println metadataWriter.model.dump()
        }
        metadataWriter.writeRDFModel("/tmp/$model.rdf", RDFFormat.RDFXML)
        return true
    }
}
