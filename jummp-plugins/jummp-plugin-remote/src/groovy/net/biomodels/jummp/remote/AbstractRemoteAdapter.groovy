package net.biomodels.jummp.remote

import net.biomodels.jummp.core.user.JummpAuthentication
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder

/**
 * @short Base class for all Remote Adapters.
 *
 * Implements methods required by all remote adapters.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
abstract class AbstractRemoteAdapter {

    /**
     *
     * @return The Authentication Hash of the current Authentication.
     */
    protected String authenticationToken() {
        Authentication auth = SecurityContextHolder.context.authentication
        if (auth instanceof AnonymousAuthenticationToken) {
            return "anonymous"
        } else if (auth instanceof JummpAuthentication) {
            return ((JummpAuthentication)auth).getAuthenticationHash()
        } else {
            return ""
        }
    }
}
