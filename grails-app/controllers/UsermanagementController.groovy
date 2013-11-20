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





import net.biomodels.jummp.webapp.RegistrationCommand
import net.biomodels.jummp.webapp.EditUserCommand
import net.biomodels.jummp.webapp.UpdatePasswordCommand
import net.biomodels.jummp.webapp.ResetPasswordCommand
import grails.plugins.springsecurity.Secured

/*
* @short Controller for managing user registrations
* @author Raza Ali <raza.ali@ebi.ac.uk>
*/



class UsermanagementController {

	/**
     * Dependency injection for the springSecurityService.
     */
    //def springSecurityService
    def simpleCaptchaService
    def userService
    def springSecurityService
    			
    private String checkForMessage() {
        String flashMessage=""
        if (flash.message) {
        	flashMessage=flash.message
        }
        return flashMessage
    }
    
    private Object checkForErrorBean() {
    	if (flash.validationError) {
    		return flash.validationError
    	}
    	return null
    }
    
     /**
     * Passes on any info messages needed to be displayed and renders the register gsp
     */
    @Secured(["isAnonymous()"])
    def create = {
    	render view: "register", model: [postUrl: "", flashMessage:checkForMessage(), 
    									validationErrorOn: checkForErrorBean()]
    }
    
    @Secured(["isAuthenticated()"])
    def edit = {
    	render view: "edit", model: [postUrl: "", flashMessage:checkForMessage(), 
    								validationErrorOn: checkForErrorBean(), 
    								user: userService.getUser(springSecurityService.principal.username)]
    }
    
    @Secured(["isAuthenticated()"])
    def editPassword = {
    	render view: "editPassword", model: [postUrl: "", flashMessage:checkForMessage(), 
    										validationErrorOn: checkForErrorBean(), 
    										user: userService.getUser(springSecurityService.principal.username)]
    }
    
    @Secured(["isAuthenticated()"])
    def show = {
    	render view: "show", model: [postUrl: "", flashMessage:checkForMessage(), 
    								validationErrorOn: checkForErrorBean(), 
    								user: userService.getUser(springSecurityService.principal.username)]
    }
    
    @Secured(["isAnonymous()"])
    def forgot = {
    	render view: "forgot", model: [postUrl: "", flashMessage:checkForMessage(), 
    								validationErrorOn: checkForErrorBean()]
    }

    @Secured(["isAnonymous()"])
    def passwordreset = {
    	flash.hashCode=params.id
    	redirect action: reset
    }
    
    /**
    * Password reset based on the unique code sent to the user
    **/
    @Secured(["isAnonymous()"])
    def reset = {
    	render view: "reset", model: [postUrl: "", flashMessage:checkForMessage(), 
    								validationErrorOn: checkForErrorBean(), 
    								hashCode: flash.hashCode]
    }
    				   
    
    boolean validateUserData(def cmd, def params) {
    	bindData(cmd, params)
    	if (!cmd.validate()) {
    		flash.validationError=cmd
        	return false
    	}
    	return true
    }
    
    
    /**
     * Validates the command object and then uses the user service to 
     * edit a user. If an error occurs at any point, the method redirects
     * to edit action and sends the user a helpful message.
     */
    def editUser = {
    	EditUserCommand cmd=new EditUserCommand()
    	if (!validateUserData(cmd, params)) {
    		return redirect(action:"edit")
    	}
    	try 
    	{
    		userService.editUser(cmd.toUser())
    	}
    	catch(Exception e) {
    		flash.message=e.toString().split(":")[0]
   			return redirect(action:"edit")
    	}
    	flash.message="Profile was updated successfully"
    	redirect(action:"show")
    }

    /**
     * Validates the command object and then uses the user service to 
     * edit a user. If an error occurs at any point, the method redirects
     * to edit action and sends the user a helpful message.
     */
    def newPassword = {
    	ResetPasswordCommand cmd=new ResetPasswordCommand()
    	if (!validateUserData(cmd, params)) {
    		flash.hashCode=params.hashCode
    		return redirect(action:"reset")
    	}
    	try 
    	{
    		userService.resetPassword(cmd.hashCode, cmd.username, cmd.newPassword)
    	}
    	catch(Exception e) {
    		flash.message="password.reset.service.error"
   			return redirect(action:"reset")
    	}
    	flash.flashMessage="Password for ${cmd.username} was updated successfully. Please try logging in now"
    	redirect(controller: "login", action:"auth")
    }

    
    
    /**
     * Validates the command object and then uses the user service to 
     * edit a user. If an error occurs at any point, the method redirects
     * to edit action and sends the user a helpful message.
     */
    def updatePassword = {
    	UpdatePasswordCommand cmd=new UpdatePasswordCommand()
    	if (!validateUserData(cmd, params)) {
    		return redirect(action:"editPassword")
    	}
    	try 
    	{
    		userService.changePassword(cmd.oldPassword, cmd.newPassword)
    	}
    	catch(Exception e) {
    		flash.message=e.toString().split(":")[0]
   			return redirect(action:"editPassword")
    	}
    	flash.message="Password was updated successfully"
    	redirect(action:"show")
    }
    
    /**
    * Requests a password link from the user service, hiding the exception thrown
    * if the username provided does not exist.
    **/
    
    def requestPassword = {
    	try
    	{
    		userService.requestPassword(params.username)
    	}
    	catch(Exception e) {
    		e.printStackTrace()
    	}
    	flash.message="Thank you. Please check the email associated with ${params.username}'s account"
    	redirect(action:"forgot")
    }
    
     /**
     * Performs validation on the captcha, ensures that the empty security parameter is not
     * filled (as is commonly done by robots), validates the command object and then uses
     * the user service to create a user. If an error occurs at any point, the method redirects
     * to create action and sends the user a helpful message.
     */
    def signUp = {
    	boolean captchaValid = simpleCaptchaService.validateCaptcha(params.captcha)
    	if (!captchaValid) {
    		flash.message="The text entered did not match the image. Please try again"
    		return redirect(action:"create")
    	}
    	if (params.verysecure) {
    		flash.message="I hope you are a robot. Otherwise something has gone wrong."
    		return redirect(action:"create")
    	}
    	RegistrationCommand cmd=new RegistrationCommand()
    	if (!validateUserData(cmd, params)) {
    		return redirect(action:"create")
    	}
    	try 
    	{
    		userService.register(cmd.toUser())
    	}
    	catch(Exception e) {
    		flash.message=e.toString().split(":")[0]
   			return redirect(action:"create")
    	}
    	render view: "successfulregistration"
    }
}
