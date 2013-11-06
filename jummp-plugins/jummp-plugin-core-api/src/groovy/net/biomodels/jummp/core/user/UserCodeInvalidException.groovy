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





package net.biomodels.jummp.core.user

/**
 * @short Exception indicating that a user change code is not valid.
 *
 * This exception should be thrown in case a user tries to e.g. validate the registration or
 * reset the password with a code which is not valid.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class UserCodeInvalidException extends UserManagementException implements Serializable {
    private static final long serialVersionUID = 1L
    private String code

    UserCodeInvalidException(String userName, Long id, String code) {
        super("Code ${code} for changing user identified by ${userName} is invalid".toString(), userName)
        this.code = code
        setId(id)
    }

    public String getCode() {
        return code
    }
}
