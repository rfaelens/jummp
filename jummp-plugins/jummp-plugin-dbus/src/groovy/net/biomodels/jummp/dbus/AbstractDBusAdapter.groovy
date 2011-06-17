package net.biomodels.jummp.dbus

import org.freedesktop.dbus.DBusInterface
import org.freedesktop.dbus.exceptions.DBusException
import org.springframework.beans.factory.InitializingBean
import net.biomodels.jummp.dbus.authentication.AuthenticationHashNotFoundDBusException
import net.biomodels.jummp.core.user.AuthenticationHashNotFoundException
import net.biomodels.jummp.remote.AbstractCoreAdapter
import net.biomodels.jummp.dbus.authentication.AccessDeniedDBusException
import org.springframework.security.access.AccessDeniedException
import net.biomodels.jummp.core.user.RoleNotFoundException
import net.biomodels.jummp.dbus.user.RoleNotFoundDBusException
import net.biomodels.jummp.core.user.UserNotFoundException
import net.biomodels.jummp.dbus.user.UserNotFoundDBusException
import net.biomodels.jummp.core.user.UserInvalidException
import net.biomodels.jummp.dbus.user.UserInvalidDBusException
import org.springframework.security.authentication.BadCredentialsException
import net.biomodels.jummp.dbus.authentication.BadCredentialsDBusException
import net.biomodels.jummp.core.user.RegistrationException
import net.biomodels.jummp.dbus.user.UserManagementDBusException
import net.biomodels.jummp.core.user.UserManagementException
import net.biomodels.jummp.core.user.UserCodeInvalidException
import net.biomodels.jummp.dbus.user.UserCodeInvalidDBusException
import net.biomodels.jummp.core.user.UserCodeExpiredException
import net.biomodels.jummp.dbus.user.UserCodeExpiredDBusException
import org.apache.log4j.Logger

/**
 * @short Abstract Base class for all DBusAdapter Implementations.
 *
 * This class provides the shared methods for all classes exported to DBus.
 * This is mostly related to setting and restoring the Authentication in the
 * thread where the DBus method is executed.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
public abstract class AbstractDBusAdapter extends AbstractCoreAdapter implements InitializingBean {
    /**
     * Dependency injection of DBusManager
     */
    protected DBusManager dbusManager
    /**
     * The name of the object exported to DBus - needs to be set in the bean configuration
     */
    protected String objectName

    protected Logger log = Logger.getLogger(getClass())

    /**
     * Helper function to set the Authentication in the current thread
     * @param authentication
     */
    protected void setAuthentication(String authenticationHash) throws AuthenticationHashNotFoundDBusException {
        try {
            super.setAuthentication(authenticationHash)
        } catch (AuthenticationHashNotFoundException e) {
            throw new AuthenticationHashNotFoundDBusException(e.getMessage())
        }
    }

    /**
     * Setter for Dependency Injection of DBusManager.
     * @param dbusManager
     */
    public void setDbusManager(DBusManager dbusManager) {
        this.dbusManager = dbusManager
    }

    /**
     * Maps an Exception to a DBus Exception
     * @param e The normal Exception
     * @return The DBus Exception
     */
    protected Exception exceptionMapping(Exception e) {
        if (e instanceof AccessDeniedException) {
            return new AccessDeniedDBusException(e.getMessage())
        }
        if (e instanceof BadCredentialsException) {
            return new BadCredentialsDBusException(e.getMessage())
        }
        if (e instanceof IllegalArgumentException) {
            return new IllegalArgumentDBusException(e.getMessage())
        }
        if (e instanceof RegistrationException) {
            return new UserManagementDBusException(e.getMessage())
        }
        if (e instanceof RoleNotFoundException) {
            return new RoleNotFoundDBusException(e.getMessage())
        }
        if (e instanceof UserCodeExpiredException) {
            return new UserCodeExpiredDBusException(e.getUserName())
        }
        if (e instanceof UserCodeInvalidException) {
            return new UserCodeInvalidDBusException(e.getCode())
        }
        if (e instanceof UserInvalidException) {
            return new UserInvalidDBusException(e.getUserName())
        }
        if (e instanceof UserNotFoundException) {
            if (e.getUserName() != null) {
                return new UserNotFoundDBusException(e.getUserName());
            } else {
                return new UserNotFoundDBusException(e.getId().toString());
            }
        }
        if (e instanceof UserManagementException) {
            return new UserManagementDBusException(e.getMessage())
        }
        log.debug(e.message, e)
        return e
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

    /**
     * Providing a method of DBusInterface.
     * @return @c false
     */
    public boolean isRemote() {
        return false
    }
}
