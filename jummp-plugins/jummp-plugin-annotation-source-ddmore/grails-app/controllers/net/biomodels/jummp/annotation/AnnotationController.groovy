/**
 * Copyright (C) 2010-2015 EMBL-European Bioinformatics Institute (EMBL-EBI),
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

package net.biomodels.jummp.annotation

import grails.converters.JSON
import grails.plugins.springsecurity.Secured
import groovy.json.JsonSlurper
import net.biomodels.jummp.core.model.RevisionTransportCommand
import net.biomodels.jummp.core.annotation.*
import org.apache.jena.riot.RDFFormat
import eu.ddmore.metadata.service.*

class AnnotationController {

    def metadataInputSource
    def modelDelegateService
    def metadataDelegateService

    @Secured(["isAuthenticated()"])
    def show() {
        if (!params.id) {
            forward controller: "errors", action: "error404"
            return
        }

        def revision = modelDelegateService.getRevisionFromParams(params.id, params.revisionId)
        def modelId = (revision.model.publicationId) ?: (revision.model.submissionId)

        def objectModel = metadataInputSource.buildObjectModel() as JSON
        def existingAnnotations = [
            "subjects": [
                "theSubject": [:]
            ]
        ] as JSON
        def theModel = [
            objectModel: objectModel,
            existingAnnotations: existingAnnotations,
            revision: revision,
            modelId: modelId
        ]
        render model: theModel, view: 'show'
    }

    def save() {
        if (!params.revision) {
            def response = [
                status: '400',
                message: 'The request to save model properties lacked the revision parameter.'
            ]
            render(response as JSON)
            return
        }
        def ap = params.annotations
        def anno = new JSON().parse(ap)
        def theSubject = anno.subjects.theSubject

        List<StatementTransportCommand> stmts = []
        String modelId = params.revision
        theSubject.predicates.each {
            String p = it.predicate
            def predicate = new QualifierTransportCommand(uri: p)

            def objects = it.object.object
            if (!(objects instanceof List)) {
                objects = [objects]
            }
            objects.each { String o ->
                def object
                if (o.startsWith("http:")) {
                    object = new ResourceReferenceTransportCommand(uri: o)
                } else {
                    object = new ResourceReferenceTransportCommand(name: o)
                }
                def stmt = new StatementTransportCommand(subject: modelId, predicate: predicate,
                        object: object)
                stmts.add(stmt)
            }
        }

        boolean result = metadataDelegateService.updateModelMetadata(modelId, stmts)
        if (result) {
            render([status: '200', message: "Information successfully saved."] as JSON)
        } else {
            render([status: '500', message: "Unable to save the information you provided."] as JSON)
        }
    }
}

