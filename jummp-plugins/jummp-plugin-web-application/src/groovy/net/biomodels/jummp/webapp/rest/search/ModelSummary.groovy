package net.biomodels.jummp.webapp.rest.search
import net.biomodels.jummp.core.model.ModelTransportCommand

class ModelSummary {
	long id
	String url
	String name
	String format
	String submitter
	Date submissionDate
	Date lastModified
	public ModelSummary(ModelTransportCommand modelTC) {
         id=modelTC.id
         def linker = new org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib()
         url = linker.createLink(controller: 'model', 
         						 action: 'show', 
         						 absolute: 'true', 
         						 id: id)
		 name=modelTC.name
         format=modelTC.format.name
         submitter=modelTC.submitter
         submissionDate=modelTC.submissionDate
         lastModified=modelTC.lastModifiedDate
 	}
}
