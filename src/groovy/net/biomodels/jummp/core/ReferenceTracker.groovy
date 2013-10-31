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
* If you modify Jummp, or any covered work, by linking or combining it with [name of library] (or a modified version of that
* library), containing parts covered by the terms of [name of library's license], the licensors of this Program grant you additional
* permission to convey the resulting work. {Corresponding Source for a non-source form of such a combination shall include the source
* code for the parts of [name of library] used as well as that of the covered work.}
**/


package net.biomodels.jummp.core
import java.lang.ref.ReferenceQueue
import net.biomodels.jummp.core.WrappedRevisionReference
import net.biomodels.jummp.core.model.RevisionTransportCommand

/**
 * @short Class written as a spring-bean singleton to track references to revisionTCs
 * 
 * The class maintains a reference queue that will be populated by the Java garbage collector
 * along with references to revisions. A separate quartz job polls the reference queue to see
 * if any revisions have been marked as weak, to clean up resources associated with them
 *
 * @author Raza Ali, raza.ali@ebi.ac.uk
 * @date   9/09/2013
 */
class ReferenceTracker {
    /*
    * Weak references to revision transport command objects, so that files can be deleted from exchange.
    */
    private final static Map<String,WrappedRevisionReference> weakRefs = new HashMap<String,WrappedRevisionReference>()
    private final static ReferenceQueue referenceQueue = new ReferenceQueue()

    /*
    * Self explanatory functions used by the quartz job for getting the reference queue and
    * accessing the references map
    */
    ReferenceQueue getRefQueue() {
    	    return referenceQueue
    }
    
    void clearReference(String id) {
    	    weakRefs.remove(id)
    }
    
    /**
	* Tracks a revision and the folder path associated with it. 
	*
	* Wraps a @param revision into a wrappedrevisionreference, enabling its tracking. Also
	* stores the folder location so it can be deleted easily.
	*
	* @param revision The revision to be added to the references
	* @param path The folder associated with the revision in the exchange
	**/
	
    void addReference(RevisionTransportCommand revision, String path) {
    	    String folder=(new File(path)).getParent()
    	    WrappedRevisionReference ref=new WrappedRevisionReference(revision, folder, referenceQueue)
    	    weakRefs.put(folder,ref)
    }
    
    
}
