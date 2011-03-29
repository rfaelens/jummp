package net.biomodels.jummp.core.user

import org.springframework.security.core.Authentication

/**
 * @short Interface describing JUMMP additions to an Authentication.
 *
 * JUMMP generates a unique hash for each authenticated Authentication. This
 * interface describes the method to access this hash value.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
public interface JummpAuthentication extends Authentication {
    /**
     *
     * @return The hash value of the Authentication in JUMMP core
     */
    public String getAuthenticationHash()
}
