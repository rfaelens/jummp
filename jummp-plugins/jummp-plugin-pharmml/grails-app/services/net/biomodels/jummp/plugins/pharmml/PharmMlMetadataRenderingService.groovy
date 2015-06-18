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
import groovy.transform.CompileStatic
import net.biomodels.jummp.core.model.RevisionTransportCommand

/**
 * Simple service for rendering annotations for PharmML models.
 *
 * The format of annotations is assumed to be the same for all models,
 * regardless of the version of PharmML in which they are encoded.
 *
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
@CompileStatic
@Commons
class PharmMlMetadataRenderingService {
    /* disable transactional behaviour */
    static transactional = false
    /* dependency injection for the page renderer */
    PageRenderer groovyPageRenderer

    void renderGenericAnnotations(RevisionTransportCommand revision, Writer out) {
        Map<String, Set<String>> anno = [:]
        anno['Modelling question'] =
                ['http://www.ddmore.org/ontologies/ontology/pkpd-ontology#pkpd_0006034',
                'http://www.ddmore.org/ontologies/ontology/pkpd-ontology#pkpd_0006036'] as Set
        anno['Field purpose'] =
                ['http://www.ddmore.org/ontologies/ontology/pkpd-ontology#pkpd_0001023'] as Set
        groovyPageRenderer.renderTo(template: "/templates/common/metadata/genericAnnotations",
                model: [annotations: anno], out)
    }
}
