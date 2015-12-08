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
import net.biomodels.jummp.core.model.ValidationState
import org.apache.jena.riot.RDFFormat
import eu.ddmore.metadata.service.*

@Secured(["isAuthenticated()"])
class AnnotationController {

    def metadataInputSource
    def modelDelegateService
    def metadataDelegateService

    def show() {
        if (!params.id) {
            forward controller: "errors", action: "error404"
            return
        }

        def revision = modelDelegateService.getRevisionFromParams(params.id, params.revisionId)
        def modelId = (revision.model.publicationId) ?: (revision.model.submissionId)

        boolean canUpdate = modelDelegateService.canAddRevision modelId
        if (!canUpdate) {
            forward controller: 'errors', action: 'error403'
            return
        }

        def annotationSections = metadataInputSource.buildObjectModel()
        metadataDelegateService.persistAnnotationSchema annotationSections
        def objectModel = annotationSections as JSON
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

    def edit() {
        if (!params.id) {
            forward controller: "errors", action: "error404"
            return
        }

        def revision = modelDelegateService.getRevisionFromParams(params.id, params.revisionId)
        def modelId = (revision.model.publicationId) ?: (revision.model.submissionId)

        boolean canUpdate = modelDelegateService.canAddRevision modelId
        if (!canUpdate) {
            forward controller: 'errors', action: 'error403'
            return
        }

        def annotations = revision.annotations
        if (annotations?.size() < 1) {
            forward action: "show", params: [id: params.id, revisionId: params.revisionId]
        }
        def annotationSections = metadataInputSource.buildObjectModel()
        def objectModel = annotationSections as JSON

        def annoMap = [:]
        def jsonAnnoList = []
        annotations.each { ElementAnnotationTransportCommand anno ->
            def stmt = anno.statement
            def pred = stmt.predicate
            String predUri = pred.uri
            def obj = stmt.object
            if (annoMap.containsKey(predUri)) {
                annoMap[predUri] << obj
            } else {
                annoMap[predUri] = [obj]
            }
        }

        annoMap.each { String key, List<ResourceReferenceTransportCommand> objects ->
            def thisPred = [:]
            thisPred['predicate'] = key
            def allValues = []
            objects.each { ResourceReferenceTransportCommand o ->
                def value = [:]
                value['object'] = o.uri ?: o.name
                allValues << value
            }
            thisPred['object'] = allValues
            jsonAnnoList << thisPred
        }

        def existingAnnotations = [
            "subjects": [
                "theSubject": [ "predicates" : jsonAnnoList]
            ]
        ] as JSON
        def theModel = [
            objectModel: objectModel,
            existingAnnotations: existingAnnotations,
            annoPropsMap: annoMap as JSON,
            revision: revision,
            modelId: modelId
        ]
        render model: theModel, view: 'edit'
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

        String modelId = params.revision
        List<StatementTransportCommand> stmts = createStatementList()

        boolean result = metadataDelegateService.saveMetadata(modelId, stmts)
        if (result) {
            render([status: '200', message: "Information successfully saved."] as JSON)
        } else {
            render([status: '500', message: "Unable to save the information you provided."] as JSON)
        }
    }

    def validate(){
        if (!params.revision) {
            def response = [
                status: '400',
                message: 'The request to validate model properties lacked the revision parameter.'
            ]
            render(response as JSON)
            return
        }

        List<StatementTransportCommand> stmts = createStatementList();

        if(stmts.isEmpty())
            render([status: '400', message: 'Annotation fields are empty. Please annotate the model before validating.'] as JSON)

        RevisionTransportCommand rev = null
        try {
            rev = modelDelegateService.getRevision(params.revision)
            metadataDelegateService.validateModelRevision(rev, params.revision, stmts)
        }catch(ValidationException e){
            render([status: '400', message: 'Annotations could not be checked.' , errorReport:e.getMessage()] as JSON)
        }
        rev = modelDelegateService.getRevision(params.revision) // force refresh from db
        if(rev.validationLevel.equals(ValidationState.APPROVED))
            render([status: '200', message: rev.getValidationLevelMessage()] as JSON)
        else if(rev.validationLevel.equals(ValidationState.CONDITIONALLY_APPROVED))
            render([status: '400', message: rev.getValidationLevelMessage(), errorReport:rev.validationReport] as JSON)
        else
            render ([status: '500', message: "Unable to validate the annotations you provided."] as JSON)

    }

    def update() {
        if (!params.revision) {
            def response = [
                status: '400',
                message: 'The request to save model annotations lacked the revision parameter.'
            ]
            render(response as JSON)
            return
        }
        List<StatementTransportCommand> stmts = createStatementList()
        String modelId = params.revision
        boolean result = metadataDelegateService.updateMetadata(modelId, stmts)
        if (result) {
            render([status: '200', message: "Annotations successfully updated."] as JSON)
        } else {
            render([status: '500', message: "Unable to save the annotations you provided."] as JSON)
        }
    }

    private List<StatementTransportCommand> createStatementList(){
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
        return stmts
    }
}

