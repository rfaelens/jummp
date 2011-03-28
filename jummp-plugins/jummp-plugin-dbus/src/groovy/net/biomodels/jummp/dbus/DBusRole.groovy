package net.biomodels.jummp.dbus

import org.freedesktop.dbus.DBusSerializable
import net.biomodels.jummp.plugins.security.Role

/**
 * @short Wrapper around Role for DBus.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 * @see net.biomodels.jummp.plugins.security.Role
 */
class DBusRole implements DBusSerializable {
    Long id
    String authority

    public DBusRole(Long id, String authority) {
        this.id = id
        this.authority = authority
    }

    public DBusRole() {}

    public void deserialize(Long id, String authority) {
        this.id = id
        this.authority = authority
    }

    public Object[] serialize() {
        Object[] returnVal = [id ? id : 0, authority ? authority : ""]
        return returnVal
    }

    /**
     * Creates a Role from this Wrapper
     * @return A security Role
     */
    public Role toRole() {
        Role role = new Role()
        role.authority = this.authority
        role.id = this.id
        return role
    }

    /**
     * Creates a Role wrapper from a net.biomodels.jummp.plugins.security.Role
     * @param r The Role to be wrapped
     * @return A DBusSerializable wrapper
     */
    public static DBusRole fromRole(Role r) {
        DBusRole role = new DBusRole()
        role.id = r.id
        role.authority = r.authority
        return role
    }
}
