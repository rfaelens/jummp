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





package net.biomodels.jummp.plugins.security
import java.util.regex.Pattern
import java.util.regex.Matcher
/**
 * @short Representation of a Person. Should be subclassed to help understand what type of person
 * @author Raza Ali <raza.ali@ebi.ac.uk>
 */
class Person implements Serializable {
    private static final long serialVersionUID = 1L

    String userRealName
    String institution
    String orcid
    
    static constraints = {
        userRealName(blank: false)
        institution(nullable:true)
        orcid nullable: true, unique:true, validator: {
        	if (it) {
        		Pattern p = Pattern.compile("^\\d{4}-\\d{4}-\\d{4}-\\d{3}(\\d|X)\$");
        		Matcher m = p.matcher(it);
        		return m.matches()
        	}
        	return true
        }
    }
    
    public PersonTransportCommand toCommandObject() {
    	return new PersonTransportCommand(id: this.id,
    									  userRealName: this.userRealName,
    									  institution: this.institution,
    									  orcid: this.orcid)
    }
    
    public String toString() {
    	return userRealName
    }
    
}
