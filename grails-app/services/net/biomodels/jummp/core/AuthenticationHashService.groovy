package net.biomodels.jummp.core

import org.springframework.security.core.Authentication
import net.biomodels.jummp.core.user.AuthenticationHashNotFoundException
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.authority.GrantedAuthorityImpl
import org.codehaus.groovy.grails.commons.ConfigurationHolder

/**
 * @short Service implementing the IAuthenticationHashService
 *
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class AuthenticationHashService implements IAuthenticationHashService {
    /**
     * Private class describing a hash entry in the authentication Map.
     */
    private class AuthenticationHashEntry {
        /**
         * Last access to this entry.
         */
        Date timeStamp
        /**
         * The Authentication represented by this entry.
         */
        Authentication authentication
        AuthenticationHashEntry(Date timeStamp, Authentication auth) {
            this.timeStamp = timeStamp
            this.authentication = auth
        }
    }
    private static final ANONYMOUS_AUTH = new AnonymousAuthenticationToken("key", "anonymousUser", [new GrantedAuthorityImpl("ROLE_ANONYMOUS")])

    static transactional = true
    /**
     * Random number generator for creating the hash values.
     */
    private final Random random = new Random(System.currentTimeMillis())
    /**
     * Map of the Authentications and their Ids.
     */
    @SuppressWarnings('GrailsStatelessService')
    private Map<String, AuthenticationHashEntry> authentications = [:]

    String hashAuthentication(Authentication auth) {
        String hash = random.nextInt() + auth.name + System.currentTimeMillis()
        hash = hash.encodeAsMD5()
        authentications.put(hash, new AuthenticationHashEntry(new Date(), auth))
        return hash
    }

    Authentication retrieveAuthentication(String hash) throws AuthenticationHashNotFoundException {
        if (hash == "anonymous") {
            return AuthenticationHashService.ANONYMOUS_AUTH
        }
        if (authentications.containsKey(hash)) {
            AuthenticationHashEntry entry = authentications[hash]
            entry.timeStamp = new Date()
            return entry.authentication
        } else {
            throw new AuthenticationHashNotFoundException("No Authentication for Hash " + hash)
        }
    }

    void checkAuthenticationExpired() {
        long time = Long.valueOf(ConfigurationHolder.config.jummp.authenticationHash.maxInactiveTime)
        long maxInactiveTime = new Date().getTime() - time
        authentications.each { user, hash ->
            long timeStamp = hash.timeStamp.getTime()
            if (timeStamp < maxInactiveTime) {
                authentications.remove(user)
            }
        }
    }

    boolean isAuthenticated(String hash) {
        return authentications.containsKey(hash)
    }
}