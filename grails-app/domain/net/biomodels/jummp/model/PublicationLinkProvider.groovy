/**
* Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI), Deutsches Krebsforschungszentrum (DKFZ)
*
* This file is part of Jummp.
*
* Jummp is free software; you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
*
* Jummp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License along with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
**/


package net.biomodels.jummp.model
import net.biomodels.jummp.core.model.PublicationLinkProviderTransportCommand

class PublicationLinkProvider implements Serializable {
	enum LinkType {
		PUBMED,
		ARXIV,
		DOI,
		ISBN,
		ISSN,
		JSTOR,
		NARCIS,
		NBN,
		PMC,
		CUSTOM
	}
	/* Type of the link */
	LinkType linkType
	/* Regular expression for the identifier */
	String pattern
	/* Identifiers.org prefix, if it exists */
	String identifiersPrefix
	
	static constraints = {
        linkType(nullable: false)
        pattern(nullable: false)
        identifiersPrefix(nullable: true)
    }
    
    PublicationLinkProviderTransportCommand toCommandObject() {
        return new PublicationLinkProviderTransportCommand(linkType:linkType.toString(),
                pattern: pattern,
                identifiersPrefix: identifiersPrefix
                )
    }

    static PublicationLinkProvider fromCommandObject(PublicationLinkProviderTransportCommand cmd) {
 	    PublicationLinkProvider link=PublicationLinkProvider.findByLinkType(cmd.linkType)
 	    if (link) {
 	    	return link
 	    }
    	return new PublicationLinkProvider(linkType:LinkType.valueOf(cmd.linkType),
    									   pattern:cmd.pattern,
    									   identifiersPrefix:cmd.identifiersPrefix)
    }
		
}
