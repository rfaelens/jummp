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
import org.springframework.security.access.prepost.PreAuthorize

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
	def springSecurityService

	Set<User> getNotificationRecipients(ModelTransportCommand model, NotificationType type) {
		Set<String> creators  = model.creatorUsernames;
		Set<User> users = new HashSet<User>();
		creators.each {
			users.add(User.findByUsername(it));
		}
		return users;
	}
	
	NotificationTypePreferences getPreference(User user, NotificationType type) {
		NotificationTypePreferences pref = NotificationTypePreferences.findByUserAndNotificationType(user, type);
		if (!pref) {
			pref = NotificationTypePreferences.getDefault(user, type);
		}
		return pref;
	}
	
	void updatePreferences(List<NotificationTypePreferences> preferences) {
		User user = User.findByUsername(preferences.first().user.username);
		preferences.each { updated ->
			NotificationTypePreferences existing = getPreference(user, updated.notificationType);
			if (existing.sendMail != updated.sendMail || existing.sendNotification != updated.sendNotification) {
				existing.sendMail = updated.sendMail;
				existing.sendNotification = updated.sendNotification;
				existing.save(failOnError: true);
			}
		}
	}
	
	void sendNotificationToUser(User user, Notification notification) {
		NotificationTypePreferences pref = getPreference(user, notification.notificationType);
		if (pref.sendMail) {
			String emailBody = notification.body
            String emailSubject = notification.title
            mailService.sendMail {
                to user.email
                from grailsApplication.config.jummp.security.registration.email.sender
                subject emailSubject
                body emailBody
            }
		}
		if (pref.sendNotification) {
			NotificationUser userNotify = new NotificationUser(notification: notification,
															   user: user);
			userNotify.save(failOnError: true);
		}
	}
	
	void sendNotification(ModelTransportCommand model, Notification notification, Set<User> watchers) {
		notification.save(failOnError:true);
		watchers.each {
			sendNotificationToUser(it, notification);
		}
	}
	
	void modelPublished(def body) {
		RevisionTransportCommand rev  = body.revision as RevisionTransportCommand;
		Notification notification = new Notification();
		notification.title = "Model Published: "+rev.name;
		notification.body = rev.name+" has been published by "+body.user;
		notification.notificationType = NotificationType.PUBLISH;
		notification.sender = User.findByUsername(body.user);
		Set<User> watchers = getNotificationRecipients(model, notification.notificationType);
		watchers = watchers - [notification.sender]
		sendNotification(rev.model, notification, watchers);
	}
    
    void readAccessGranted(def body) {
    	System.out.println("readAccessGranted MESSAGE SENT: "+body);
    }
    
    void writeAccessGranted(def body) {
    	System.out.println("writeAccessGranted MESSAGE SENT: "+body);
    }
    
    int unreadNotificationCount() {
    	User notificationsFor = User.findByUsername(springSecurityService.authentication.name)
    	def notifications = NotificationUser.findAllNotNotificationSeenByUser(notificationsFor);
    	if (notifications) {
    		return notifications.size();
    	}
		return 0;    	
    }
    
    @PreAuthorize("isAuthenticated()") 
    def getNotificationPermissions(String username) {
    	User notificationsFor = User.findByUsername(username)
    	def retval = [];
    	NotificationType.values().each {
    		retval.add(getPreference(notificationsFor, it));
    	}
    	return retval
    }
    
    @PreAuthorize("isAuthenticated()") 
    def list(String username) {
    	User notificationsFor = User.findByUsername(username)
    	return NotificationUser.findAllByUser(notificationsFor).reverse();
    }
    
    void markAsRead(def msgID, String username) {
    	Notification notification = Notification.get(msgID);
    	User notificationsFor = User.findByUsername(username)
    	NotificationUser notificationUser = NotificationUser.findByNotificationAndUser(notification, notificationsFor);
		notificationUser.setNotificationSeen(true);
    	notificationUser.save();
    }
    
    void delete(def body) {
		ModelTransportCommand model  = body.model as ModelTransportCommand;
		Notification notification = new Notification();
		notification.title = "Model Deleted: "+model.name;
		notification.body = model.name+" has been published by "+body.user;
		notification.notificationType = NotificationType.DELETED;
		notification.sender = User.findByUsername(body.user);
		Set<User> watchers = getNotificationRecipients(model, notification.notificationType);
		watchers = watchers - [notification.sender]
		sendNotification(model, notification, watchers);
    }
    
    void update(def body) {
		ModelTransportCommand model  = body.model as ModelTransportCommand;
		Notification notification = new Notification();
		notification.title = "Model Updated: "+model.name;
		notification.body = model.name+" has been updated by "+body.user+". Changes include: "+body.update;
		notification.notificationType = NotificationType.VERSION_CREATED;
		notification.sender = User.findByUsername(body.user);
		Set<User> watchers = getNotificationRecipients(model, notification.notificationType);
		watchers = watchers - [notification.sender]
		sendNotification(model, notification, watchers);
    }
    
}
