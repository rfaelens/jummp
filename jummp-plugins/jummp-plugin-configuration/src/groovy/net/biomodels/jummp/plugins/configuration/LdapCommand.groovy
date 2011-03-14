package net.biomodels.jummp.plugins.configuration

/**
 * Command Object for validating LDAP settings.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class LdapCommand implements Serializable {
    private static final long serialVersionUID = 1L
    String ldapServer
    String ldapManagerDn
    String ldapManagerPassword
    String ldapSearchBase
    String ldapSearchFilter
    Boolean ldapSearchSubtree

    static constraints = {
        ldapServer(nullable: false, blank: false)
        ldapManagerDn(nullable: false, blank: false)
        ldapManagerPassword(nullable: false, blank: false)
        ldapSearchBase(nullable: false, blank: true)
        ldapSearchFilter(nullable: false, blank: true)
        ldapSearchSubtree(nullable: false)
    }
}
