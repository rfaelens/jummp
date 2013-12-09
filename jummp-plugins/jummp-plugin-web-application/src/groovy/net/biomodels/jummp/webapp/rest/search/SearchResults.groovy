package net.biomodels.jummp.webapp.rest.search
import net.biomodels.jummp.core.model.ModelTransportCommand as MTC

class SearchResults extends Results {
	int matches
	public SearchResults(def searchResults) {
		super(searchResults)
		matches=searchResults.matches
	}
}
