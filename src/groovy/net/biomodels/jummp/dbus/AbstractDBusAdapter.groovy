package net.biomodels.jummp.dbus

import org.freedesktop.dbus.DBusInterface
import org.freedesktop.dbus.exceptions.DBusException
import org.springframework.beans.factory.InitializingBean
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.authority.GrantedAuthorityImpl
import org.springframework.security.core.context.SecurityContextHolder
import net.biomodels.jummp.core.IAuthenticationHashService
import net.biomodels.jummp.dbus.authentication.AuthenticationHashNotFoundDBusException
import net.biomodels.jummp.core.user.AuthenticationHashNotFoundException

/**
 * @short Abstract Base class for all DBusAdapter Implementations.
 *
 * This class provides the shared methods for all classes exported to DBus.
 * This is mostly related to setting and restoring the Authentication in the
 * thread where the DBus method is executed.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
public abstract class AbstractDBusAdapter implements InitializingBean {
    /**
     * Dependency injection of AuthenticationHashService
     */
    protected IAuthenticationHashService authenticationHashService
    /**
     * Dependency injection of DBusManager
     */
    protected DBusManager dbusManager
    /**
     * The name of the object exported to DBus - needs to be set in the bean configuration
     */
    protected String objectName
    private static final ANONYMOUS_AUTH = new AnonymousAuthenticationToken("key", "anonymousUser", [new GrantedAuthorityImpl("ROLE_ANONYMOUS")])

    /**
     * Helper function to set the Authentication in the current thread
     * @param authentication
     */
    protected void setAuthentication(String authenticationHash) throws AuthenticationHashNotFoundDBusException {
        SecurityContextHolder.clearContext()
        try {
            SecurityContextHolder.context.setAuthentication(authenticationHashService.retrieveAuthentication(authenticationHash))
        } catch (AuthenticationHashNotFoundException e) {
            throw new AuthenticationHashNotFoundDBusException(e.getMessage())
        }
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

    /**
     * Setter for Dependency Injection of DBusManager.
     * @param dbusManager
     */
    public void setDbusManager(DBusManager dbusManager) {
        this.dbusManager = dbusManager
    }

    /**
     * Setter for Injection of objectName.
     * @param objectName
     */
    public void setObjectName(String objectName) {
        this.objectName = objectName
    }

    public void afterPropertiesSet() throws Exception {
        try {
            dbusManager.getConnection().exportObject(objectName, (DBusInterface)this)
        } catch (DBusException e) {
            e.printStackTrace()
        }
    }
}
