package net.biomodels.jummp.core.model
import org.codehaus.groovy.grails.validation.Validateable
/**
 * @short Wrapper for a Publciation to be transported through JMS.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
@grails.validation.Validateable	
class PublicationTransportCommand implements Serializable {
    private static final long serialVersionUID = 1L
    /**
     * Name of the journal where the publication has been published
     */
    String journal
    /**
     * The title of the publication.
     */
    String title
    /**
     * The authors' affiliation.
     */
    String affiliation
    /**
     * The abstract of the publication.
     */
    String synopsis
    // TODO: merge date fields into one property and store a format property (only year, year/month, complete date)
    /**
     * The year the Journal issue has been published.
     */
    Integer year
    /**
     * The month the Journal issue has been published.
     */
    String month
    /**
     * The day the Journal issue has been published.
     */
    Integer day
    /**
     * The volume of the Journal issue.
     */
    Integer volume
    /**
     * The issue of the Journal the publication has been published in.
     */
    Integer issue
    /**
     * The pages of the publication in the Journal Issue.
     */
    String pages
    /**
     * The provider of the publication id (e.g. PubMed)
     */
    PublicationLinkProvider linkProvider
    /**
     * The key to the publication at the linkProvider or a URL
     */
    String link
    List<AuthorTransportCommand> authors
    static constraints = {
    	// importFrom Publication ...would have been nice :(
    	// TODO: do we need more than 250 characters?
        journal(nullable: false, blank: false)
        // TODO: do we need more than 250 characters?
        title(nullable: false, blank: false)
        // TODO: do we need more than 250 characters?
        affiliation(nullable: false, blank: false)
        // TODO: How long can an abstract be? Are 5000 characters sufficient?
        synopsis(nullable: false, blank: true, maxSize: 5000)
        year(nullable: true)
        month(nullable: true)
        day(nullable: true)
        volume(nullable: true)
        issue(nullable: true)
        pages(nullable: true)
    }
}
