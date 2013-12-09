package net.biomodels.jummp.webapp.rest.search

class SearchParameters {
	String sortBy
	String sortDirection
	int offset
	int maxResults
	
	SearchParameters(def results) {
		sortBy=results.sortBy;
		sortDirection=results.sortDirection
        offset=results.offset
        maxResults=results.length
	}
}
