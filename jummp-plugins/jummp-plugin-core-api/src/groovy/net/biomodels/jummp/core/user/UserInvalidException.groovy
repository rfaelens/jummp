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

/**
 * @short Exception indicating that a User object does not validate.
 *
 * This exception should be thrown whenever a User object does not validate,
 * either when it is initially created or updated later one.
 *
 * For security reasons the Exception does not include the User data which was
 * tried to be modified.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class UserInvalidException extends UserManagementException implements Serializable {
    private static final long serialVersionUID = 1L
    UserInvalidException(String userName) {
        super("The user ${userName} does not validate".toString(), userName)
    }
}
