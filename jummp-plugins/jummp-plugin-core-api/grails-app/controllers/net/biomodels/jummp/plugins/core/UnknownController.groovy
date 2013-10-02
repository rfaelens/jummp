package net.biomodels.jummp.plugins.core
import net.biomodels.jummp.core.model.RevisionTransportCommand

/**
 * Controller for handling Model files in the unknown model format.
 * @author  Raza Ali <raza.ali@ebi.ac.uk>
 */
class UnknownController {
	
	def modelDelegateService
	
	def show={
		 Long modelID=params.id as Long;
		 List<RevisionTransportCommand> revs=modelDelegateService.getAllRevisions(modelID)
		 
		render(view:"/model/unknown/show", model: [revision: revs.last(), 
    		 authors: revs.last().model.creators,
    		 allRevs: revs,
    		 flashMessage: params.flashMessage,
		     canUpdate:params.canUpdate,
		     showPublishOption:params.showPublishOption, 
    	     showUnpublishOption:params.showUnpublishOption
    		]
    		)
	}
}
