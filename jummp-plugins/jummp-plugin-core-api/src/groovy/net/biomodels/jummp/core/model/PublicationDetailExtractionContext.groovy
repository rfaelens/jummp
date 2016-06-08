/**
 * Copyright (C) 2010-2016 EMBL-European Bioinformatics Institute (EMBL-EBI),
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





package net.biomodels.jummp.core.model

/**
 * @short Simple wrapper around a publication to capture how the its details were retrieved.
 *
 * Instances of this class specify, through the flag comesFromDatabase, whether a publication
 * has been fetched from the database, or from an external service.
 *
 * @author Tung Nguyen <tung.nguyen@ebi.ac.uk>
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 * @date created on 08/06/16.
 */
//PublicationInformationExtractionContext
class PublicationDetailExtractionContext implements Serializable {
    private static final long serialVersionUID = 1L
    /**
     * Information about the Publication.
     */
    PublicationTransportCommand publication

    /**
     * Flag to indicate whether the publication was found in the database or via an
     * external call e.g. to PubMedService.
     */
    boolean comesFromDatabase
}
