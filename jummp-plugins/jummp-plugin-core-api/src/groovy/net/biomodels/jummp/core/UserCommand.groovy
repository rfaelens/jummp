package net.biomodels.jummp.core

/**
 * Command Object for validating User settings
 * @author Jochen Schramm <j.schramm@dkfz-heidelberg.de>
 */
class UserCommand implements Serializable {
    private static final long serialVersionUID = 1L
    String username
    String password
    String userRealName
    String email

    static constraints = {
        username( blank: false)
        password( blank: false)
        userRealName(blank: false)
        email(email: true)
    }
}
