package net.biomodels.jummp.dbus

import org.freedesktop.dbus.DBusSerializable
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.GrantedAuthorityImpl
import net.biomodels.jummp.core.user.JummpAuthentication

/**
 * @short DBus Wrapper for an Authentication.
 *
 * This Authentication is extended by a unique hash identifying the Authentication in core.
 * The Authorities are mapped in a List of Strings and getAuthorities converts them into
 * GrantedAuthorityImpl objects.
 *
 * The Authentication is written in a way that it can be used also in a remote application.
 * Please note that it always returns @c true for isAuthenticated. You should only construct
 * an instance of this class to transport an Authentication over DBus to a different application
 * part. Don't use instances of this class in the core application!
 * 
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class DBusAuthentication implements DBusSerializable, JummpAuthentication {
    private static final long serialVersionUID = 1L
    String username
    String hash
    List<String> roles = []

    public DBusAuthentication() {}

    public DBusAuthentication(String username) {
        this.username = username
    }

    Collection<GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = []
        roles.each {
            authorities << new GrantedAuthorityImpl(it)
        }
        return authorities
    }

    Object getCredentials() {
        // not used
        return null
    }

    Object getDetails() {
        // TODO: unique identifier of authentication
        return null
    }

    Object getPrincipal() {
        return username
    }

    boolean isAuthenticated() {
        return true
    }

    void setAuthenticated(boolean b) {
        if (b) {
            throw new IllegalArgumentException("Not allowed")
        }
    }

    String getName() {
        return username
    }

    Object[] serialize() {
        Object[] returnVal = new Object[3]
        returnVal[0] = username
        returnVal[1] = hash
        returnVal[2] = roles
        return returnVal
    }

    public void deserialize(String username, String hash, List<String> roles) {
        this.username = username
        this.hash = hash
        this.roles = roles
    }

    public String getAuthenticationHash() {
        return this.hash
    }

    public static DBusAuthentication fromAuthentication(Authentication auth) {
        DBusAuthentication dbusAuth = new DBusAuthentication(auth.getName())
        auth.authorities.each {
            dbusAuth.roles << it.authority
        }
        return dbusAuth
    }
}
