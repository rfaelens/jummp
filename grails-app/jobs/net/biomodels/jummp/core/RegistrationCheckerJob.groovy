package net.biomodels.jummp.core

import org.quartz.JobExecutionException
import org.quartz.JobExecutionContext
/**
 * @short Job for checking the status of the modelfileformatconfig which was getting
 * lost (so long as it was in the service). While its running anyway, it updates the 
 * search provider's index, ensuring we have (relatively) recent results.
 *
 * @author Raza Ali <raza.ali@ebi.ac.uk>
 */
class RegistrationCheckerJob {
    
    def grailsApplication
	
    static triggers = {
    	    //run on startup, and then every six hours.
    	    simple name: 'regCheckTrigger', startDelay: 30000, repeatInterval: 5*60000  
    }
  
    def execute() {
    	    grailsApplication.mainContext.getBean("modelFileFormatConfig").status()
    	    //grailsApplication.mainContext.getBean("searchEngine").refreshIndex()
    }

}
