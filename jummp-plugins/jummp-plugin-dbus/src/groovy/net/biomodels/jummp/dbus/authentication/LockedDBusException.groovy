package net.biomodels.jummp.dbus.authentication

/**
 * @short DBus Wrapper for org.springframework.security.authentication.LockedException
 */
public class LockedDBusException extends AuthenticationDBusException {
    public LockedDBusException(String msg) {
        super(msg)
    }
}
