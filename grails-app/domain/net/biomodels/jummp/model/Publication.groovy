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
    static hasMany = [models: Model]
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
        def authors=PublicationPerson.findAllByPublication(this, [sort: "position", order: "asc"])
    	authors.each {
    		PersonTransportCommand personAlias= it.person.toCommandObject();
    		personAlias.userRealName = it.pubAlias;
    		pubTC.authors.add(personAlias);
    	}
        return pubTC;
    }
    
    public static void reconcile(Publication publication, def tobeAdded) {
    	def existing = PublicationPerson.findAllByPublication(publication, [sort: "position", order: "asc"]);
    	tobeAdded.eachWithIndex { newAuthor, index ->
            	def existingAuthor = existing.find { oldAuthor ->
            		if (newAuthor.id) {
            			return newAuthor.id == oldAuthor.person.id
            		}
            		else if (newAuthor.orcid) {
            			return newAuthor.orcid == oldAuthor.person.orcid
            		}
            		return false
            	}
            	if (!existingAuthor) {
            		Person newlyCreatedPubAuthor;
            		if (newAuthor.orcid) {
            			def personWithSameOrcid=Person.findByOrcid(newAuthor.orcid)
            			if (personWithSameOrcid) {
            				newlyCreatedPubAuthor=personWithSameOrcid
            			}
            		}
            		if (!newlyCreatedPubAuthor) {
            			newlyCreatedPubAuthor = new Person(userRealName: newAuthor.userRealName, orcid: newAuthor.orcid)
            			newlyCreatedPubAuthor.save(failOnError: true, flush: true);
            		}
            		try {
            			def tmp=new PublicationPerson(publication: publication, 
            							  person: newlyCreatedPubAuthor,
            							  pubAlias: newAuthor.userRealName,
            							  position: index)
            			tmp.save(failOnError:true, flush: true);
            		}
            		catch(Exception e) {
            			e.printStackTrace();
            		}
            	}
            	else {
            		if (existingAuthor.position !=index) {
            			existingAuthor.position = index;
            			existingAuthor.save();
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
            publication.save(flush:true)
            reconcile(publication, cmd.authors)
            return publication
        }
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
                link: cmd.link
                )
        publ.save(failOnError: true, flush:true)
        reconcile(publ, cmd.authors)
    	return publ
    }
}
