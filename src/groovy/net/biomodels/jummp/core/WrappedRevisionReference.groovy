package net.biomodels.jummp.core
import java.lang.ref.WeakReference
import java.lang.ref.ReferenceQueue

/**
 * @short Class for tracking no-longer-in-use revision TCs, storing the location of the
 * folder associated with the revision, so it can be deleted by the garbage collection
 * based exchange cleaner quartz job.
 * @author Raza Ali <raza.ali@ebi.ac.uk>
 */
 
class WrappedRevisionReference extends WeakReference {
	String revisionFolder
	
	WrappedRevisionReference(Object referent, String folder, ReferenceQueue queue) {
		super(referent, queue);
		revisionFolder=folder;
	}
	
	void deleteFolder() {
		(new File(revisionFolder)).deleteDir()
	}
	
	String id() {
		return revisionFolder
	}
	
}
