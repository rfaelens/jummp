package net.biomodels.jummp.webapp.rest.model.show
import net.biomodels.jummp.core.model.RevisionTransportCommand

class Revision {
	int versionNumber
	long submitted
	String submitter
	String comment
	
	public Revision(RevisionTransportCommand revision) {
		versionNumber=revision.revisionNumber
		submitted=revision.uploadDate.getTime()
		submitter=revision.owner
		comment=revision.comment
	}
}
