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

package net.biomodels.jummp.plugins.pharmml

import grails.gsp.PageRenderer
import groovy.util.logging.Commons
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import net.biomodels.jummp.core.annotation.ElementAnnotationTransportCommand
import net.biomodels.jummp.core.annotation.QualifierTransportCommand
import net.biomodels.jummp.core.annotation.ResourceReferenceTransportCommand
import net.biomodels.jummp.core.annotation.StatementTransportCommand
import net.biomodels.jummp.core.model.RevisionTransportCommand
import org.perf4j.aop.Profiled

/**
 * Simple service for rendering annotations for PharmML models.
 *
 * The format of annotations is assumed to be the same for all models,
 * regardless of the version of PharmML in which they are encoded.
 *
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
@Commons
@CompileStatic
class PharmMlMetadataRenderingService {
    static final String DEVELOPMENT_CONTEXT_PROPERTY ='model-modelling-question'
    static final String DEVELOPMENT_CONTEXT = 'Context of model development'
    static final String NATURE_OF_RESEARCH_PROPERTY = 'model-research-stage'
    static final String NATURE_OF_RESEARCH = 'Nature of research'
    static final String THERAPEUTIC_AREA_PROPERTY = 'model-field-purpose'
    static final String THERAPEUTIC_AREA = 'Therapeutic/disease area'
    static final String TASK_IN_SCOPE_PROPERTY = 'model-tasks-in-scope'
    static final String TASK_IN_SCOPE = 'Modelling task in scope'
    static final Map<String, String> GENERIC_ANNOTATIONS = [
            (DEVELOPMENT_CONTEXT) : DEVELOPMENT_CONTEXT_PROPERTY,
            (NATURE_OF_RESEARCH) : NATURE_OF_RESEARCH_PROPERTY,
            (THERAPEUTIC_AREA) : THERAPEUTIC_AREA_PROPERTY,
            (TASK_IN_SCOPE) : TASK_IN_SCOPE_PROPERTY
    ]
    /* disable transactional behaviour */
    static transactional = false
    /* dependency injection for the page renderer */
    PageRenderer groovyPageRenderer

    @CompileDynamic
    @Profiled(tag = "pharmmlMetadataRenderingService.renderGenericAnnotations")
    void renderGenericAnnotations(RevisionTransportCommand revision, Writer out) {
        List<ElementAnnotationTransportCommand> annotations = revision.annotations
        List<StatementTransportCommand> statements = annotations.collect { it.statement }
        Map<String, List<ResourceReferenceTransportCommand>> anno = [:]
        GENERIC_ANNOTATIONS.each { String name, String property ->
            List<ResourceReferenceTransportCommand> objects =
                    findAllResourceReferencesForQualifier(statements, property)
            anno.put(name, objects)
        }
        groovyPageRenderer.renderTo(template: "/templates/common/metadata/annotations",
                model: [annotations: anno], out)
    }

    @Profiled(tag = "pharmmlMetadataRenderingService.findAllResourceReferencesForQualifier")
    private List findAllResourceReferencesForQualifier(List<StatementTransportCommand> stmts,
            String qName) {
        List result = []
        stmts.each { StatementTransportCommand s ->
            QualifierTransportCommand q = s.predicate
            if (qName == q.uri || qName == q.accession) {
                result.add s.object
            }
        }
        return result
    }
}
