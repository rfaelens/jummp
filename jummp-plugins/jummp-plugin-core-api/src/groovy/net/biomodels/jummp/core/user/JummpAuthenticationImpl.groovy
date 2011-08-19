package net.biomodels.jummp.core.user

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority

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
        auth.authenticationHash = authenticationHash
        return auth
    }
}
