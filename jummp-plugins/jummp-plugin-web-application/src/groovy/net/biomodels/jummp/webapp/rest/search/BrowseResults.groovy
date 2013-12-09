package net.biomodels.jummp.webapp.rest.search
import net.biomodels.jummp.core.model.ModelTransportCommand as MTC

class BrowseResults extends Results {
	int modelsAvailable
	public BrowseResults(def browseResults) {
		super(browseResults)
		modelsAvailable=browseResults.modelsAvailable
	}
}
