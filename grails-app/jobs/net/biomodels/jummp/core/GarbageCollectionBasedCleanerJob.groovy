/**
* Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI), Deutsches Krebsforschungszentrum (DKFZ)
*
* This file is part of Jummp.
*
* Jummp is free software; you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as
* published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
*
* Jummp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License along with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
*
* Additional permission under GNU Affero GPL version 3 section 7
*
* If you modify Jummp, or any covered work, by linking or combining it with Quartz (or a modified version of that
* library), containing parts covered by the terms of Apache 2.0, the licensors of this Program grant you additional
* permission to convey the resulting work. {Corresponding Source for a non-source form of such a combination shall include the source
* code for the parts of Quartz used as well as that of the covered work.}
**/


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
    
    def grailsApplication;
    boolean forcedGC=false;
	
    static triggers = {
    	    //Run every three minutes
    	    simple name: 'GarbageCollectionBasedCleanerTrigger', startDelay: 1000*60*3, repeatInterval: 1000*60*3  
    }
  
    def execute() {
   	    if (forcedGC) {
   	    	    System.gc()
   	    }
   	    def refTracker=grailsApplication.mainContext.getBean("referenceTracker")
    	    ReferenceQueue queue=refTracker.getRefQueue()
    	    while (true) {
    	    	   WrappedRevisionReference ref=(WrappedRevisionReference) queue.poll()
    	    	   if (!ref) {
    	    	   	   break
    	    	   }
    	    	   System.out.println("GC Cleaner deleting: "+ref.id()+" ..."+ref.deleteFolder())
    	    	   refTracker.clearReference(ref.id())
    	    }
    }

}
