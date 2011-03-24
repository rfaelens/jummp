package net.biomodels.jummp.dbus.authentication

/**
 * @short DBus Wrapper for org.springframework.security.authentication.CredentialsExpiredException
 */
public class CredentialsExpiredDBusException extends AuthenticationDBusException {
    public CredentialsExpiredDBusException(String msg) {
        super(msg)
    }
}
