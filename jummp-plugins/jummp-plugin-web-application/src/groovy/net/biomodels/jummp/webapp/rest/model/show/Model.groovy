package net.biomodels.jummp.webapp.rest.model.show
import net.biomodels.jummp.core.model.RevisionTransportCommand
import net.biomodels.jummp.core.model.ModelTransportCommand

class Model {
		String name
		String description
		Format format
		String publication
		ModelFiles files
		History history
		
		public Model(RevisionTransportCommand revision) {
			ModelTransportCommand model=revision.model
			name=model.name
			description=revision.description
			format=new Format(revision.format)
			if (!model.publication) {
				publication="Not Provided"
			}
			else {
				publication=model.publication.linkProvider.identifiersPrefix?
								model.publication.linkProvider.identifiersPrefix+model.publication.link:model.publication.link
			}
			files=new ModelFiles(revision.files)
			history=new History(model.id)
		}
}
