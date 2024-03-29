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





package net.biomodels.jummp.core.user

import net.biomodels.jummp.core.JummpException

/**
 * @short Exception indicating that a Role could not be found.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class RoleNotFoundException extends JummpException implements Serializable {
    private static final long serialVersionUID = 1L
    Long id = null
    String authority = null

    RoleNotFoundException(Long id) {
        super("Role with id ${id} not found".toString())
        this.id = id
    }

    RoleNotFoundException(String authority) {
        super("Role with authority ${authority} not found".toString())
        this.authority = authority
    }
}
