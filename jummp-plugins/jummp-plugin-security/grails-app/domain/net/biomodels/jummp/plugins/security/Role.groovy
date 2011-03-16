package net.biomodels.jummp.plugins.security

/**
 * This class has been aut-generated by the Spring Security Core plugin.
 * See http://burtbeckwith.github.com/grails-spring-security-core/
 */
class Role implements Serializable {
    private static final long serialVersionUID = 1L

    String authority

    static mapping = {
        cache true
    }

    static constraints = {
        authority blank: false, unique: true
    }
}
