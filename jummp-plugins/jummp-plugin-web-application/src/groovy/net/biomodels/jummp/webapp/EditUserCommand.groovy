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


package net.biomodels.jummp.webapp
import net.biomodels.jummp.plugins.security.User

/**
 * @short Command Object to validate the user before editing.
 * @author: Raza Ali: raza.ali@gmail.com
 */
 @grails.validation.Validateable
class EditUserCommand implements Serializable {
    private static final long serialVersionUID = 1L
    String username
    String userRealName
    String email
    String institution
    String orcid

    static constraints = {
        username(nullable: false, blank: false)
        userRealName(nullable: false, blank: false)
        email(nullable: false, blank: false, email: true)
        institution(nullable:true)
        orcid(nullable:true)
    }

    /**
     *
     * @return The command object as a User
     */
    User toUser() {
        return new User(username: this.username, userRealName: this.userRealName, email: this.email, institution:this.institution, orcid:this.orcid)
    }
}
