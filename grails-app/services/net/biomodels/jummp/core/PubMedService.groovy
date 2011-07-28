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

    /**
     * Downloads the XML describing the PubMed resource and parses the Publication information.
     * @param id The PubMed Identifier
     * @return A fully populated Publication
     */
    @Transactional
    private Publication fetchPublicationData(String id) throws JummpException {
        URL url
        try {
            url = new URL("http://www.ebi.ac.uk/citexplore/viewXML.do?externalId=${id}&dataSource=MED")
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
        // journal information
        if (slurper.Article.Journal.JournalIssue.PubDate.size() == 1) {
            if (slurper.Article.Journal.JournalIssue.PubDate.Year.size() == 1) {
                publication.year = slurper.Article.Journal.JournalIssue.PubDate.Year[0].text() as Integer
            }
            if (slurper.Article.Journal.JournalIssue.PubDate.Month.size() == 1) {
                publication.month = slurper.Article.Journal.JournalIssue.PubDate.Month[0].text()
            }
            if (slurper.Article.Journal.JournalIssue.PubDate.Day.size() == 1) {
                publication.day = slurper.Article.Journal.JournalIssue.PubDate.Day[0]?.text() as Integer
            }
        }
        if (slurper.Article.Journal.JournalIssue.Volume.size() == 1) {
            try {
                publication.volume = slurper.Article.Journal.JournalIssue.Volume[0].text() as Integer
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        if (slurper.Article.Journal.JournalIssue.Issue.size() == 1) {
            try {
                publication.issue = slurper.Article.Journal.JournalIssue.Issue[0].text() as Integer
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        if (slurper.Article.Pagination.MedlinePgn.size() == 1) {
            publication.pages = slurper.Article.Pagination.MedlinePgn[0].text()
        }
        if (slurper.MedlineJournalInfo.MedlineTA.size() == 1) {
            publication.journal = slurper.MedlineJournalInfo.MedlineTA[0].text()
        }

        if (slurper.Article.ArticleTitle.size() == 1) {
            publication.title = slurper.Article.ArticleTitle.text()
        }
        if (slurper.Article.Affiliation.size() == 1) {
            publication.affiliation = slurper.Article.Affiliation.text()
        }
        if (slurper.Article.Abstract.AbstractText.size() > 0) {
            publication.synopsis = ''
            slurper.Article.Abstract.AbstractText.each {
                if (it.@Label.size() == 1) {
                    publication.synopsis += it.@Label.text() + "\n"
                }
                publication.synopsis += it.text()
            }
            if (publication.synopsis.length() > 1000) {
                publication.synopsis = publication.synopsis.substring(0, 999) + "…"
            }
        }

        parseAuthors(slurper, publication)

        if (!publication.validate()) {
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
        for (def authorXml in slurper.Article.AuthorList.Author) {
            Author author = new Author()
            author.lastName = authorXml.LastName[0].text()
            author.firstName = authorXml.ForeName[0].text()
            author.initials = authorXml.Initials[0].text()
            author.save()
            publication.addToAuthors(author)
        }
    }
}
