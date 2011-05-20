package net.biomodels.jummp.dbus

import grails.converters.JSON
import org.springframework.security.core.context.SecurityContextHolder
import net.biomodels.jummp.core.model.RevisionTransportCommand
import net.biomodels.jummp.core.user.AuthenticationHashNotFoundException
import net.biomodels.jummp.dbus.authentication.AuthenticationHashNotFoundDBusException

/**
 * @short Helper class for the SbmlDBusAdapter.
 *
 * The SbmlDBusAdapter needs to use the same code over and over. This class simplifies the
 * maintenance of the adapter by using some groovy way of writing code.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class SbmlDBusAdapterHelperImpl implements SbmlDBusAdapterHelper {
    /**
     * Dependency Injection of sbmlService
     */
    def sbmlService
    /**
     * Dependency Injection of authenticationHashService
     */
    def authenticationHashService
    /**
     * Dependency Injection of modelDelegateService
     */
    def modelDelegateService

    def perform = { name, authenticationHash, modelId, revisionNumber, id ->
        try {
            SecurityContextHolder.clearContext()
            SecurityContextHolder.context.setAuthentication(authenticationHashService.retrieveAuthentication(authenticationHash))

            RevisionTransportCommand revision = modelDelegateService.getRevision(modelId, revisionNumber)
            if (id) {
                return sbmlService."get${name}"(revision, id)
            } else {
                return sbmlService."get${name}"(revision)
            }
        } catch (AuthenticationHashNotFoundException e) {
            throw new AuthenticationHashNotFoundDBusException(e.getMessage())
        } finally {
            SecurityContextHolder.clearContext()
        }
    }

    public String getAsJSON(String name, String authenticationHash, long modelId, int revisionNumber) {
        return perform(name, authenticationHash, modelId, revisionNumber, null) as JSON
    }

    public String getAsJSON(String name, String authenticationHash, long modelId, int revisionNumber, String id) {
        return perform(name, authenticationHash, modelId, revisionNumber, id) as JSON
    }

    public String getAsString(String name, String authenticationHash, long modelId, int revisionNumber) {
        return perform(name, authenticationHash, modelId, revisionNumber, null)
    }

    public long getAsLong(String name, String authenticationHash, long modelId, int revisionNumber) {
        return perform(name, authenticationHash, modelId, revisionNumber, null)
    }
}
