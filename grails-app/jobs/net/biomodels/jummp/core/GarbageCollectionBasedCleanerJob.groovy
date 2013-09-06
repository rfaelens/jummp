package net.biomodels.jummp.core

import org.quartz.JobExecutionException
import org.quartz.JobExecutionContext
import java.lang.ref.ReferenceQueue
import net.biomodels.jummp.core.WrappedRevisionReference

/**
 * @short Job for cleaning the exchange directory after RTCs have been
 * marked by the GC as no longer having references to them. Gets the 
 * reference queue from the model delegate service, polls for any changes
 * and handles any resources associated with the object.
 *
 * @author Raza Ali <raza.ali@ebi.ac.uk>
 */
class GarbageCollectionBasedCleanerJob {
    
    def modelDelegateService;
    boolean forcedGC=false;
	
    static triggers = {
    	    //Run every three minutes
    	    simple name: 'GarbageCollectionBasedCleanerTrigger', startDelay: 1000*60*3, repeatInterval: 1000*60*3  
    }
  
    def execute() {
   	    if (forcedGC) {
   	    	    System.gc()
   	    }
    	    ReferenceQueue queue=modelDelegateService.getRefQueue()
    	    while (true) {
    	    	   WrappedRevisionReference ref=(WrappedRevisionReference) queue.poll()
    	    	   if (!ref) {
    	    	   	   break
    	    	   }
    	    	   System.out.println("GC Cleaner deleting: "+ref.id()+" ..."+ref.deleteFolder())
    	    	   modelDelegateService.clearReference(ref.id())
    	    }
    }

}
