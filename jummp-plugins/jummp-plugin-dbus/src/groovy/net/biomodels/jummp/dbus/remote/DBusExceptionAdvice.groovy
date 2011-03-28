package net.biomodels.jummp.dbus.remote

import org.springframework.aop.ThrowsAdvice
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.AccountExpiredException
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import net.biomodels.jummp.core.JummpException
import org.freedesktop.dbus.exceptions.DBusExecutionException

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
        default:
            throw new JummpException(e.message, e)
        }
    }
}
