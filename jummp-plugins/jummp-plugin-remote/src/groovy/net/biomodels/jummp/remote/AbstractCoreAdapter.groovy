package net.biomodels.jummp.remote

import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.authority.GrantedAuthorityImpl
import org.springframework.security.core.context.SecurityContextHolder
import net.biomodels.jummp.core.user.AuthenticationHashNotFoundException
import net.biomodels.jummp.core.IAuthenticationHashService

/**
 * @short Abstract Base class for all Adapters exporting core functionality.
 *
 * This abstract class bundles all the methods required by all adapters which
 * export parts of the Core API. This means mostly methods to interact with the
 * Authentication.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
abstract class AbstractCoreAdapter {
    /**
     * An anonymous authentication
     */
    private static final ANONYMOUS_AUTH = new AnonymousAuthenticationToken("key", "anonymousUser", [new GrantedAuthorityImpl("ROLE_ANONYMOUS")])
    /**
     * Dependency injection of AuthenticationHashService
     */
    protected IAuthenticationHashService authenticationHashService

    /**
     * Helper function to set the Authentication in the current thread
     * @param authentication
     */
    protected void setAuthentication(String authenticationHash) throws AuthenticationHashNotFoundException {
        SecurityContextHolder.clearContext()
        SecurityContextHolder.context.setAuthentication(authenticationHashService.retrieveAuthentication(authenticationHash))
    }

    /**
     * Helper function to set an anonymous Authentication in the current thread
     */
    protected void setAnonymousAuthentication() {
        SecurityContextHolder.clearContext()
        SecurityContextHolder.context.setAuthentication(ANONYMOUS_AUTH)
    }

    /**
     * Helper function to remove the Authentication from current thread.
     */
    protected void restoreAuthentication() {
        SecurityContextHolder.clearContext()
    }

    /**
     * Setter for Dependency Injection of AuthenticationHashService.
     * @param authenticationHashService
     */
    public void setAuthenticationHashService(IAuthenticationHashService authenticationHashService) {
        this.authenticationHashService = authenticationHashService
    }
}
