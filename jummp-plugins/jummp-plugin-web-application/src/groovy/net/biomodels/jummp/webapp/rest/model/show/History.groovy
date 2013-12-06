package net.biomodels.jummp.webapp.rest.model.show
import grails.util.Holders
import net.biomodels.jummp.core.model.RevisionTransportCommand

class History {
	 List<Revision> revisions=[]
	 
	 public History(long modelId) {
	 	 List<RevisionTransportCommand> revs=Holders.grailsApplication.mainContext
	 	 									 .getBean("modelDelegateService")
	 	 									 .getAllRevisions(modelId)
	 	 
	     for (int i=0; i<revs.size(); i++) {
	 	 	 revisions.add(new Revision(revs.get(i)))
	 	 }
	 }
}
