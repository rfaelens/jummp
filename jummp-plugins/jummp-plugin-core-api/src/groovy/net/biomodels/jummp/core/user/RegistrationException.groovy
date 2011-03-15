package net.biomodels.jummp.core.user

/**
 * @short Exception thrown during registration of new user.
 * 
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class RegistrationException extends UserManagementException implements Serializable {
    private static final long serialVersionUID = 1L
    RegistrationException(userName) {
        this("Error occurred during registration of new user ${userName}".toString(), userName)
    }

    RegistrationException(String message, String userName) {
        super(message, userName)
    }
}
