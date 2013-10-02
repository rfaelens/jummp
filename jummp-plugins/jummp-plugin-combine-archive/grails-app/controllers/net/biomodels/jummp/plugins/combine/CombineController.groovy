package net.biomodels.jummp.plugins.combine
import net.biomodels.jummp.core.model.RevisionTransportCommand

/**
 * Controller for handling Model files in the Combine Archive format.
 * @author  Raza Ali <raza.ali@ebi.ac.uk>
 */
class CombineController {
	
	def modelDelegateService
	
	def show={
		 Long modelID=params.id as Long;
		 List<RevisionTransportCommand> revs=modelDelegateService.getAllRevisions(modelID)
		 
		render(view:"/model/combine/show", model: [revision: revs.last(), 
    		 authors: revs.last().model.creators,
    		 allRevs: revs,
    		 flashMessage: params.flashMessage,
    		 showPublishOption:params.showPublishOption, 
    	     showUnpublishOption:params.showUnpublishOption
    		]
    		)
	}
}
