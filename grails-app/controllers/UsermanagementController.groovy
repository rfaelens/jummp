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
    def human=["email":"Email", "username":"Username", 
    		   "userRealName":"Name", "institution":"Institution", 
    		   "orcid":"Orcid", "email.invalid":"invalid",
    		   "net.biomodels.jummp.core.user.RegistrationException: User with same name already exists":"A user with this name already exists. Please try another username"]
    
    private String humanReadable(String input) {
    	if (human.containsKey(input)) {
    		input=human.get(input)
    	}
    	return input
    }
    				   
     /**
     * Passes on any info messages needed to be displayed and renders the register gsp
     */
    def create = {
        String flashMessage=""
        if (flash.message) {
        	flashMessage=flash.message
        }
        render view: "register", model: [postUrl: "", flashMessage:flashMessage]
    }
    
    @Secured(["isAuthenticated()"])
    def edit = {
        String flashMessage=""
        if (flash.message) {
        	flashMessage=flash.message
        }
        render view: "edit", model: [postUrl: "", flashMessage:flashMessage, user: userService.getUser(springSecurityService.principal.id)]
    }
    
    boolean validateUserData(def cmd, def params) {
    	bindData(cmd, params)
    	if (!cmd.validate()) {
    		StringBuilder errors=new StringBuilder("Your submission had the following errors: ")
    		cmd.errors.allErrors.each {
    			errors.append(humanReadable(it.field)).append(" was ").append(humanReadable(it.code)).append(". ")
        	}
        	flash.message=errors.toString()
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
    		flash.message=humanReadable(e.toString())
   			return redirect(action:"create")
    	}
    	flash.message="Profile was updated successfully"
    	redirect(action:"edit")
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
    		flash.message=humanReadable(e.toString())
   			return redirect(action:"create")
    	}
    	render view: "successfulregistration"
    }
}
