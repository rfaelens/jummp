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


package net.biomodels.jummp.webapp
import net.biomodels.jummp.plugins.security.User
/**
 * @short Command object for User registration
 */
@grails.validation.Validateable
class RegistrationCommand {
    String username
    String email
    String userRealName
    String institution
    String orcid

    static constraints = {
        username(nullable: false, blank: false)
        email(nullable: false, email: true, blank: false)
        userRealName(nullable: false, blank: false)
        institution(nullable:true)
        orcid(nullable:true)
    }

    User toUser() {
        return new User(username: this.username, email: this.email, userRealName: this.userRealName, institution:this.institution, orcid:this.orcid)
    }
}
