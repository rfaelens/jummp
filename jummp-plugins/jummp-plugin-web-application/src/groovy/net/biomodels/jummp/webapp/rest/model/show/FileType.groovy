package net.biomodels.jummp.webapp.rest.model.show
import net.biomodels.jummp.core.model.RepositoryFileTransportCommand

class FileType {
	String name
	String filesize
	
	public FileType(RepositoryFileTransportCommand file) {
		File f=new File(file.path)
		name=f.getName()
		filesize=f.length()
	}
}
