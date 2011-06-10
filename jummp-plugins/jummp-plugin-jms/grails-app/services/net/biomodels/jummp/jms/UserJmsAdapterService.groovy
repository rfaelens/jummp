package net.biomodels.jummp.jms

import grails.plugin.jms.Queue
import net.biomodels.jummp.plugins.security.User
import net.biomodels.jummp.core.IUserService
import net.biomodels.jummp.webapp.ast.JmsAdapter
import net.biomodels.jummp.webapp.ast.JmsQueueMethod

/**
 * @short Wrapper class around the UserService exposed to JMS.
 *
 * For more documentation about the idea of exporting a service to JMS please refer to
 * @link JmsAdapterService.
 * 
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
@JmsAdapter
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
    @JmsQueueMethod(isAuthenticate=true, arguments=[String, String])
    def changePassword(def message) {
        userService.changePassword(message[1], message[2])
        return true
    }

    /**
     * Wrapper around UserService.editUser
     * @param message List consisting of AuthenticationHash and User
     * @return @c true if user was changed, IllegalArgumentException or AccessDeniedException
     */
    @grails.plugin.jms.Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[User])
    def editUser(def message) {
        userService.editUser((User)message[1])
        return true
    }

    /**
     * Wrapper around UserService.getCurrentUser
     * @param message AuthenticationHash
     * @return The User, IllegalArgumentException or AccessDeniedException
     */
    @grails.plugin.jms.Queue
    def getCurrentUser(def message) {
        if (!(message instanceof String)) {
            return new IllegalArgumentException("AuthenticationHash as arguments expected")
        }

        setAuthentication((String)message)
        return userService.getCurrentUser()
    }

    /**
     * Wrapper around UserService.getUser
     * @param message List consisting of AuthenticationHash and Username or Id
     * @return The User, IllegalArgumentException or AccessDeniedException
     */
    @grails.plugin.jms.Queue
    def getUser(def message) {
        if (!verifyMessage(message, [String, String]) && !verifyMessage(message, [String, Long])) {
            return new IllegalArgumentException("AuthenticationHash and String or Long as arguments expected")
        }
        setAuthentication((String)message[0])
        if (message[1] instanceof Long) {
            return userService.getUser((Long)message[1])
        } else {
            return userService.getUser((String)message[1])
        }
    }

    /**
     * Wrapper around UserService.getAllUsers
     * @param message List consisting of AuthenticationHash, Integer and Integer
     * @return List of Users, IllegalArgumentException of AccessDeniedException
     */
    @grails.plugin.jms.Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[Integer, Integer])
    def getAllUsers(def message) {
        return userService.getAllUsers((Integer)message[1], (Integer)message[2])
    }

    /**
     * Wrapper around UserService.enableUser
     * @param message List consisting of AuthenticationHash, Long and Boolean
     * @return Boolean, AccessDeniedException or IllegalArgumentException
     */
    @grails.plugin.jms.Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[Long, Boolean])
    def enableUser(def message) {
        return userService.enableUser((Long)message[1], (Boolean)message[2])
    }

    /**
     * Wrapper around UserService.lockAccount
     * @param message List consisting of AuthenticationHash, Long and Boolean
     * @return Boolean, AccessDeniedException or IllegalArgumentException
     */
    @grails.plugin.jms.Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[Long, Boolean])
    def lockAccount(def message) {
        return userService.lockAccount((Long)message[1], (Boolean)message[2])
    }

    /**
     * Wrapper around UserService.expireAccount
     * @param message List consisting of AuthenticationHash, Long and Boolean
     * @return Boolean, AccessDeniedException or IllegalArgumentException
     */
    @grails.plugin.jms.Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[Long, Boolean])
    def expireAccount(def message) {
        return userService.expireAccount((Long)message[1], (Boolean)message[2])
    }

    /**
     * Wrapper around UserService.expirePassword
     * @param message List consisting of AuthenticationHash, Long and Boolean
     * @return Boolean, AccessDeniedException or IllegalArgumentException
     */
    @grails.plugin.jms.Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[Long, Boolean])
    def expirePassword(def message) {
        return userService.expirePassword((Long)message[1], (Boolean)message[2])
    }

    /**
     * Wrapper around UserService.register
     * @param message List consisting of AuthenticationHash and User
     * @return Boolean, AccessDeniedException, JummpException or IllegalArgumentException
     */
    @grails.plugin.jms.Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[User])
    def register(def message) {
        return userService.register((User)message[1])
    }

    /**
     * Wrapper around UserService.validateRegistration
     * @param message List consisting of AuthenticationHash, String and String
     * @return Boolean, AccessDeniedException, JummpException or IllegalArgumentException
     */
    @grails.plugin.jms.Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[String, String])
    def validateRegistration(def message) {
        userService.validateRegistration((String)message[1], (String)message[2])
        return true
    }

    /**
     * Wrapper around UserService.validateAdminRegistration
     * @param message List consisting of AuthenticationHash, String and String or AuthenticationHash, String, String and String
     * @return Boolean, AccessDeniedException, UserManagementException or IllegalArgumentException
     */
    @grails.plugin.jms.Queue
    def validateAdminRegistration(def message) {
        if (!verifyMessage(message, [String, String, String]) &&
                !verifyMessage(message, [String, String, String, String])) {
            return new IllegalArgumentException("AuthenticationHash, String and String or AuthenticationHash, String, String and String as arguments expected")
        }

        setAuthentication((String)message[0])
        if (message.size() == 3) {
            userService.validateAdminRegistration((String)message[1], (String)message[2])
        } else {
            userService.validateAdminRegistration((String)message[1], (String)message[2], (String)message[3])
        }
        return true
    }

    /**
     * Wrapper around UserSerivce.requestPassword
     * @param message List consisting of AuthenticationHash and String
     * @return Boolean, AccessDeniedException, JummpException of IllegalArgumentException
     */
    @grails.plugin.jms.Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[String])
    def requestPassword(def message) {
        userService.requestPassword((String)message[1])
        return true
    }

    /**
     * Wrapper around UserSerivce.resetPassword
     * @param message List consisting of AuthenticationHash, String, String and String
     * @return Boolean, AccessDeniedException, JummpException of IllegalArgumentException
     */
    @grails.plugin.jms.Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[String, String, String])
    def resetPassword(def message) {
        userService.resetPassword((String)message[1], (String)message[2], (String)message[3])
        return true
    }

    /**
     * Wrapper around UserService.getAllRoles
     * @param message AuthenticationHash
     * @return List<Role>, AccessDeniedException or IllegalArgumentException
     */
    @grails.plugin.jms.Queue
    def getAllRoles(def message) {
        if (!(message instanceof String)) {
            return new IllegalArgumentException("AuthenticationHash as argument expected")
        }

        setAuthentication(message)
        return userService.getAllRoles()
    }

    /**
     * Wrapper around UserService.getRolesForUser
     * @param message List consisting of AuthenticationHash and Long
     * @return List<Role>, AccessDeniedException or IllegalArgumentException
     */
    @grails.plugin.jms.Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[Long])
    def getRolesForUser(def message) {
        return userService.getRolesForUser((Long)message[1])
    }

    /**
     * Wrapper around UserService.addRoleToUser
     * @param message List consisting of AuthenticationHash, Long and Long
     * @return Boolean, AccessDeniedException, UserNotFoundException, RoleNotFoundException or IllegalArgumentException
     */
    @grails.plugin.jms.Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[Long, Long])
    def addRoleToUser(def message) {
        userService.addRoleToUser((Long)message[1], (Long)message[2])
        return true
    }

    /**
     * Wrapper around UserService.addRoleToUser
     * @param message List consisting of AuthenticationHash, Long and Long
     * @return Boolean, AccessDeniedException, UserNotFoundException, RoleNotFoundException or IllegalArgumentException
     */
    @grails.plugin.jms.Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[Long, Long])
    def removeRoleFromUser(def message) {
        userService.removeRoleFromUser((Long)message[1], (Long)message[2])
        return true
    }
}
