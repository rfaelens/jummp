package net.biomodels.jummp.dbus.authentication

/**
 * @short DBus Wrapper for org.springframework.security.authentication.DisabledException
 */
public class DisabledDBusException extends AuthenticationDBusException {
    public DisabledDBusException(String msg) {
        super(msg)
    }
}
