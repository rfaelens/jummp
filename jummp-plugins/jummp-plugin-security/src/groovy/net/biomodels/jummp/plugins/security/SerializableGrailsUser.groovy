package net.biomodels.jummp.plugins.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User
import org.codehaus.groovy.grails.plugins.springsecurity.GrailsUser

/**
 * @short A serializable version of GrailsUser.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class SerializableGrailsUser extends User implements Serializable {
    private static final long serialVersionUID = 1L
    private final Object _id

    SerializableGrailsUser(String username, String password, boolean enabled, boolean accountNonExpired,
    boolean credentialsNonExpired, boolean accountNonLocked,
    Collection<GrantedAuthority> authorities, Object id) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired,
                accountNonLocked, authorities)
        _id = id
    }

    Object getId() { _id }

    /**
     * Creates a SerializableGrailsUser from a GrailsUser
     * @param user The GrailsUser
     * @return new instance of SerializableGrailsUser
     */
    public static SerializableGrailsUser fromGrailsUser(GrailsUser user) {
        return new SerializableGrailsUser(user.username, user.password, user.enabled, user.accountNonExpired,
                user.credentialsNonExpired, user.accountNonLocked, user.authorities, user.id)
    }
}
