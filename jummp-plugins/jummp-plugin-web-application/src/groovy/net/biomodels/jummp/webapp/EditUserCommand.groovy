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


package net.biomodels.jummp.webapp
import net.biomodels.jummp.plugins.security.User
import net.biomodels.jummp.plugins.security.Person
import java.util.regex.Pattern
import java.util.regex.Matcher
import net.biomodels.jummp.webapp.NotificationTypePreferences
import net.biomodels.jummp.webapp.NotificationType

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
    
    boolean sendNotification1;
    boolean sendNotification2;
    boolean sendNotification3;
    boolean sendNotification4;

    boolean sendMail1;
    boolean sendMail2;
    boolean sendMail3;
    boolean sendMail4;

    static constraints = {
        username(nullable: false, blank: false)
        userRealName(nullable: false, blank: false)
        email(nullable: false, blank: false, email: true)
        institution(nullable:true)
        orcid nullable: true, validator: {
        	if (it) {
        		Pattern p = Pattern.compile("^\\d{4}-\\d{4}-\\d{4}-\\d{3}(\\d|X)\$");
        		Matcher m = p.matcher(it);
        		return m.matches()
        	}
        	return true
        }
    }

    /**
     *
     * @return The command object as a User
     */
    User toUser() {
    	return new User(username: this.username, person: new Person(userRealName: this.userRealName, institution:this.institution, orcid:this.orcid), email: this.email)
    }
    
    List<NotificationTypePreferences> getPreferences(User user) {
    	List<NotificationTypePreferences> prefs = new LinkedList<NotificationTypePreferences>();
    	for (int i=1; i<=4; i++) {
    		NotificationType type = NotificationType.getById(i);
    		NotificationTypePreferences pref = new NotificationTypePreferences(user: user, 
    																		   notificationType: type, 
    																		   sendMail: this."sendMail${i}", 
    																		   sendNotification: this."sendNotification${i}")
            prefs.add(pref)
    	}
    	return prefs
    }
}
