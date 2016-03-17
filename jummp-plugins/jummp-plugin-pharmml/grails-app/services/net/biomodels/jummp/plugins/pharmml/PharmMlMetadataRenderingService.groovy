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
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import net.biomodels.jummp.core.annotation.ElementAnnotationTransportCommand
import net.biomodels.jummp.core.annotation.QualifierTransportCommand
import net.biomodels.jummp.core.annotation.ResourceReferenceTransportCommand
import net.biomodels.jummp.core.annotation.StatementTransportCommand
import net.biomodels.jummp.core.model.RevisionTransportCommand
import net.biomodels.jummp.core.IMetadataService
import org.perf4j.aop.Profiled

/**
 * Simple service for rendering annotations for PharmML models.
 *
 * The format of annotations is assumed to be the same for all models,
 * regardless of the version of PharmML in which they are encoded.
 *
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
@CompileStatic
class PharmMlMetadataRenderingService {
    /* disable transactional behaviour */
    static transactional = false
    /* dependency injection for the page renderer */
    PageRenderer groovyPageRenderer
    /* dependency injection for the metadata delegate service */
    IMetadataService metadataDelegateService
    def grailsApplication
    /* the namespaces for which only the resource reference name is displayed, not its URL */
    List<String> ignoredNamespaces = new ArrayList<String>()

    @CompileDynamic
    @Profiled(tag = "pharmmlMetadataRenderingService.renderGenericAnnotations")
    void renderGenericAnnotations(RevisionTransportCommand revision, Writer out) {
        Map<String, List<ResourceReferenceTransportCommand>> anno = [:]
        ignoredNamespaces = metadataDelegateService.getMetadataNamespaces()
        // this will need to change when we're annotating several model elements.
        revision.annotations*.statement.each { StatementTransportCommand s ->
            String p = s.predicate.accession
            ResourceReferenceTransportCommand o = s.object
            if (o.uri) {
                boolean shouldCreateHyperlink = shouldCreateResourceHyperlink(o.uri)
                if (!shouldCreateHyperlink) {
                    o.uri = null
                }
            }
            if (anno.containsKey(p)) {
                anno[p] << o
            } else {
                anno.put(p, [o])
            }
        }
        groovyPageRenderer.renderTo(template: "/templates/common/metadata/annotations",
            model: [annotations: anno], out)
    }

    private boolean shouldCreateResourceHyperlink(String uri) {
        // don't create a hyperlink if we find one of these namespaces
        null == ignoredNamespaces.find { String ns -> uri.startsWith(ns) }
    }
}
