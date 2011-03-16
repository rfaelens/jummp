package net.biomodels.jummp.core.user

import net.biomodels.jummp.core.JummpException

/**
 * @short Exception indicating that a Role could not be found.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class RoleNotFoundException extends JummpException implements Serializable {
    private static final long serialVersionUID = 1L
    Long id = null
    String authority = null

    RoleNotFoundException(Long id) {
        super("Role with id ${id} not found".toString())
        this.id = id
    }

    RoleNotFoundException(String authority) {
        super("Role with authority ${authority} not found".toString())
        this.authority = authority
    }
}
