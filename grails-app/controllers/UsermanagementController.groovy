import net.biomodels.jummp.webapp.RegistrationCommand

class UsermanagementController {

	/**
     * Dependency injection for the springSecurityService.
     */
    //def springSecurityService
    def simpleCaptchaService
    /**
     * Default action; redirects to 'defaultTargetUrl' if logged in, /login/auth otherwise.
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
    
    def signUp = {
    	boolean captchaValid = simpleCaptchaService.validateCaptcha(params.captcha)
    	if (!captchaValid) {
    		flash.message="The text entered did not match the image. Please try again"
    		redirect(action:"create")
    	}
    	render view: "successfulregistration"
    }
}
