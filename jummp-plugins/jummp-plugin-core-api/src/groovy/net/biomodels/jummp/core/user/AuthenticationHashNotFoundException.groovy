package net.biomodels.jummp.core.user

import org.springframework.security.core.AuthenticationException

/**
 * @short Exception thrown if an Authentication Hash is not found.
 *
 * A reason for this exception to be thrown is if the core already discarded an
 * Authentication Hash and a connected application is still using it.
 * 
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
public class AuthenticationHashNotFoundException extends AuthenticationException {
    public AuthenticationHashNotFoundException(String message) {
        super(message)
    }
}
