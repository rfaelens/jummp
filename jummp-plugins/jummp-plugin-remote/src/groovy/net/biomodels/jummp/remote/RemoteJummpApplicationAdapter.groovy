package net.biomodels.jummp.remote

import net.biomodels.jummp.core.JummpException
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException

/**
 * @short Interface describing the Application methods exported by remote adapters.
 *
 * Remote adapters have to implement this interface if they want to provide access
 * to JUMMP's core application functionality. This interface does not describe methods
 * for handling Users or Models, etc. Therefore there are specific interfaces. The only
 * purpose of this interface is to provide common methods to interact with core aspects
 * not matching into a more specific interface.
 * 
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
public interface RemoteJummpApplicationAdapter {
    /**
     * Tries to authenticate the given @p Authentication in the core.
     * @param authentication The Authentication to test. In most cases a UsernamePasswordAuthenticationToken
     * @return An authenticated user
     * @throws AuthenticationException If the Authentication is not valid
     * @throws JummpException If an error occurred
     */
    Authentication authenticate(Authentication authentication) throws AuthenticationException, JummpException
    /**
     * Retrieves the externalized configuration of the core application.
     * @param appToken The unique application token
     * @return The core's configuration
     */
    ConfigObject getJummpConfig(String appToken)
    /**
     * Retrieves a boolean from the core, indicating whether a user's
     * authentication hash is still valid or already removed.
     * @param authentication The authentication of a user
     * @return true if authentication is valid, false otherwise
     */
    boolean isAuthenticated(Authentication authentication)
}
