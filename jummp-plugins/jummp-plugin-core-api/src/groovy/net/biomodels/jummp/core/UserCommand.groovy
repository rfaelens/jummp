package net.biomodels.jummp.core

import grails.validation.Validateable

/**
 * Command Object for validating User settings
 * @author Jochen Schramm <j.schramm@dkfz-heidelberg.de>
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
@Validateable
class UserCommand implements Serializable {
    private static final long serialVersionUID = 1L
    String username
    String password
    String rePassword
    String userRealName
    String email

    static constraints = {
        username( blank: false)
        password( blank: false)
        rePassword( validator: { val, obj ->
            return obj.password == val
        })
        userRealName(blank: false)
        email(email: true)
    }
}
