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


package net.biomodels.jummp.core.user

import net.biomodels.jummp.core.JummpException

/**
 * @short Base class for all User management related exceptions.
 * 
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
abstract class UserManagementException extends JummpException implements Serializable {
    private static final long serialVersionUID = 1L
    private String userName = null
    private Long id = null

    protected UserManagementException(String userName) {
        this("Unknown error while managing user with username ${userName}".toString(), userName)
    }

    protected UserManagementException(String message, String userName) {
        super(message)
        this.userName = userName
    }

    protected UserManagementException(long id) {
        this("Unknown eror while managing user with id ${id}".toString(), id)
    }

    protected UserManagementException(String message, Long id) {
        super(message)
        this.id = id
    }

    protected setUserName(String userName) {
        this.userName = userName
    }

    protected setId(Long id) {
        this.id = id
    }

    public String getUserName() {
        return this.userName
    }

    public Long getId() {
        return this.id
    }
}
