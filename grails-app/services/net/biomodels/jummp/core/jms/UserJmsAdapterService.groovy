package net.biomodels.jummp.core.jms

import grails.plugin.jms.Queue
import net.biomodels.jummp.core.JmsAdapterService
import net.biomodels.jummp.core.JummpException
import net.biomodels.jummp.plugins.security.User
import org.perf4j.aop.Profiled
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication

/**
 * @short Wrapper class around the UserService exposed to JMS.
 *
 * For more documentation about the idea of exporting a service to JMS please refer to
 * @link JmsAdapterService.
 * 
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class UserJmsAdapterService extends JmsAdapterService {

    @SuppressWarnings("GrailsStatelessService")
    static exposes = ['jms']
    @SuppressWarnings("GrailsStatelessService")
    static destination = "jummpUserJms"
    static transactional = false
    /**
     * Dependency injection of UserService
     */
    def userService

    /**
     * Wraper around UserService.changePassword
     * @param message List consisting of Authentication, oldPassword and newPassword
     * @return @c true if password changed, InvalidArgumentException of BadCredentialsException if old password is incorrect
     */
    @Queue
    @Profiled(tag="userJmsAdapterService.changePassword")
    def changePassword(def message) {
        if (!verifyMessage(message, [Authentication, String, String])) {
            return new IllegalArgumentException("Authentication, String and String as arguments expected")
        }

        def result
        try {
            setAuthentication((Authentication)message[0])
            userService.changePassword(message[1], message[2])
            result = true
        } catch (BadCredentialsException e) {
            result = e
        } finally {
            restoreAuthentication()
        }
        return result
    }

    /**
     * Wrapper around UserService.editUser
     * @param message List consisting of Authentication and User
     * @return @c true if user was changed, IllegalArgumentException or AccessDeniedException
     */
    @Queue
    @Profiled(tag="userJmsAdapterService.editUser")
    def editUser(def message) {
        if (!verifyMessage(message, [Authentication, User])) {
            return new IllegalArgumentException("Authentication and User as arguments expected")
        }

        def result
        try {
            setAuthentication((Authentication)message[0])
            userService.editUser((User)message[1])
            result = true
        } catch (AccessDeniedException e) {
            result = e
        } catch (IllegalArgumentException e) {
            result = e
        } finally {
            restoreAuthentication()
        }
        return result
    }

    /**
     * Wrapper around UserService.getCurrentUser
     * @param message Authentication
     * @return The User, IllegalArgumentException or AccessDeniedException
     */
    @Queue
    @Profiled(tag="userJmsAdapterService.getCurrentUser")
    def getCurrentUser(def message) {
        if (!(message instanceof Authentication)) {
            return new IllegalArgumentException("Authentication as arguments expected")
        }

        def result
        try {
            setAuthentication((Authentication)message)
            result = userService.getCurrentUser()
        } catch (AccessDeniedException e) {
            result = e
        } finally {
            restoreAuthentication()
        }
        return result
    }

    /**
     * Wrapper around UserService.getUser
     * @param message List consisting of Authentication and Username
     * @return The User, IllegalArgumentException or AccessDeniedException
     */
    @Queue
    @Profiled(tag="userJmsAdapterService.getUser")
    def getUser(def message) {
        if (!verifyMessage(message, [Authentication, String])) {
            return new IllegalArgumentException("Authentication and String as arguments expected")
        }
        def result
        try {
            setAuthentication((Authentication)message[0])
            result = userService.getUser(message[1])
        } catch (AccessDeniedException e) {
            result = e
        } catch (IllegalArgumentException e) {
            result = e
        } finally {
            restoreAuthentication()
        }
        return result
    }

    /**
     * Wrapper around UserService.getAllUsers
     * @param message List consisting of Authentication, Integer and Integer
     * @return List of Users, IllegalArgumentException of AccessDeniedException
     */
    @Queue
    @Profiled(tag="userJmsAdapterService.getAllUser")
    def getAllUsers(def message) {
        if (!verifyMessage(message, [Authentication, Integer, Integer])) {
            return new IllegalArgumentException("Authentication, Integer and Integer as arguments expected")
        }

        def result
        try {
            setAuthentication((Authentication)message[0])
            result = userService.getAllUsers((Integer)message[1], (Integer)message[2])
        } catch (AccessDeniedException e) {
            result = e
        } finally {
            restoreAuthentication()
        }
        return result
    }

    /**
     * Wrapper around UserService.enableUser
     * @param message List consisting of Authentication, Long and Boolean
     * @return Boolean, AccessDeniedException or IllegalArgumentException
     */
    @Queue
    @Profiled(tag="userJmsAdapterService.enableUser")
    def enableUser(def message) {
        if (!verifyMessage(message, [Authentication, Long, Boolean])) {
            return new IllegalArgumentException("Authentication, Long and Boolean as arguments expected")
        }

        def result
        try {
            setAuthentication((Authentication)message[0])
            result = userService.enableUser((Long)message[1], (Boolean)message[2])
        } catch (AccessDeniedException e) {
            result = e
        } catch (IllegalArgumentException e) {
            result = e
        } finally {
            restoreAuthentication()
        }
        return result
    }

    /**
     * Wrapper around UserService.lockAccount
     * @param message List consisting of Authentication, Long and Boolean
     * @return Boolean, AccessDeniedException or IllegalArgumentException
     */
    @Queue
    @Profiled(tag="userJmsAdapterService.lockAccount")
    def lockAccount(def message) {
        if (!verifyMessage(message, [Authentication, Long, Boolean])) {
            return new IllegalArgumentException("Authentication, Long and Boolean as arguments expected")
        }

        def result
        try {
            setAuthentication((Authentication)message[0])
            result = userService.lockAccount((Long)message[1], (Boolean)message[2])
        } catch (AccessDeniedException e) {
            result = e
        } catch (IllegalArgumentException e) {
            result = e
        } finally {
            restoreAuthentication()
        }
        return result
    }

    /**
     * Wrapper around UserService.expireAccount
     * @param message List consisting of Authentication, Long and Boolean
     * @return Boolean, AccessDeniedException or IllegalArgumentException
     */
    @Queue
    @Profiled(tag="userJmsAdapterService.expireAccount")
    def expireAccount(def message) {
        if (!verifyMessage(message, [Authentication, Long, Boolean])) {
            return new IllegalArgumentException("Authentication, Long and Boolean as arguments expected")
        }

        def result
        try {
            setAuthentication((Authentication)message[0])
            result = userService.expireAccount((Long)message[1], (Boolean)message[2])
        } catch (AccessDeniedException e) {
            result = e
        } catch (IllegalArgumentException e) {
            result = e
        } finally {
            restoreAuthentication()
        }
        return result
    }

    /**
     * Wrapper around UserService.expirePassword
     * @param message List consisting of Authentication, Long and Boolean
     * @return Boolean, AccessDeniedException or IllegalArgumentException
     */
    @Queue
    @Profiled(tag="userJmsAdapterService.expirePassword")
    def expirePassword(def message) {
        if (!verifyMessage(message, [Authentication, Long, Boolean])) {
            return new IllegalArgumentException("Authentication, Long and Boolean as arguments expected")
        }

        def result
        try {
            setAuthentication((Authentication)message[0])
            result = userService.expirePassword((Long)message[1], (Boolean)message[2])
        } catch (AccessDeniedException e) {
            result = e
        } catch (IllegalArgumentException e) {
            result = e
        } finally {
            restoreAuthentication()
        }
        return result
    }

    /**
     * Wrapper around UserService.register
     * @param message List consisting of Authentication and User
     * @return Boolean, AccessDeniedException, JummpException or IllegalArgumentException
     */
    @Queue
    @Profiled(tag="userJmsAdapterService.register")
    def register(def message) {
        if (!verifyMessage(message, [Authentication, User])) {
            return new IllegalArgumentException("Authentication and User as arguments expected")
        }

        def result
        try {
            setAuthentication((Authentication)message[0])
            userService.register((User)message[1])
            result = true
        } catch (AccessDeniedException e) {
            result = e
        } catch (JummpException e) {
            result = e
        } finally {
            restoreAuthentication()
        }
        return result
    }

    /**
     * Wrapper around UserService.validateRegistration
     * @param message List consisting of Authentication, String and String
     * @return Boolean, AccessDeniedException, JummpException or IllegalArgumentException
     */
    @Queue
    @Profiled(tag="userJmsAdapterService.validateRegistration")
    def validateRegistration(def message) {
        if (!verifyMessage(message, [Authentication, String, String])) {
            return new IllegalArgumentException("Authentication, String and String as arguments expected")
        }

        def result
        try {
            setAuthentication((Authentication)message[0])
            userService.validateRegistration((String)message[1], (String)message[2])
            result = true
        } catch (AccessDeniedException e) {
            result = e
        } catch (JummpException e) {
            result = e
        } finally {
            restoreAuthentication()
        }
        return result
    }

    /**
     * Wrapper around UserSerivce.requestPassword
     * @param message List consisting of Authentication and String
     * @return Boolean, AccessDeniedException, JummpException of IllegalArgumentException
     */
    @Queue
    @Profiled(tag="userJmsAdapterService.requestPassword")
    def requestPassword(def message) {
        if (!verifyMessage(message, [Authentication, String])) {
            return new IllegalArgumentException("Authentication and String as arguments expected")
        }

        def result
        try {
            setAuthentication((Authentication)message[0])
            userService.requestPassword((String)message[1])
            result = true
        } catch (AccessDeniedException e) {
            result = e
        } catch (JummpException e) {
            result = e
        } finally {
            restoreAuthentication()
        }
        return result
    }

    /**
     * Wrapper around UserSerivce.resetPassword
     * @param message List consisting of Authentication, String, String and String
     * @return Boolean, AccessDeniedException, JummpException of IllegalArgumentException
     */
    @Queue
    @Profiled(tag="userJmsAdapterService.resetPassword")
    def resetPassword(def message) {
        if (!verifyMessage(message, [Authentication, String, String, String])) {
            return new IllegalArgumentException("Authentication, String, String and String as arguments expected")
        }

        def result
        try {
            setAuthentication((Authentication)message[0])
            userService.resetPassword((String)message[1], (String)message[2], (String)message[3])
            result = true
        } catch (AccessDeniedException e) {
            result = e
        } catch (JummpException e) {
            result = e
        } finally {
            restoreAuthentication()
        }
        return result
    }
}
