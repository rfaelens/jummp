package net.biomodels.jummp.core.user

/**
 * @short Exception indicating that a User could not be found.
 *
 * This exception should be thrown whenever it is tried to access a User by either
 * Id or login identifier, but there is no such User present in the database.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class UserNotFoundException extends UserManagementException implements Serializable{
    private static final long serialVersionUID = 1L
    public UserNotFoundException(Long id) {
        super("No user for given id ${id}".toString(), id)
    }

    public UserNotFoundException(String userName) {
        super("User with identifier ${userName} not found".toString(), userName)
    }
}
