package net.biomodels.jummp.dbus.model

import net.biomodels.jummp.core.model.PublicationTransportCommand
import org.freedesktop.dbus.DBusSerializable
import net.biomodels.jummp.core.model.PublicationLinkProvider
import net.biomodels.jummp.core.model.AuthorTransportCommand

/**
 * @short DBusWrapper for a PublicationTransportCommand.
 *
 * This class actually extends the PublicationTransportCommand, so it can be used to be
 * passed to the core.
 *
 * Authors are included in the serialization as a list of Strings with Author fields
 * separated by new lines.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class DBusPublication extends PublicationTransportCommand implements DBusSerializable {

    public DBusPublication() {}

    Object[] serialize() {
        Object returnVal = new Object[13]
        returnVal[0] = this.journal ? this.journal : ""
        returnVal[1] = this.title ? this.title : ""
        returnVal[2] = this.affiliation ? this.affiliation : ""
        returnVal[3] = this.synopsis ? this.synopsis : ""
        returnVal[4] = this.year ? this.year : 0
        returnVal[5] = this.month ? this.month : ""
        returnVal[6] = this.day ? this.day : 0
        returnVal[7] = this.volume ? this.volume : 0
        returnVal[8] = this.issue ? this.issue : 0
        returnVal[9] = this.pages ? this.pages : ""
        returnVal[10] = this.linkProvider ? this.linkProvider.toString() : ""
        returnVal[11] = this.link ? this.link : ""
        List<String> authorStrings = []
        for (AuthorTransportCommand author in this.authors) {
            authorStrings << author.lastName + "\n" + author.firstName + "\n" + author.initials

        }
        returnVal[12] = authorStrings
        return returnVal
    }

    void deserialize(String journal, String title, String affiliation, String synopsis, int year, String month, int day, int volume, int issue, String pages, String linkProvider, String link, List<String> authors) {
        if (journal != "") {
            this.journal = journal
        }
        if (title != "") {
            this.title = title
        }
        if (affiliation != "") {
            this.affiliation = affiliation
        }
        if (synopsis != "") {
            this.synopsis = synopsis
        }
        if (year != 0) {
            this.year = year
        }
        if (month != "") {
            this.month = month
        }
        if (day != 0) {
            this.day = day
        }
        if (volume != 0) {
            this.volume = volume
        }
        if (issue != 0) {
            this.issue = issue
        }
        if (pages != "") {
            this.pages = pages
        }
        if (linkProvider != "") {
            this.linkProvider = PublicationLinkProvider.valueOf(PublicationLinkProvider.class, linkProvider)
        }
        if (link != "") {
            this.link = link
        }
        this.authors = []
        for (String author in authors) {
            List<String> authorParts = author.readLines()
            if (authorParts.size() == 3) {
                this.authors << new AuthorTransportCommand(lastName: authorParts[0], firstName: authorParts[1], initials: authorParts[2])
            }
        }
    }

    /**
     * Creates a new instance of this class from a PublicationTransportCommand.
     * @param publication The publication to use as base
     * @return New instance based on passed in publication.
     */
    static public DBusPublication fromPublicationTransportCommand(PublicationTransportCommand publication) {
        return new DBusPublication(journal: publication.journal,
                title: publication.title,
                affiliation: publication.affiliation,
                synopsis: publication.synopsis,
                year: publication.year,
                month: publication.month,
                day: publication.day,
                volume: publication.volume,
                issue: publication.issue,
                pages: publication.pages,
                linkProvider: publication.linkProvider,
                link: publication.link,
                authors: publication.authors
        )
    }
}
