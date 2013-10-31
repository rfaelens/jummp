/**
* Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI), Deutsches Krebsforschungszentrum (DKFZ)
*
* This file is part of Jummp.
*
* Jummp is free software; you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
*
* Jummp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License along with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
**/


package net.biomodels.jummp.core.model

/**
 * @short Enum to specify the column for sorting the Model overview.
 *
 * The primary use of this enum is to support sorting of arbitrary columns
 * in ModelService.getAllModels(). This enum is used to specify the column
 * which has to be used for sorting when going down to the database.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
public enum ModelListSorting {
    ID, ///< sort by the Model.id column in numerical order
    NAME, ///< sort by Model.name in alphanumerical order
    PUBLICATION, ///< sort by the Model's publication, TODO: what to sort on directly
    LAST_MODIFIED, ///< sort by the last modification date, that is latest revision
    FORMAT, ///< sort by the name of the format
    SUBMITTER, ///< sort by the submitter
    SUBMISSION_DATE ///< sort by the submission date
}
