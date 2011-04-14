package net.biomodels.jummp.core.user

import net.biomodels.jummp.core.JummpException

/**
 * @short Base class for all User management related exceptions.
 * 
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
abstract class UserManagementException extends JummpException implements Serializable {
    private static final long serialVersionUID = 1L
    private String userName = null
    private Long id = null

    protected UserManagementException(String userName) {
        this("Unknown error while managing user with username ${userName}".toString(), userName)
    }

    protected UserManagementException(String message, String userName) {
        super(message)
        this.userName = userName
    }

    protected UserManagementException(long id) {
        this("Unknown eror while managing user with id ${id}".toString(), id)
    }

    protected UserManagementException(String message, Long id) {
        super(message)
        this.id = id
    }

    protected setUserName(String userName) {
        this.userName = userName
    }

    protected setId(Long id) {
        this.id = id
    }

    public String getUserName() {
        return this.userName
    }

    public Long getId() {
        return this.id
    }
}
