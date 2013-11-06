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
 * Domain object to describe a MIRIAM resource provider.
 * Linked to MiriamDatatype.
 */
class MiriamResource {
    static belongsTo = [datatype: MiriamDatatype]
    /**
     * The identifier of the resource. E.g. "MIR:00100022"
     */
    String identifier
    /**
     * The location of the resource. In general a URL
     */
    String location
    /**
     * The URL to resolve one MIRIAM URI. $Id in the URL needs to be replaced.
     */
    String action
    /**
     * Whether the resource is obsoleted.
     */
    boolean obsolete = false

    static constraints = {
    }

    static mapping = {
        version false
        identifier unique: true
    }

    public String toString() {
        return "Id: ${identifier}, Location: ${location}, Action: ${action}, Obsolete: ${obsolete}"
    }
}
