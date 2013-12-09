package net.biomodels.jummp.webapp.rest.search

class RequestParameters {
	String sortBy
	String sortDirection
	int offset
	int maxResults
	
	RequestParameters(def results) {
		sortBy=results.sortBy;
		sortDirection=results.sortDirection
        offset=results.offset
        maxResults=results.length
	}
}
