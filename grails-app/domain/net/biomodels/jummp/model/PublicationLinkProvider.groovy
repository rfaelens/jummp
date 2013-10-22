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
