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
* A PARTICULAR PURPOSE. See the GNU Affero General Publicc
*
* You should have received a copy of the GNU Affero General Public License along
* with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
*
* Additional permission under GNU Affero GPL version 3 section 7
*
* If you modify Jummp, or any covered work, by linking or combining it with
* Apache Commons, Perf4j (or a modified version of that library), containing parts
* covered by the terms of Apache License v2.0, the licensors of this
* Program grant you additional permission to convey the resulting work.
* {Corresponding Source for a non-source form of such a combination shall
* include the source code for the parts of Apache Commons, Perf4j used as well as
* that of the covered work.}
**/





package net.biomodels.jummp.core
import net.biomodels.jummp.plugins.security.User
import net.biomodels.jummp.webapp.Notification
import net.biomodels.jummp.webapp.NotificationType
import net.biomodels.jummp.webapp.NotificationTypePreferences
import net.biomodels.jummp.webapp.NotificationUser
import net.biomodels.jummp.core.model.RevisionTransportCommand
import net.biomodels.jummp.core.model.ModelTransportCommand

/**
 * Service asynchronously called by Camel plugin, in response to various messages. 
 * Sends notifications to users.
 *
 * @author Raza Ali <raza.ali@ebi.ac.uk>
 * @date 20141015
 */
class NotificationService {
	
	def modelDelegateService
	def grailsApplication
	def mailService

	Set<User> getNotificationRecipients(ModelTransportCommand model, NotificationType type) {
		Set<String> creators  = model.creators;
		Set<User> users = new HashSet<User>();
		creators.each {
			users.add(User.findByUsername(it));
		}
		return creators;
	}
	
	NotificationTypePreferences getPreference(User user, NotificationType type) {
		NotificationTypePreferences pref = NotificationTypePreferences.findByUserAndNotificationType(user, type);
		if (!pref) {
			pref = NotificationTypePreferences.getDefault(user, type);
		}
		return pref;
	}
	
	void sendNotificationToUser(User user, Notification notification) {
		NotificationTypePreferences pref = getPreference(user, notification.notificationType);
		if (NotificationTypePreferences.sendMail) {
			String emailBody = notification.body
            String emailSubject = notification.title
            mailService.sendMail {
                to recipient
                from grailsApplication.config.jummp.security.registration.email.sender
                subject emailSubject
                body emailBody
            }
		}
		if (NotificationTypePreferences.sendNotification) {
			NotificationUser userNotify = new NotificationUser(notification: notification,
															   user: user);
			userNotify.save(failOnError: true);
		}
	}
	
	void sendNotification(ModelTransportCommand model, Notification notification) {
		notification.save(failOnError:true);
		Set<User> watchers = getNotificationRecipients(model, NotificationType.PUBLISH);
		watchers.each {
			sendNotificationToUser(it, notification);
		}
	}
	
	void modelPublished(def body) {
		RevisionTransportCommand rev  = body.revision as RevisionTransportCommand;
		System.out.println(body);
		System.out.println(body.rev);
		System.out.println(body.rev.inspect());
		Notification notification = new Notification();
		notification.title = "Model Published: "+rev.name;
		notification.body = "Dear Jummp User. "+rev.name+" has been published";
		notification.notificationType = NotificationType.PUBLISH;
		notification.sender = User.findByUsername(body.user);
		sendNotification(rev.model, notification);
	}
    
    void readAccessGranted(def body) {
    	System.out.println("readAccessGranted MESSAGE SENT: "+body);
    }
    
    void writeAccessGranted(def body) {
    	System.out.println("writeAccessGranted MESSAGE SENT: "+body);
    }
    
    void delete(def body) {
    	System.out.println("delete MESSAGE SENT: "+body);
    }
    
    void update(def body) {
    	System.out.println("update MESSAGE SENT: "+body);
    }
    
}
