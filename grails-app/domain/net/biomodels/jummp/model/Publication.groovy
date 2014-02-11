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
**/





package net.biomodels.jummp.model

import net.biomodels.jummp.core.model.PublicationTransportCommand
import net.biomodels.jummp.plugins.security.PersonTransportCommand
import net.biomodels.jummp.plugins.security.Person

/**
 * @short Representation for a Publication.
 * A publication is used by a Model to reference the meta information
 * about the paper the Model belongs to.
 * @see Model
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class Publication implements Serializable {
    /**
     * A Publication is part of a Model.
     */
    static belongsTo = [Model]
    static hasMany = [authors: Person, models: Model]
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
        // TODO: How long can an abstract be? Are 5000 characters sufficient?
        synopsis(nullable: false, maxSize: 5000)
        year(nullable: true)
        month(nullable: true)
        day(nullable: true)
        volume(nullable: true)
        issue(nullable: true)
        pages(nullable: true)
        linkProvider(nullable: true)
        link(nullable: true)
        authors validator: { authorValue, pubObj ->
        	return authorValue && !authorValue.isEmpty()
        }
    }

    PublicationTransportCommand toCommandObject() {
       PublicationTransportCommand pubTC=new PublicationTransportCommand(journal: journal,
                title: title,
                affiliation: affiliation,
                synopsis: synopsis,
                year: year,
                month: month,
                day: day,
                volume: volume,
                issue: issue,
                pages: pages,
                linkProvider: linkProvider.toCommandObject(),
                link: link,
                authors: new LinkedList<PersonTransportCommand>())
        authors.each {
        	pubTC.authors.add(it.toCommandObject())
        }
        return pubTC;
    }

    private static void reconcile(List authors, List tobeAdded) {
    	tobeAdded.each { newAuthor ->
            	Person existing = authors.find { oldAuthor ->
            		if (newAuthor.orcid) {
            			return newAuthor.orcid == oldAuthor.orcid
            		}
            		return false
            	}
            	if (!existing) {
            		if (newAuthor.orcid) {
            			existing=Person.findByOrcid(newAuthor.orcid)
            			if (existing && existing.userRealName==newAuthor.userRealName) {
            				authors<<existing
            			}
            			if (existing) {
            				log.error "Received duplicate ORCID for ${existing.userRealName} (in the repository) and ${newAuthor.userRealName}. Please reconcile."
            			}
            		}
            		else {
            			Person current = new Person(userRealName: newAuthor.userRealName, orcid: newAuthor.orcid)
            			authors << current
            			current.save()
            		}
            	}
         }
    }
    
    static Publication fromCommandObject(PublicationTransportCommand cmd) {
        Publication publication = Publication.createCriteria().get() {
    		eq("link",cmd.link)
    		linkProvider {
    			eq("linkType",PublicationLinkProvider.LinkType.valueOf(cmd.linkProvider.linkType))
    		}
    	}
    	if (publication) {
    		publication.title=cmd.title;
        	publication.affiliation=cmd.affiliation;
            publication.synopsis=cmd.synopsis;
            publication.journal=cmd.journal;
            publication.year=cmd.year;
            publication.month=cmd.month;
            publication.day=cmd.day;
            publication.volume=cmd.volume;
            publication.issue=cmd.issue;
            publication.pages=cmd.pages;
            reconcile(publication.authors, cmd.authors)
            publication.save(flush:true)
            return publication
        }
    	List<Person> authors = []
        reconcile(authors, cmd.authors)
    	Publication publ=new Publication(journal: cmd.journal,
                title: cmd.title,
                affiliation: cmd.affiliation,
                synopsis: cmd.synopsis,
                year: cmd.year,
                month: cmd.month,
                day: cmd.day,
                volume: cmd.volume,
                issue: cmd.issue,
                pages: cmd.pages,
                linkProvider: PublicationLinkProvider.fromCommandObject(cmd.linkProvider),
                link: cmd.link,
                authors: authors
                )
        publ.save(flush:true)
        return publ
    }
}
