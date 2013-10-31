import net.biomodels.jummp.webapp.RegistrationCommand

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
        /*if (springSecurityService.isLoggedIn()) {
            redirect uri: SpringSecurityUtils.securityConfig.successHandler.defaultTargetUrl
        }
        else {*/
        	String flashMessage=""
        	if (flash.message) {
        		flashMessage=flash.message
        	}
        	render view: "register", model: [postUrl: "", flashMessage:flashMessage]
     //   }
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
    		redirect(action:"create")
    	}
    	if (params.verysecure) {
    		flash.message="I hope you are a robot. Otherwise something has gone wrong."
    		redirect(action:"create")
    	}
    	RegistrationCommand cmd=new RegistrationCommand()
    	bindData(cmd, params)
    	if (!cmd.validate()) {
    		StringBuilder errors=new StringBuilder("Your submission had the following errors: ")
    		cmd.errors.allErrors.each {
    			errors.append(humanReadable(it.field)).append(" was ").append(humanReadable(it.code)).append(". ")
        	}
        	flash.message=errors.toString()
        	redirect(action:"create")
    	}
    	try 
    	{
    		userService.register(cmd.toUser())
    	}
    	catch(Exception e) {
    		flash.message=humanReadable(e.toString())
   			redirect(action:"create")
    	}
    	render view: "successfulregistration"
    }
}
