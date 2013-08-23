package net.biomodels.jummp.plugins.pharmml
import net.biomodels.jummp.core.model.RevisionTransportCommand

/**
 * Controller for handling Model files in the Pharmml format.
 * @author  Raza Ali <raza.ali@ebi.ac.uk>
 */
class PharmmlController {
	
	def modelDelegateService
	
	def show={
		 Long modelID=params.id as Long;
		 List<RevisionTransportCommand> revs=modelDelegateService.getAllRevisions(modelID)
		 
		render(view:"/model/pharmml/show", model: [revision: revs.last(), 
    		 authors: revs.last().model.creators,
    		 allRevs: revs
    		]
    		)
	}
}
