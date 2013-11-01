/**
* Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI),
* Deutsches Krebsforschungszentrum (DKFZ)
*
* This file is part of Jummp.
*
* Jummp is free software; you can redistribute it and/or modify it under the
* terms of the GNU Affero General Public License as published by the Free
* Software Foundation; either version 3 of the License, or (at your option) any
* later version.
*
* Jummp is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
* details.
*
* You should have received a copy of the GNU Affero General Public License along
* with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
*
* Additional permission under GNU Affero GPL version 3 section 7
*
* If you modify Jummp, or any covered work, by linking or combining it with
* Spring Framework (or a modified version of that library), containing parts
* covered by the terms of Apache License v2.0 the licensors of this
* Program grant you additional permission to convey the resulting work.
* {Corresponding Source for a non-source form of such a combination shall
* include the source code for the parts of Spring Framework used as well as
* that of the covered work.}
**/





package net.biomodels.jummp.core

import net.biomodels.jummp.model.Publication
import net.biomodels.jummp.model.PublicationLinkProvider
import net.biomodels.jummp.model.Author
import org.xml.sax.SAXParseException
import org.springframework.transaction.annotation.Transactional
import net.biomodels.jummp.core.model.PublicationTransportCommand
import java.util.regex.Pattern
import java.util.regex.Matcher
/**
 * @short Service for fetching Publication Information for PubMed resources.
 *
 * This service class handles the interaction with the Web Service to retrieve
 * publication information for PubMed resources. It connects to citexplore and
 * parses the returned HTML page for the publication information.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 * @author Raza Ali <raza.ali@ebi.ac.uk>
 */
class PubMedService {

    static transactional = true

    Publication getPublication(String id) throws JummpException {
    	Publication publication = Publication.createCriteria().get() {
    		eq("link",id)
    		linkProvider {
    			eq("linkType",PublicationLinkProvider.LinkType.PUBMED)
    		}
    	}
    	if (publication) {
            return publication
        } else {
        	return fetchPublicationData(id)
        }
    }
    
    Publication getPublication(PublicationTransportCommand cmd) throws JummpException {
    	if (PublicationLinkProvider.LinkType.valueOf(cmd.linkProvider.linkType)==PublicationLinkProvider.LinkType.PUBMED) {
    		return getPublication(cmd.link)
    	}
    	return null
    }

    boolean verifyLink(String linkTypeAsString, String link) {
    	PublicationLinkProvider linkType=PublicationLinkProvider.createCriteria().get() {
        	eq("linkType",PublicationLinkProvider.LinkType.valueOf(linkTypeAsString))
        }
        if (!linkType) {
        	return false
        }
        Pattern p = Pattern.compile(linkType.pattern);
        Matcher m = p.matcher(link);
        System.out.println(link+" ... "+linkType.pattern+" ... "+p.toString()+" ... "+m.matches())
        return m.matches()
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
        PublicationLinkProvider link=PublicationLinkProvider.createCriteria().get() {
        	eq("linkType",PublicationLinkProvider.LinkType.PUBMED)
        }
        Publication publication = new Publication(linkProvider: link, link: id)
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
