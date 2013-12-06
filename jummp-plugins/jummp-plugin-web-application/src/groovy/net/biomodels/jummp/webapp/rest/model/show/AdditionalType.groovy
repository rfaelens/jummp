package net.biomodels.jummp.webapp.rest.model.show
import net.biomodels.jummp.core.model.RepositoryFileTransportCommand

class AdditionalType extends FileType {
	String description
	
	public AdditionalType(RepositoryFileTransportCommand file) {
		super(file)
		description=file.description
	}
}
