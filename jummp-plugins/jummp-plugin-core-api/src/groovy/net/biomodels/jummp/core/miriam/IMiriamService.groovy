/**
* Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI),
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





package net.biomodels.jummp.core.miriam

/**
 * @short Interface for MIRIAM service.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz.de>
 */
public interface IMiriamService {
    /**
     * Updates the MIRIAM Resources in the database from the XML specified in @p url.
     * This is executed in a threaded way
     * @param url The URL to the MIRIAM Resource XML
     * @param force If @c true previously fetched data will be discarded, if @c false only new entries are added
     */
    public void updateMiriamResources(String url, boolean force)

    /**
     * Returns all relevant MIRIAM data in one map for the given @p resource, that is a complete URN
     * consisting of the MIRIAM datatype plus the element identifier within the datatype
     * The map consists of the following elements:
     * @li <strong>dataTypeLocation:</strong> URL for the datatype
     * @li <strong>dataTypeName:</strong> Human readable name of the datatype
     * @li <strong>name:</strong> Human readable name of the identifier (if it could be resolved) or the identifier
     * @li <strong>url:</strong> URL to the identifier
     *
     * If the datatype is unknown or no MIRIAM resource could be located for the datatype an empty map is returned.
     * @param urn The URN consisting of both MIRIAM datatype and identifier
     * @return Map as described above
     */
    public Map miriamData(String urn)

    /**
     * Updates all Miriam Identifiers stored in the database by trying to resolve the name again and update
     * if it changed.
     */
    public updateAllMiriamIdentifiers()
    /**
     * Fetches the MIRIAM annotations in each of the models.
     */
    public void updateModels()
}
