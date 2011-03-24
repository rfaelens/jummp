package net.biomodels.jummp.dbus

import org.freedesktop.dbus.DBusSerializable

/**
 * @short Wrapper around User for DBus.
 *
 * Does not wrap the authentication/password forgotten token.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 * @see net.biomodels.jummp.plugins.security.User
 */
class User implements DBusSerializable {
    Long id
    String username
    String password
    String userRealName
    String email
    Boolean enabled
    Boolean accountExpired
    Boolean accountLocked
    Boolean passwordExpired

    public User(Long id, String username, String password, String userRealName, String email, Boolean enabled, Boolean accountExpired, Boolean accountLocked, Boolean passwordExpired) {
        this.id = id
        this.username = username
        this.password = password
        this.userRealName = userRealName
        this.email = email
        this.enabled = enabled
        this.accountExpired = accountExpired
        this.accountLocked = accountLocked
        this.passwordExpired = passwordExpired
    }

    public User () {}

    public void deserialize(Long id, String username, String password, String userRealName, String email, Boolean enabled, Boolean accountExpired, Boolean accountLocked, Boolean passwordExpired) {
        this.id = id
        this.username = username
        this.password = password
        this.userRealName = userRealName
        this.email = email
        this.enabled = enabled
        this.accountExpired = accountExpired
        this.accountLocked = accountLocked
        this.passwordExpired = passwordExpired
    }

    public Object[] serialize() {
        Object[] returnVal = [id ? id : 0,
                username ? username : "",
                password ? password : "",
                userRealName ? userRealName : "",
                email ? email : "",
                enabled ? enabled : false,
                accountExpired ? accountExpired : false,
                accountLocked ? accountLocked : false,
                passwordExpired ? passwordExpired : false]
        return returnVal
    }

    /**
     * Creates a User from this Wrapper
     * @return A security User
     */
    public net.biomodels.jummp.plugins.security.User toUser() {
        net.biomodels.jummp.plugins.security.User user = new net.biomodels.jummp.plugins.security.User()
        user.id = this.id
        user.username = this.username
        user.password = this.password
        user.userRealName = this.userRealName
        user.email = this.email
        user.enabled = this.enabled
        user.accountExpired = this.accountExpired
        user.accountLocked = this.accountLocked
        user.passwordExpired = this.passwordExpired
        return user
    }

    /**
     * Creates a User wrapper from a net.biomodels.jummp.plugins.security.User
     * @param u The User to be wrapped
     * @return A DBusSerializable wrapper
     */
    public static User fromUser(net.biomodels.jummp.plugins.security.User u) {
        User user = new User()
        user.id = u.id
        user.username = u.username
        user.password = u.password
        user.userRealName = u.userRealName
        user.email = u.email
        user.enabled = u.enabled
        user.accountExpired = u.accountExpired
        user.accountLocked = u.accountLocked
        user.passwordExpired = u.passwordExpired
        return user
    }
}
