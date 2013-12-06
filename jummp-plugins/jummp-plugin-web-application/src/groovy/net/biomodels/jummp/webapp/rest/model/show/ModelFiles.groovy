package net.biomodels.jummp.webapp.rest.model.show
import net.biomodels.jummp.core.model.RepositoryFileTransportCommand

class ModelFiles {
	List<FileType> main=new LinkedList()
	List<AdditionalType> additional=new LinkedList()
	
	public ModelFiles(List<RepositoryFileTransportCommand> files) {
		files.findAll { it.mainFile }.each {
			main.add(new FileType(it))
		}
		files.findAll { !it.mainFile }.each {
			additional.add(new AdditionalType(it))
		}
	}
}
