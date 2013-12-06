package net.biomodels.jummp.webapp.rest.model.show
import net.biomodels.jummp.core.model.ModelFormatTransportCommand

class Format {
	String format
	String version
	
	public Format(ModelFormatTransportCommand f) {
		format=f.name
		version=f.formatVersion
	}
}
