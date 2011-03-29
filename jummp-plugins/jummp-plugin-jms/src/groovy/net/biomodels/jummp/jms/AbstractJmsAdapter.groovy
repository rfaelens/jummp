package net.biomodels.jummp.jms

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.Authentication
import net.biomodels.jummp.core.IAuthenticationHashService
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.authority.GrantedAuthorityImpl
import net.biomodels.jummp.core.user.AuthenticationHashNotFoundException

/**
 * @short Abstract base class for all Jms Adapter.
 *
 * This class provides the common base for all individual JMS Adapters such as
 * verification of message and setting and restoring the Authentication.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
abstract class AbstractJmsAdapter {
    /**
     * Dependency injection of AuthenticationHashService
     */
    protected IAuthenticationHashService authenticationHashService
    /**
     * Helper function to verify that @p message has correct structure.
     * @param message The message to verify
     * @param classes The structure as List of Class types.
     * @return @c true, if the message structure is valid, @c false otherwise
     */
    protected boolean verifyMessage(def message, List<Class<?>> classes) {
        if (!(message instanceof List)) {
            return false
        }
        if (message.size() != classes.size()) {
            return false
        }
        for (int i=0; i<classes.size(); i++) {
            Class clazz = classes[i]
            if (!clazz.isInstance(message[i])) {
                return false
            }
        }
        return true
    }

    /**
     * Helper function to set the Authentication in the current thread
     * @param authentication
     */
    protected void setAuthentication(String authenticationHash) throws AuthenticationHashNotFoundException {
        SecurityContextHolder.clearContext()
        SecurityContextHolder.context.setAuthentication(authenticationHashService.retrieveAuthentication(authenticationHash))
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
