package net.biomodels.jummp.dbus.remote

import org.springframework.aop.ThrowsAdvice
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

/**
 * @short Advice to map DBusExecutionException to appropriate type.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class DBusExceptionAdvice implements ThrowsAdvice {

    void afterThrowing(DBusExecutionException e) throws Throwable {
        switch (e.type) {
        case "net.biomodels.jummp.dbus.authentication.BadCredentialsDBusException":
            throw new BadCredentialsException(e.message)
        case "net.biomodels.jummp.dbus.authentication.AccountExpiredDBusException":
            throw new AccountExpiredException(e.message)
        case "net.biomodels.jummp.dbus.authentication.CredentialsExpiredDBusException":
            throw new CredentialsExpiredException(e.message)
        case "net.biomodels.jummp.dbus.authentication.DisabledDBusException":
            throw new DisabledException(e.message)
        case "net.biomodels.jummp.dbus.authentication.LockedDBusException":
            throw new LockedException(e.message)
        case "net.biomodels.jummp.dbus.authentication.AuthenticationHashNotFoundDBusException":
            throw new AuthenticationHashNotFoundException(e.message)
        case "net.biomodels.jummp.dbus.authentication.AccessDeniedDBusException":
            throw new AccessDeniedException(e.message)
        case "net.biomodels.jummp.dbus.user.UserNotFoundDBusException":
            try {
                Long id = Long.parseLong(e.message)
                throw new UserNotFoundException(id)
            } catch (NumberFormatException nfe) {
                throw new UserNotFoundException(e.message)
            }
        case "net.biomodels.jummp.dbus.user.UserInvalidDBusException":
            throw new UserInvalidException(e.message)
        default:
            throw new JummpException(e.message, e)
        }
    }
}
