package net.biomodels.jummp.core

import net.biomodels.jummp.model.Publication
import net.biomodels.jummp.core.model.PublicationLinkProvider
import net.biomodels.jummp.model.Author
import org.xml.sax.SAXParseException
import org.springframework.transaction.annotation.Transactional

/**
 * @short Service for fetching Publication Information for PubMed resources.
 *
 * This service class handles the interaction with the Web Service to retrieve
 * publication information for PubMed resources. It connects to citexplore and
 * parses the returned HTML page for the publication information.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class PubMedService {

    static transactional = true

    Publication getPublication(String id) throws JummpException {
        Publication publication = Publication.findByLinkProviderAndLink(PublicationLinkProvider.PUBMED, id)
        if (publication) {
            return publication
        } else {
            return fetchPublicationData(id)
        }
    }

    private setFieldIfItExists(String fieldName, Publication publication, def xmlField, boolean castToInt) {
    	if (xmlField && xmlField.size()==1) {
    		String text=xmlField.text()
    		def fields=Publication.getFields()
    		if (castToInt) {
    			publication."${fieldName}"=Integer.parseInt(text)
    		}
    		else {
    			publication."${fieldName}"=text
    		}
    	}
    }
    
    /**
     * Downloads the XML describing the PubMed resource and parses the Publication information.
     * @param id The PubMed Identifier
     * @return A fully populated Publication
     */
    @SuppressWarnings("EmptyCatchBlock")
    @Transactional
    private Publication fetchPublicationData(String id) throws JummpException {
        URL url
        try {
            url = new URL("http://www.ebi.ac.uk/europepmc/webservices/rest/search/query=ext_id:${id}%20src:med&resulttype=core")
        } catch (MalformedURLException e) {
            // TODO: throw a specific exception
            throw new JummpException("PubMed URL is malformed", e)
        }

        def slurper
        try {
            slurper = new XmlSlurper().parse(url.openStream())
        } catch (SAXParseException e) {
            throw new JummpException("Could not parse PubMed information", e)
        }
        Publication publication = new Publication(linkProvider: PublicationLinkProvider.PUBMED, link: id)
        setFieldIfItExists("pages", publication, slurper.resultList.result.pageInfo, false)
        setFieldIfItExists("title", publication, slurper.resultList.result.title, false)
        setFieldIfItExists("affiliation", publication, slurper.resultList.result.affiliation, false)
        setFieldIfItExists("synopsis", publication, slurper.resultList.result.abstractText, false)
        
        if (slurper.resultList.result.journalInfo) {
        	setFieldIfItExists("month", publication, slurper.resultList.result.journalInfo.monthOfPublication, true)
        	setFieldIfItExists("year", publication, slurper.resultList.result.journalInfo.yearOfPublication, true)
        	//setFieldIfItExists("day", publication, slurper.resultList.result.journalInfo.dateOfPublication, true) //we have integer, this returns a string
        	setFieldIfItExists("volume", publication, slurper.resultList.result.journalInfo.volume, true)
        	setFieldIfItExists("issue", publication, slurper.resultList.result.journalInfo.issue, true)
        	setFieldIfItExists("journal", publication, slurper.resultList.result.journalInfo.journal.title, false)
        }
        System.out.println(slurper.resultList.result.authorString.text())
        parseAuthors(slurper, publication)
        publication.save(flush:true)
        System.out.println("Publication: "+publication.inspect())
        
        
        if (!publication.validate()) {
        	System.out.println(publication.errors.inspect())
            throw new JummpException("Retrieved PubMed Publication ${id} does not create a valid Publication")
        }
        return publication
    }

    /**
     * Parses the author information and adds it to @p publication
     * @param slurper The parsed XML document
     * @param publication The publication to add the authors to
     */
    private void parseAuthors(def slurper, Publication publication) {
        for (def authorXml in slurper.resultList.result.authorList.author) {
            Author author = new Author()
            author.lastName = authorXml.lastName[0].text()
            //author.firstName = authorXml.ForeName[0].text()
            author.initials = authorXml.initials[0].text()
            author.save()
            publication.addToAuthors(author)
        }
    }
}
