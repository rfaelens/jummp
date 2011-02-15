package net.biomodels.jummp.model

import net.biomodels.jummp.core.model.PublicationLinkProvider
import net.biomodels.jummp.core.model.PublicationTransportCommand
import net.biomodels.jummp.core.model.AuthorTransportCommand

/**
 * @short Representation for a Publication.
 * A publication is used by a Model to reference the meta information
 * about the paper the Model belongs to.
 * @see Model
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class Publication {
    /**
     * A Publication is part of a Model.
     */
    static belongsTo = [Model]
    static hasMany = [authors: Author, models: Model]
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

    static constraints = {
        // TODO: do we need more than 250 characters?
        journal(nullable: false, blank: false)
        // TODO: do we need more than 250 characters?
        title(nullable: false, blank: false)
        // TODO: do we need more than 250 characters?
        affiliation(nullable: false, blank: false)
        // TODO: How long can an abstract be? Are 1000 characters sufficient?
        synopsis(nullable: false, blank: true, maxSize: 1000)
        year(nullable: true)
        month(nullable: true)
        day(nullable: true)
        volume(nullable: true)
        issue(nullable: true)
        pages(nullable: true)
        linkProvider(nullable: true)
        link(nullable: true)
    }

    PublicationTransportCommand toCommandObject() {
        List<AuthorTransportCommand> authorCmds = []
        authors.each { author ->
            authorCmds << author.toCommandObject()
        }
        return new PublicationTransportCommand(journal: journal,
                title: title,
                affiliation: affiliation,
                synopsis: synopsis,
                year: year,
                month: month,
                day: day,
                volume: volume,
                issue: issue,
                pages: pages,
                linkProvider: linkProvider,
                link: link,
                authors: authorCmds)
    }

    static Publication fromCommandObject(PublicationTransportCommand cmd) {
        return new Publication(journal: cmd.journal,
                title: cmd.title,
                affiliation: cmd.affiliation,
                synopsis: cmd.synopsis,
                year: cmd.year,
                month: cmd.month,
                day: cmd.day,
                volume: cmd.volume,
                issue: cmd.issue,
                pages: cmd.pages,
                linkProvider: cmd.linkProvider,
                link: cmd.link
                // TODO: authors
                )

    }
}
