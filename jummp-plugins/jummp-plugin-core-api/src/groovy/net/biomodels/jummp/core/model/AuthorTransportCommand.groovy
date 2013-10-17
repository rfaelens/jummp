package net.biomodels.jummp.core.model
import org.codehaus.groovy.grails.validation.Validateable
/**
 * @short Wrapper for an Author to be transported through JMS.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
@grails.validation.Validateable	
class AuthorTransportCommand implements Serializable {
    private static final long serialVersionUID = 1L
    String lastName
    String firstName
    String initials
    static constraints = {
		//importFrom Author ...would have been nice :(
		lastName(nullable: false)
        firstName(nullable: true)
        initials(nullable: true)
	}	
}
