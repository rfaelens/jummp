package net.biomodels.jummp.webapp.rest.error

class Error {
	String errorType
	String errorMessage
	
	public Error(String e, String msg) {
		errorType=e
		errorMessage=msg
	}
}
