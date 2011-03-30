package net.biomodels.jummp.core.user

import org.codehaus.groovy.grails.plugins.springsecurity.GrailsUser
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import net.biomodels.jummp.plugins.security.SerializableGrailsUser

/**
 * @short Concrete Implementation of the JummpAuthentication.
 *
 * This Authentication is a small wrapper for a normal Authentication extended by
 * the AuthenticationHash.
 *
 * To create an instance of this class use the static factory method @link fromAuthentication.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
final class JummpAuthenticationImpl extends AbstractAuthenticationToken implements JummpAuthentication {
    private static final long serialVersionUID = 1L
    def principal
    private String authenticationHash

    private JummpAuthenticationImpl() {}
    private JummpAuthenticationImpl(Map map) {}

    private JummpAuthenticationImpl(Collection<GrantedAuthority> authorities) {
        super(authorities)
    }

    Object getCredentials() {
        return null
    }

    Object getPrincipal() {
        return principal
    }

    def String getAuthenticationHash() {
        return authenticationHash
    }

    /**
     * Creates an instance of this class from the passed @p authentication and @p authenticationHash.
     * @param authentication The Authentication used as source
     * @param authenticationHash The hash value for this Authentication
     * @return an instance of JummpAuthentication
     */
    public static JummpAuthentication fromAuthentication(Authentication authentication, String authenticationHash) {
        JummpAuthenticationImpl auth = new JummpAuthenticationImpl(authentication.getAuthorities())
        auth.setAuthenticated(authentication.isAuthenticated())
        auth.setDetails(authentication.getDetails())
        auth.principal = authentication.getPrincipal()
        if (auth.principal instanceof GrailsUser) {
            // The authentication is propagated with an GrailsUser as principal
            // Unfortunately the GrailsUser class is not serializable.
            // Because of that we transform to a serializable GrailsUser
            auth.principal = SerializableGrailsUser.fromGrailsUser((GrailsUser)auth.principal)
        }
        auth.authenticationHash = authenticationHash
        return auth
    }
}
