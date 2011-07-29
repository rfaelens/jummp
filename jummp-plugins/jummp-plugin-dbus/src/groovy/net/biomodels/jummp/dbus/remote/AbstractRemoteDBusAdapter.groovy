package net.biomodels.jummp.dbus.remote

import net.biomodels.jummp.remote.AbstractRemoteAdapter
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.AccountExpiredException
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import net.biomodels.jummp.core.JummpException
import org.freedesktop.dbus.exceptions.DBusExecutionException
import net.biomodels.jummp.core.user.AuthenticationHashNotFoundException
import org.springframework.security.access.AccessDeniedException
import net.biomodels.jummp.core.user.UserNotFoundException
import net.biomodels.jummp.core.user.UserInvalidException
import net.biomodels.jummp.core.user.UserCodeInvalidException
import net.biomodels.jummp.core.user.UserCodeExpiredException
import net.biomodels.jummp.core.ModelException
import net.biomodels.jummp.core.model.ModelTransportCommand
import net.biomodels.jummp.core.bives.DiffNotExistingException
import org.freedesktop.dbus.DBusConnection

/**
 * @short Abstract base class for all RemoteDBusAdapters.
 *
 * @author Martin Gräßlin <m.graeslin@dkfz.de>
 */
class AbstractRemoteDBusAdapter extends AbstractRemoteAdapter {
    protected DBusConnection connection

    /**
     * Retrieves a reference to the remote Object at the bus with name "net.biomodels.jummp" and with @p objectPath.
     * The method first tries to use the System bus. If the object cannot be resolved
     * on the System bus, the Session bus is tried.
     * @param objectPath The path on which the
     * @param clazz The interface
     * @return A reference to the remote object
     * @throws DBusExecutionException
     */
    protected <T> T getRemoteObject(String objectPath, Class<T> clazz) throws DBusExecutionException {
        try {
            connection = DBusConnection.getConnection(DBusConnection.SYSTEM)
            return (T)connection.getPeerRemoteObject("net.biomodels.jummp", objectPath, T)
        } catch (DBusExecutionException e) {
            connection = DBusConnection.getConnection(DBusConnection.SESSION)
            return (T)connection.getPeerRemoteObject("net.biomodels.jummp", objectPath, clazz)
        }
    }

    /**
     * Maps a DBusExecutionException to a "normal" exception.
     * @param e The DBusExecutionException
     * @return The mapped exception or a JummpException if we don't have a special type
     */
    protected Exception mapException(DBusExecutionException e) {
        switch (e.type) {
        case "net.biomodels.jummp.dbus.authentication.BadCredentialsDBusException":
            return new BadCredentialsException(e.message)
        case "net.biomodels.jummp.dbus.authentication.AccountExpiredDBusException":
            return new AccountExpiredException(e.message)
        case "net.biomodels.jummp.dbus.authentication.CredentialsExpiredDBusException":
            return new CredentialsExpiredException(e.message)
        case "net.biomodels.jummp.dbus.authentication.DisabledDBusException":
            return new DisabledException(e.message)
        case "net.biomodels.jummp.dbus.authentication.LockedDBusException":
            return new LockedException(e.message)
        case "net.biomodels.jummp.dbus.authentication.AuthenticationHashNotFoundDBusException":
            return new AuthenticationHashNotFoundException(e.message)
        case "net.biomodels.jummp.dbus.authentication.AccessDeniedDBusException":
            return new AccessDeniedException(e.message)
        case "net.biomodels.jummp.dbus.user.UserNotFoundDBusException":
            try {
                Long id = Long.parseLong(e.message)
                return new UserNotFoundException(id)
            } catch (NumberFormatException nfe) {
                return new UserNotFoundException(e.message)
            }
        case "net.biomodels.jummp.dbus.user.UserInvalidDBusException":
            return new UserInvalidException(e.message)
        case "net.biomodels.jummp.dbus.user.UserCodeInvalidDBusException":
            return new UserCodeInvalidException("", null, e.message)
        case "net.biomodels.jummp.dbus.user.UserCodeExpiredDBusException":
            return new UserCodeExpiredException(e.message, null)
        case "net.biomodels.jummp.dbus.IllegalArgumentDBusException":
            return new IllegalArgumentException(e.message)
        case "net.biomodels.jummp.dbus.model.ModelDBusException":
            return new ModelException(new ModelTransportCommand(), e.getMessage())
		case "net.biomodels.jummp.dbus.bives.DiffNotExistingDBusException":
			return new DiffNotExistingException(e.message)
        default:
            return new JummpException(e.message, e)
        }
    }
}
