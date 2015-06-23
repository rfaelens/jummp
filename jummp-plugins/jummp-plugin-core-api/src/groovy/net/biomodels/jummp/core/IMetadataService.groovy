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

import net.biomodels.jummp.core.annotation.ResourceReferenceTransportCommand
import net.biomodels.jummp.core.model.RevisionTransportCommand

/**
 * @short Contract for services wishing to deal with model metadata.
 *
 * Implementations of this interface must provide means for extracting metadata associated
 * with a model revision, and are expected to work with TransportCommand objects, rather than
 * the underlying domains, to facilitate usage from outside of Jummp's core.
 *
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
interface IMetadataService {
    /**
     * Retrieves revision annotations with a given property.
     *
     * @param revision the TransportCommand wrapper for the revision where annotations were defined.
     * @param qualifier the accession for the qualifier by which to look up annotations.
     * @return a list of TransportCommand wrappers corresponding to the ResourceReference
     */
    List<ResourceReferenceTransportCommand> findAllResourceReferencesForQualifier(
            RevisionTransportCommand revision, String qualifier)
}
