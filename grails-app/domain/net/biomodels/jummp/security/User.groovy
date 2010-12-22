package net.biomodels.jummp.security

class User {

    String username
    String password
    String userRealName
    String email
    boolean enabled
    boolean accountExpired
    boolean accountLocked
    boolean passwordExpired

    static constraints = {
        username(blank: false, unique: true)
        password(blank: false)
        userRealName(blank: false)
        email(email: true)
    }

    static mapping = {
        password(column: '`password`')
    }

    Set<Role> getAuthorities() {
        UserRole.findAllByUser(this).collect { it.role } as Set
    }
}
