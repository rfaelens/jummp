package net.biomodels.jummp.core

import org.quartz.JobExecutionException
import org.quartz.JobExecutionContext
/**
 * @short Job for cleaning the exchange directory after RTCs if they have been in the
 * exchange for a long time. This can happen if the GC based cleaner doesnt clean up
 * the files in the first place (which could be because the program ended before GC ran,
 * or the GC decided theres lots of memory so it doesnt need to run. 
 *
 * @author Raza Ali <raza.ali@ebi.ac.uk>
 */
class OldFilesExchangeCleanerJob {
    
    def grailsApplication
    def veryOld = ( new Date() ).time - 1000*60*60*6 //remove six hour old files
	
    static triggers = {
    	    //run on startup, and then every six hours.
    	    simple name: 'OldFilesExchangeCleanerTrigger', startDelay: 30000, repeatInterval: 1000*60*60*6  
    }
  
    def execute() {
    	    new File(grailsApplication.config.jummp.vcs.exchangeDirectory).eachFile({f ->
    	    	    if (f.lastModified() <= veryOld) {
    	    	    	    if (f.isFile()) {
    	    	    	    	    f.delete()
    	    	    	    }
    	    	    	    else {
    	    	    	    	    f.deleteDir()
    	    	    	    }
    	    	    }
    	    })
    }

}
