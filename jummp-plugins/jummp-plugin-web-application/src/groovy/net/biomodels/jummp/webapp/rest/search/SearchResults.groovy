package net.biomodels.jummp.webapp.rest.search
import net.biomodels.jummp.core.model.ModelTransportCommand as MTC

class SearchResults {
	int totalMatches
	SearchParameters queryParameters
	List<ModelSummary> models=[]
	public SearchResults(def searchResults) {
		totalMatches=searchResults.matches
		searchResults.models.each {
			models.add(new net.biomodels.jummp.webapp.rest.search.ModelSummary(it))
		}
		queryParameters=new SearchParameters(searchResults)
	}
}
