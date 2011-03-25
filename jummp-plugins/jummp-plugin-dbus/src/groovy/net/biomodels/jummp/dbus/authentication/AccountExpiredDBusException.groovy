package net.biomodels.jummp.dbus.authentication

/**
 * @short DBus Wrapper for org.springframework.security.authentication.AccountExpiredDBusException
 */
class AccountExpiredDBusException extends AuthenticationDBusException {
    public AccountExpiredDBusException(String msg) {
        super(msg)
    }
}
