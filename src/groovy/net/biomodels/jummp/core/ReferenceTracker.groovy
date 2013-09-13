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
