package net.biomodels.jummp.jms

import grails.plugin.jms.Queue
import net.biomodels.jummp.plugins.security.User
import org.perf4j.aop.Profiled
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.BadCredentialsException
import net.biomodels.jummp.core.user.RegistrationException
import net.biomodels.jummp.core.user.RoleNotFoundException
import net.biomodels.jummp.core.user.UserInvalidException
import net.biomodels.jummp.core.user.UserManagementException
import net.biomodels.jummp.core.user.UserNotFoundException
import net.biomodels.jummp.core.IUserService
import net.biomodels.jummp.core.user.AuthenticationHashNotFoundException

/**
 * @short Wrapper class around the UserService exposed to JMS.
 *
 * For more documentation about the idea of exporting a service to JMS please refer to
 * @link JmsAdapterService.
 * 
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class UserJmsAdapterService extends AbstractJmsAdapter {

    @SuppressWarnings("GrailsStatelessService")
    static exposes = ['jms']
    @SuppressWarnings("GrailsStatelessService")
    static destination = "jummpUserJms"
    static transactional = false
    /**
     * Dependency injection of UserService
     */
    IUserService userService

    /**
     * Wraper around UserService.changePassword
     * @param message List consisting of AuthenticationHash, oldPassword and newPassword
     * @return @c true if password changed, InvalidArgumentException of BadCredentialsException if old password is incorrect
     */
    @grails.plugin.jms.Queue
    @Profiled(tag="UserJmsAdapter.changePassword")
    def changePassword(def message) {
        if (!verifyMessage(message, [String, String, String])) {
            return new IllegalArgumentException("AuthenticationHash, String and String as arguments expected")
        }

        def result
        try {
            setAuthentication((String)message[0])
            userService.changePassword(message[1], message[2])
            result = true
        } catch (BadCredentialsException e) {
            result = e
        } catch (AuthenticationHashNotFoundException e) {
            result = e
        } finally {
            restoreAuthentication()
        }
        return result
    }

    /**
     * Wrapper around UserService.editUser
     * @param message List consisting of AuthenticationHash and User
     * @return @c true if user was changed, IllegalArgumentException or AccessDeniedException
     */
    @grails.plugin.jms.Queue
    @Profiled(tag="UserJmsAdapter.editUser")
    def editUser(def message) {
        if (!verifyMessage(message, [String, User])) {
            return new IllegalArgumentException("AuthenticationHash and User as arguments expected")
        }

        def result
        try {
            setAuthentication((String)message[0])
            userService.editUser((User)message[1])
            result = true
        } catch (AccessDeniedException e) {
            result = e
        } catch (UserInvalidException e) {
            result = e
        } catch (AuthenticationHashNotFoundException e) {
            result = e
        } finally {
            restoreAuthentication()
        }
        return result
    }

    /**
     * Wrapper around UserService.getCurrentUser
     * @param message AuthenticationHash
     * @return The User, IllegalArgumentException or AccessDeniedException
     */
    @grails.plugin.jms.Queue
    @Profiled(tag="UserJmsAdapter.getCurrentUser")
    def getCurrentUser(def message) {
        if (!(message instanceof String)) {
            return new IllegalArgumentException("AuthenticationHash as arguments expected")
        }

        def result
        try {
            setAuthentication((String)message)
            result = userService.getCurrentUser()
        } catch (AccessDeniedException e) {
            result = e
        } catch (AuthenticationHashNotFoundException e) {
            result = e
        } finally {
            restoreAuthentication()
        }
        return result
    }

    /**
     * Wrapper around UserService.getUser
     * @param message List consisting of AuthenticationHash and Username or Id
     * @return The User, IllegalArgumentException or AccessDeniedException
     */
    @grails.plugin.jms.Queue
    @Profiled(tag="UserJmsAdapter.getUser")
    def getUser(def message) {
        if (!verifyMessage(message, [String, String]) && !verifyMessage(message, [String, Long])) {
            return new IllegalArgumentException("AuthenticationHash and String or Long as arguments expected")
        }
        def result
        try {
            setAuthentication((String)message[0])
            if (message[1] instanceof Long) {
                result = userService.getUser((Long)message[1])
            } else {
                result = userService.getUser((String)message[1])
            }
        } catch (AccessDeniedException e) {
            result = e
        } catch (UserNotFoundException e) {
            result = e
        } catch (AuthenticationHashNotFoundException e) {
            result = e
        } finally {
            restoreAuthentication()
        }
        return result
    }

    /**
     * Wrapper around UserService.getAllUsers
     * @param message List consisting of AuthenticationHash, Integer and Integer
     * @return List of Users, IllegalArgumentException of AccessDeniedException
     */
    @grails.plugin.jms.Queue
    @Profiled(tag="UserJmsAdapter.getAllUser")
    def getAllUsers(def message) {
        if (!verifyMessage(message, [String, Integer, Integer])) {
            return new IllegalArgumentException("AuthenticationHash, Integer and Integer as arguments expected")
        }

        def result
        try {
            setAuthentication((String)message[0])
            result = userService.getAllUsers((Integer)message[1], (Integer)message[2])
        } catch (AccessDeniedException e) {
            result = e
        } catch (AuthenticationHashNotFoundException e) {
            result = e
        } finally {
            restoreAuthentication()
        }
        return result
    }

    /**
     * Wrapper around UserService.enableUser
     * @param message List consisting of AuthenticationHash, Long and Boolean
     * @return Boolean, AccessDeniedException or IllegalArgumentException
     */
    @grails.plugin.jms.Queue
    @Profiled(tag="UserJmsAdapter.enableUser")
    def enableUser(def message) {
        if (!verifyMessage(message, [String, Long, Boolean])) {
            return new IllegalArgumentException("AuthenticationHash, Long and Boolean as arguments expected")
        }

        def result
        try {
            setAuthentication((String)message[0])
            result = userService.enableUser((Long)message[1], (Boolean)message[2])
        } catch (AccessDeniedException e) {
            result = e
        } catch (UserNotFoundException e) {
            result = e
        } catch (AuthenticationHashNotFoundException e) {
            result = e
        } finally {
            restoreAuthentication()
        }
        return result
    }

    /**
     * Wrapper around UserService.lockAccount
     * @param message List consisting of AuthenticationHash, Long and Boolean
     * @return Boolean, AccessDeniedException or IllegalArgumentException
     */
    @grails.plugin.jms.Queue
    @Profiled(tag="UserJmsAdapter.lockAccount")
    def lockAccount(def message) {
        if (!verifyMessage(message, [String, Long, Boolean])) {
            return new IllegalArgumentException("AuthenticationHash, Long and Boolean as arguments expected")
        }

        def result
        try {
            setAuthentication((String)message[0])
            result = userService.lockAccount((Long)message[1], (Boolean)message[2])
        } catch (AccessDeniedException e) {
            result = e
        } catch (UserNotFoundException e) {
            result = e
        } catch (AuthenticationHashNotFoundException e) {
            result = e
        } finally {
            restoreAuthentication()
        }
        return result
    }

    /**
     * Wrapper around UserService.expireAccount
     * @param message List consisting of AuthenticationHash, Long and Boolean
     * @return Boolean, AccessDeniedException or IllegalArgumentException
     */
    @grails.plugin.jms.Queue
    @Profiled(tag="UserJmsAdapter.expireAccount")
    def expireAccount(def message) {
        if (!verifyMessage(message, [String, Long, Boolean])) {
            return new IllegalArgumentException("AuthenticationHash, Long and Boolean as arguments expected")
        }

        def result
        try {
            setAuthentication((String)message[0])
            result = userService.expireAccount((Long)message[1], (Boolean)message[2])
        } catch (AccessDeniedException e) {
            result = e
        } catch (UserNotFoundException e) {
            result = e
        } catch (AuthenticationHashNotFoundException e) {
            result = e
        } finally {
            restoreAuthentication()
        }
        return result
    }

    /**
     * Wrapper around UserService.expirePassword
     * @param message List consisting of AuthenticationHash, Long and Boolean
     * @return Boolean, AccessDeniedException or IllegalArgumentException
     */
    @grails.plugin.jms.Queue
    @Profiled(tag="UserJmsAdapter.expirePassword")
    def expirePassword(def message) {
        if (!verifyMessage(message, [String, Long, Boolean])) {
            return new IllegalArgumentException("AuthenticationHash, Long and Boolean as arguments expected")
        }

        def result
        try {
            setAuthentication((String)message[0])
            result = userService.expirePassword((Long)message[1], (Boolean)message[2])
        } catch (AccessDeniedException e) {
            result = e
        } catch (UserNotFoundException e) {
            result = e
        } catch (AuthenticationHashNotFoundException e) {
            result = e
        } finally {
            restoreAuthentication()
        }
        return result
    }

    /**
     * Wrapper around UserService.register
     * @param message List consisting of AuthenticationHash and User
     * @return Boolean, AccessDeniedException, JummpException or IllegalArgumentException
     */
    @grails.plugin.jms.Queue
    @Profiled(tag="UserJmsAdapter.register")
    def register(def message) {
        if (!verifyMessage(message, [String, User])) {
            return new IllegalArgumentException("AuthenticationHash and User as arguments expected")
        }

        def result
        try {
            setAuthentication((String)message[0])
            result = userService.register((User)message[1])
        } catch (AccessDeniedException e) {
            result = e
        } catch (RegistrationException e) {
            result = e
        } catch (UserInvalidException e) {
            result = e
        } catch (AuthenticationHashNotFoundException e) {
            result = e
        } finally {
            restoreAuthentication()
        }
        return result
    }

    /**
     * Wrapper around UserService.validateRegistration
     * @param message List consisting of AuthenticationHash, String and String
     * @return Boolean, AccessDeniedException, JummpException or IllegalArgumentException
     */
    @grails.plugin.jms.Queue
    @Profiled(tag="UserJmsAdapter.validateRegistration")
    def validateRegistration(def message) {
        if (!verifyMessage(message, [String, String, String])) {
            return new IllegalArgumentException("AuthenticationHash, String and String as arguments expected")
        }

        def result
        try {
            setAuthentication((String)message[0])
            userService.validateRegistration((String)message[1], (String)message[2])
            result = true
        } catch (AccessDeniedException e) {
            result = e
        } catch (UserManagementException e) {
            result = e
        } catch (AuthenticationHashNotFoundException e) {
            result = e
        } finally {
            restoreAuthentication()
        }
        return result
    }

    /**
     * Wrapper around UserService.validateAdminRegistration
     * @param message List consisting of AuthenticationHash, String and String or AuthenticationHash, String, String and String
     * @return Boolean, AccessDeniedException, UserManagementException or IllegalArgumentException
     */
    @grails.plugin.jms.Queue
    @Profiled(tag="UserJmsAdapter.validateAdminRegistration")
    def validateAdminRegistration(def message) {
        if (!verifyMessage(message, [String, String, String]) &&
                !verifyMessage(message, [String, String, String, String])) {
            return new IllegalArgumentException("AuthenticationHash, String and String or AuthenticationHash, String, String and String as arguments expected")
        }

        def result
        try {
            setAuthentication((String)message[0])
            if (message.size() == 3) {
                userService.validateAdminRegistration((String)message[1], (String)message[2])
            } else {
                userService.validateAdminRegistration((String)message[1], (String)message[2], (String)message[3])
            }
            result = true
        } catch (AccessDeniedException e) {
            result = e
        } catch (UserManagementException e) {
            result = e
        } catch (AuthenticationHashNotFoundException e) {
            result = e
        } finally {
            restoreAuthentication()
        }
        return result
    }

    /**
     * Wrapper around UserSerivce.requestPassword
     * @param message List consisting of AuthenticationHash and String
     * @return Boolean, AccessDeniedException, JummpException of IllegalArgumentException
     */
    @grails.plugin.jms.Queue
    @Profiled(tag="UserJmsAdapter.requestPassword")
    def requestPassword(def message) {
        if (!verifyMessage(message, [String, String])) {
            return new IllegalArgumentException("AuthenticationHash and String as arguments expected")
        }

        def result
        try {
            setAuthentication((String)message[0])
            userService.requestPassword((String)message[1])
            result = true
        } catch (AccessDeniedException e) {
            result = e
        } catch (UserNotFoundException e) {
            result = e
        } catch (AuthenticationHashNotFoundException e) {
            result = e
        } finally {
            restoreAuthentication()
        }
        return result
    }

    /**
     * Wrapper around UserSerivce.resetPassword
     * @param message List consisting of AuthenticationHash, String, String and String
     * @return Boolean, AccessDeniedException, JummpException of IllegalArgumentException
     */
    @grails.plugin.jms.Queue
    @Profiled(tag="UserJmsAdapter.resetPassword")
    def resetPassword(def message) {
        if (!verifyMessage(message, [String, String, String, String])) {
            return new IllegalArgumentException("AuthenticationHash, String, String and String as arguments expected")
        }

        def result
        try {
            setAuthentication((String)message[0])
            userService.resetPassword((String)message[1], (String)message[2], (String)message[3])
            result = true
        } catch (AccessDeniedException e) {
            result = e
        } catch (UserManagementException e) {
            result = e
        } catch (AuthenticationHashNotFoundException e) {
            result = e
        } finally {
            restoreAuthentication()
        }
        return result
    }

    /**
     * Wrapper around UserService.getAllRoles
     * @param message AuthenticationHash
     * @return List<Role>, AccessDeniedException or IllegalArgumentException
     */
    @grails.plugin.jms.Queue
    @Profiled(tag="UserJmsAdapter.getAllRoles")
    def getAllRoles(def message) {
        if (!(message instanceof String)) {
            return new IllegalArgumentException("AuthenticationHash as argument expected")
        }

        def result
        try {
            setAuthentication(message)
            result = userService.getAllRoles()
        } catch (AccessDeniedException e) {
            result = e
        } catch (AuthenticationHashNotFoundException e) {
            result = e
        } finally {
            restoreAuthentication()
        }
        return result
    }

    /**
     * Wrapper around UserService.getRolesForUser
     * @param message List consisting of AuthenticationHash and Long
     * @return List<Role>, AccessDeniedException or IllegalArgumentException
     */
    @grails.plugin.jms.Queue
    @Profiled(tag="UserJmsAdapter.getRolesForUser")
    def getRolesForUser(def message) {
        if (!verifyMessage(message, [String, Long])) {
            return new IllegalArgumentException("AuthenticationHash and Long as arguments expected")
        }

        def result
        try {
            setAuthentication((String)message[0])
            result = userService.getRolesForUser((Long)message[1])
        } catch (AccessDeniedException e) {
            result = e
        } catch (AuthenticationHashNotFoundException e) {
            result = e
        } finally {
            restoreAuthentication()
        }
        return result
    }

    /**
     * Wrapper around UserService.addRoleToUser
     * @param message List consisting of AuthenticationHash, Long and Long
     * @return Boolean, AccessDeniedException, UserNotFoundException, RoleNotFoundException or IllegalArgumentException
     */
    @grails.plugin.jms.Queue
    @Profiled(tag="UserJmsAdapter.addRoleToUser")
    def addRoleToUser(def message) {
        if (!verifyMessage(message, [String, Long, Long])) {
            return new IllegalArgumentException("AuthenticationHash, Long and Long as arguments expected")
        }

        def result
        try {
            setAuthentication((String)message[0])
            userService.addRoleToUser((Long)message[1], (Long)message[2])
            result = true
        } catch (AccessDeniedException e) {
            result = e
        } catch (UserNotFoundException e) {
            result = e
        } catch (RoleNotFoundException e) {
            result = e
        } catch (AuthenticationHashNotFoundException e) {
            result = e
        } finally {
            restoreAuthentication()
        }
        return result
    }

    /**
     * Wrapper around UserService.addRoleToUser
     * @param message List consisting of AuthenticationHash, Long and Long
     * @return Boolean, AccessDeniedException, UserNotFoundException, RoleNotFoundException or IllegalArgumentException
     */
    @grails.plugin.jms.Queue
    @Profiled(tag="UserJmsAdapter.removeRoleFromUser")
    def removeRoleFromUser(def message) {
        if (!verifyMessage(message, [String, Long, Long])) {
            return new IllegalArgumentException("AuthenticationHash, Long and Long as arguments expected")
        }

        def result
        try {
            setAuthentication((String)message[0])
            userService.removeRoleFromUser((Long)message[1], (Long)message[2])
            result = true
        } catch (AccessDeniedException e) {
            result = e
        } catch (UserNotFoundException e) {
            result = e
        } catch (RoleNotFoundException e) {
            result = e
        } catch (AuthenticationHashNotFoundException e) {
            result = e
        } finally {
            restoreAuthentication()
        }
        return result
    }
}
