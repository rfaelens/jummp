package net.biomodels.jummp.core

import org.springframework.security.core.Authentication
import net.biomodels.jummp.core.user.AuthenticationHashNotFoundException

/**
 * @short Service Interface for hashing Authentications.
 *
 * This service allows to hash and store an Authentication. After an Authentication
 * has been hashed it can be retrieved with the help of the unique identifier.
 *
 * The Service has an internal bookkeeping of the Authentications. Whenever an Authentication
 * is retrieved the lifetime of the Authentication is increased. When the lifetime
 * has expired the Service may remove the Authentication. Further retrievals with the hash
 * will fail.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
public interface IAuthenticationHashService {
    /**
     * Inserts the given @auth into the internal hash and returns a unique hash identifier.
     * The hash identifier is an MD5 sum of the Authentication's username, a random number
     * and the timestamp of insertion. The idea is that the hash can be passed around in
     * the open without the chance to extract information on which user it identifies.
     * @param auth The Authentication to hash
     * @return A unique hash identifier which can be used to retrieve the Authentication
     * @see retrieveAuthentication
     */
    String hashAuthentication(Authentication auth)
    /**
     * Returns the Authentication for the @p hash.
     * @param hash The unique identifier for the Authentication
     * @return Authentication identified by the hash
     * @throws AuthenticationHashNotFoundException Thrown if the internal Authentication hash does not contain @p hash.
     */
    Authentication retrieveAuthentication(String hash) throws AuthenticationHashNotFoundException
    /**
     * Checks, if an authentication exists or if it has been removed by checkAuthenticationExpired().
     * @param hash The hash of the authentication to be checked
     * @return True if authentication exists, false otherwise
     */
    boolean isAuthenticated(String hash)
}
