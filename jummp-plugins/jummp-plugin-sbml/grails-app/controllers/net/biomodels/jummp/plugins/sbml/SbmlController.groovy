package net.biomodels.jummp.plugins.sbml
import net.biomodels.jummp.core.model.RevisionTransportCommand

/**
 * Controller for handling Model files in the SBML format.
 * @author  Raza Ali <raza.ali@ebi.ac.uk>
 */
class SbmlController {
	
	def modelDelegateService
	
	def show={
		 Long modelID=params.id as Long;
		 List<RevisionTransportCommand> revs=modelDelegateService.getAllRevisions(modelID)
		 
		render(view:"/model/sbml/show", model: [revision: revs.last(), 
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
