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

import net.biomodels.jummp.core.annotation.ResourceReferenceCategory
import net.biomodels.jummp.core.annotation.ResourceReferenceTransportCommand
import net.biomodels.jummp.annotationstore.ResourceReference
import net.biomodels.jummp.core.model.RevisionTransportCommand
import org.perf4j.aop.Profiled

/**
 * Simple delegate for metadataService.
 *
 * This service is the point of contact for any class outside of Jummp's core
 * that wishes to interact with metadataService.
 *
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
class MetadataDelegateService {
    /**
     * Dependency injection for the metadata service.
     */
    MetadataService metadataService

    @Profiled(tag = "metadataDelegateService.findAllResourceReferencesForQualifier")
    List<ResourceReferenceTransportCommand> findAllResourceReferencesForQualifier(
            RevisionTransportCommand revision, String qualifier) {
        List<ResourceReference> references = metadataService.
                findAllResourceReferencesForQualifier(revision.id, qualifier)
        use(ResourceReferenceCategory) {
            return references.collect { ResourceReference r ->
                r.toCommandObject()
            }
        }
    }

    @Profiled(tag = "metadataDelegateService.findAllResourceReferencesForQualifiers")
    List<ResourceReferenceTransportCommand> findAllResourceReferencesForQualifiers(
            RevisionTransportCommand revision, List<String> qualifiers) {
        List<ResourceReference> references = metadataService.
                findAllResourceReferencesForQualifiers(revision.id, qualifiers)
        use(ResourceReferenceCategory) {
            return references.collect { ResourceReference r
                r.toCommandObject()
            }
        }
    }
}
