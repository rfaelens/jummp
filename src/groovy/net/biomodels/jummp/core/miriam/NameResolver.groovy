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
**/





package net.biomodels.jummp.core.miriam

/**
 * @short Interface for resolving the name of MIRIAM identifiers.
 *
 * This interface needs to be implemented by classes which are able to resolve the name of
 * a MIRIAM identifier for a given MiriamDatatype. Classes implementing this interface are
 * primarily used by the MiriamService in order to resolve the names of an identifier and
 * store it in the database.
 *
 * @see net.biomodels.jummp.core.MiriamService
 * @see MiriamDatatype
 * @see MiriamIdentifier
 * @author Martin Gräßlin <m.graesslin@dkfz.de>
 */
public interface NameResolver {

    /**
     * An implementing class should return @c true if it is able to resolve the name
     * for identifiers of the given @p datatype.
     * @param datatype The Miriam Datatype for which a name for an identifier should be resolved.
     * @return @c true if the name can be resolved, @c false otherwise
     */
    boolean supports(MiriamDatatype datatype)

    /**
     * This method is invoked when the name for the Miriam identifier @p id of MiriamDatatype @p datatype
     * should be resolved. The method has to resolve the name and return it. If it is not able to resolve
     * the name, it has to return @c null.
     * @param datatype The Datatype of the Miriam identifier
     * @param id The identifier for which the name should be resolved
     * @return The resolved name in success case, in failure case @c null
     */
    String resolve(MiriamDatatype datatype, String id)
}