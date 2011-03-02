package net.biomodels.jummp.webapplication

/**
 * Small TagLib to render custom tags.
 * @author Martin Gräßlin <m.graesslin@dkfz.de>
 */
class JummpTagLib {
    static namespace = "jummp"

    /**
     * Renders a compact title of a publication.
     * Format: Journal Year Month; Volume (Issue) Pages
     * All fields are optional.
     * @param publication The PublicationTransportCommand
     */
    def compactPublicationTitle = { attrs ->
        def publication = attrs.publication
        String publicationSummary = publication.journal
        if (publication.year) {
            publicationSummary += " " + publication.year
            if (publication.month) {
                publicationSummary += " " + publication.month
            }
        }
        if (publication.volume) {
            publicationSummary += "; " + publication.volume
        }
        if (publication.issue) {
            publicationSummary += "(" + publication.issue + ")"
        }
        if (publication.pages) {
            publicationSummary += ": " + publication.pages
        }
        out << publicationSummary
    }

    /**
     * Renders a span element with an alert icon and initially hidden.
     */
    def errorField = { attrs ->
        out << "<span class=\"ui-icon ui-icon-alert\" style=\"display: none\"></span>"
    }
}
