package net.biomodels.jummp.core
import java.lang.ref.ReferenceQueue
import net.biomodels.jummp.core.WrappedRevisionReference
import net.biomodels.jummp.core.model.RevisionTransportCommand

class ReferenceTracker {
    /*
    * Weak references to revision transport command objects, so that files can be deleted from exchange.
    */
    private final static Map<String,WrappedRevisionReference> weakRefs = new HashMap<String,WrappedRevisionReference>()
    private final static ReferenceQueue referenceQueue = new ReferenceQueue()

    /*
    * Functions used by the quartz job for removing files from the exchange
    */
    ReferenceQueue getRefQueue() {
    	    return referenceQueue
    }
    
    void clearReference(String id) {
    	    weakRefs.remove(id)
    }
    
    void addReference(RevisionTransportCommand revision, String path) {
    	    String folder=(new File(path)).getParent()
    	    WrappedRevisionReference ref=new WrappedRevisionReference(revision, folder, referenceQueue)
    	    weakRefs.put(folder,ref)
    }
    
}
