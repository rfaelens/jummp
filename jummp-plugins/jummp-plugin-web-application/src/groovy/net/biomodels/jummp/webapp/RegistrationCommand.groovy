package net.biomodels.jummp.webapp
import net.biomodels.jummp.plugins.security.User
/**
 * @short Command object for User registration
 */
@grails.validation.Validateable
class RegistrationCommand {
    String username
    String email
    String userRealName
    String institution
    String orcid

    static constraints = {
        username(nullable: false, blank: false)
        email(nullable: false, email: true, blank: false)
        userRealName(nullable: false, blank: false)
        institution(nullable:true)
        orcid(nullable:true)
    }

    User toUser() {
        return new User(username: this.username, email: this.email, userRealName: this.userRealName, institution:this.institution, orcid:this.orcid)
    }
}
