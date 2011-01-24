package net.biomodels.jummp.core

import grails.plugin.jms.Queue
import net.biomodels.jummp.core.model.ModelFormat
import net.biomodels.jummp.model.Model
import net.biomodels.jummp.model.Revision
import net.biomodels.jummp.plugins.security.User
import org.apache.commons.io.FileUtils
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.access.AccessDeniedException
import net.biomodels.jummp.plugins.security.SerializableGrailsUser
import org.codehaus.groovy.grails.plugins.springsecurity.GrailsUser
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken

/**
 * @short Wrapper class around the ModelService exposed to JMS.
 *
 * This service is mostly just a wrapper around the ModelService exposed as a JMS interface.
 * Each method takes the Authentication as a parameter and sets it in the SecurityContext of the
 * current thread. Before the method returns the SecurityContext is cleared again.
 *
 * If an exception is thrown in the execution of a method the calling service method will time out.
 * Because of that each method has to catch the possible exception and return them. This implies that each
 * method needs to return a def.
 *
 * Returning @c null results in a time out as @c null stands for no return value. Because of that no method
 * may return @c null. In case a @c null value should be returned it has to be wrapped in a different type,
 * e.g. an Exception.
 *
 * In case a method is invoked with the wrong number of arguments an IllegalArgumentException is returned.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class JmsAdapterService {

    static exposes = ['jms']
    static destination = "jummpJms"
    static transactional = false
    /**
     * Dependency injection of authentication Manager
     */
    def authenticationManager
    /**
     * Dependency injection of ModelService
     */
    def modelService

    /**
     * Performs an Authentication.
     * @param message The Authentication, mostly a UsernamePasswordAuthenticationToken
     * @return A fully propagated Authentication or an AuthenticationException or an IllegalArgumentException if @p message is not an Authentication
     */
    @Queue
    def authenticate(def message) {
        if (message instanceof Authentication) {
            try {
                Authentication auth = authenticationManager.authenticate(message)
                // The authentication is propagated with an GrailsUser as principal
                // Unfortunately the GrailsUser class is not serializable.
                // Because of that a new Authentication is created using an own implementation of a serializable GrailsUser
                return new UsernamePasswordAuthenticationToken(SerializableGrailsUser.fromGrailsUser((GrailsUser)auth.principal),
                        auth.getCredentials(), auth.getAuthorities())
            } catch (AuthenticationException e) {
                // extraInformation is also a GrailsUser, so if it is set we need to create a new AuthenticationException
                // with a SerializableGrailsUser instead of the GrailsUser as extraInformation
                if (e.extraInformation) {
                    AuthenticationException exception = e.class.newInstance(e.message, SerializableGrailsUser.fromGrailsUser((GrailsUser)e.extraInformation))
                    exception.setAuthentication(e.authentication)
                    return exception
                } else {
                    return e
                }
            }
        }
        return new IllegalArgumentException("Did not receive an authentication")
    }

    /**
     * Wrapper for ModelService.getAllModels
     * @param message List with first element the Authentication, further arguments as required
     * @return List of Models or IllegalArgumentException for incorrect arguments
     */
    @Queue
    def getAllModels(def message) {
        if (!verifyMessage(message, [Authentication.class]) &&
                !verifyMessage(message, [Authentication.class, Integer.class, Integer.class]) &&
                !verifyMessage(message, [Authentication.class, Integer.class, Integer.class, Boolean.class])) {
            return new IllegalArgumentException("Invalid arguments passed to method. Allowed is Authentication or Authentication, Integer, Integer or Authentication, Integer, Integer, Boolean")
        }
        List arguments = (List)message
        List<Model> returnList = []
        // set authentication
        try {
            setAuthentication((Authentication)arguments[0])
            switch (arguments.size()) {
            case 1:
                returnList = modelService.getAllModels()
                break
            case 3:
                returnList = modelService.getAllModels((Integer)arguments[1], (Integer)arguments[2])
                break
            case 4:
                returnList = modelService.getAllModels((Integer)arguments[1], (Integer)arguments[2], (Boolean)arguments[3])
                break
            default:
                // nothing
                break
            }
        } finally {
            restoreAuthentication()
        }
        return returnList
    }

    /**
     * Wrapper for ModelService.getModelCount
     * @param message Authentication
     * @return Number of Models or IllegalArgumentException for missing Authentication
     */
    @Queue
    def getModelCount(def message) {
        if (!(message instanceof Authentication)) {
            return new IllegalArgumentException("Authentication as argument expected")
        }
        Integer result = 0
        try {
            setAuthentication((Authentication)message)
            result = modelService.getModelCount()
        } finally {
            restoreAuthentication()
        }
        return result
    }

    /**
     * Wrapper around ModelService.getLatestRevision
     * @param message List consisting of Authentication and Model
     * @return The Revision or IllegalArgumentException or AccessDeniedException if user does not have access to any Revision.
     */
    @Queue
    def getLatestRevision(def message) {
        if (!verifyMessage(message, [Authentication, Model])) {
            return new IllegalArgumentException("Authentication and Model as arguments expected")
        }
        Revision revision = null
        try {
            setAuthentication((Authentication)message[0])
            revision = modelService.getLatestRevision((Model)message[1])
        } finally {
            restoreAuthentication()
        }

        if (revision == null) {
            return new AccessDeniedException("No access to any revision of Model ${message[1].id}")
        } else {
            return revision
        }
    }

    /**
     * Wrapper around ModelService.getAllRevisions
     * @param message List consisting of Authentication and Model
     * @return List of Revisions or IllegalArgumentException
     */
    @Queue
    def getAllRevisions(def message) {
        if (!verifyMessage(message, [Authentication, Model])) {
            return new IllegalArgumentException("Authentication and Model as arguments expected")
        }
        List<Revision> result = []
        try {
            setAuthentication((Authentication)message[0])
            result = modelService.getAllRevisions((Model)message[1])
        } finally {
            restoreAuthentication()
        }
        return result
    }

    /**
     * Wrapper around ModelService.uploadModel
     * @param message List consisting of Authentication, content of file and command object
     * @return New created Model, InvalidArgumentException, AccessDeniedException or ModelException
     */
    @Queue
    def uploadModel(def message) {
        // TODO: replace Map by the proper type
        if (!verifyMessage(message, [Authentication, byte[], Map])) {
            return new IllegalArgumentException("Authentication, Byte Array and CommandObject as arguments excepted")
        }
        def result

        try {
            setAuthentication((Authentication)message[0])
            File file = File.createTempFile("jummpJms", null)
            file.append(message[1])
            result = modelService.uploadModel(file, message[2])
            FileUtils.deleteQuietly(file)
        } catch (AccessDeniedException e) {
            result = e
        } catch (ModelException e) {
            result = e
        } finally {
            restoreAuthentication()
        }

        return result
    }

    /**
     * Wrapper around ModelService.addRevision
     * @param message List consisting of Authentication, content of file as Byte Array, ModelFormat and String
     * @return New created Revision, InvalidArgumentException, AccessDeniedException or ModelException
     */
    @Queue
    def addRevision(def message) {
        if (!verifyMessage(message, [Authentication, Model, byte[], ModelFormat, String])) {
            return new IllegalArgumentException("Authentication, Model, Byte Array, ModelFormat and String as arguments expected")
        }
        def result

        try {
            setAuthentication((Authentication)message[0])
            File file = File.createTempFile("jummpJms", null)
            file.append(message[2])
            result = modelService.addRevision((Model)message[1], file, (ModelFormat)message[3], (String)message[4])
            FileUtils.deleteQuietly(file)
        } catch (AccessDeniedException e) {
            result = e
        } catch (ModelException e) {
            result = e
        } finally {
            restoreAuthentication()
        }

        return result
    }

    /**
     * Wrapper around ModelService.retrieveModelFile
     * @param message List consisting of Authentication and Revision
     * @return Byte Array, InvalidArgumentException, AccessDeniedException or ModelException
     */
    @Queue
    def retrieveModelFile(def message) {
        if (!verifyMessage(message, [Authentication, Revision])) {
            return new IllegalArgumentException("Authentication and Revision as arguments expected")
        }

        def result
        try {
            setAuthentication((Authentication)message[0])
            result = modelService.retrieveModelFile(message[1])
        } catch (AccessDeniedException e) {
            result = e
        } catch (ModelException e) {
            result = e
        } finally {
            restoreAuthentication()
        }
        return result
    }

    /**
     * Wrapper around ModelService.grantReadAccess
     * @param message List consisting of Authentication, Model and User
     * @return @c true if successfully updated, InvalidArgumentException or AccessDeniedException
     */
    @Queue
    def grantReadAccess(def message) {
        if (!verifyMessage(message, [Authentication, Model, User])) {
            return new IllegalArgumentException("Authentication, Model and User as arguments expected")
        }

        def result
        try {
            setAuthentication((Authentication)message[0])
            modelService.grantReadAccess(Model.get(message[1].id), User.get(message[2].id))
            result = true
        } catch (AccessDeniedException e) {
            result = e
        } finally {
            restoreAuthentication()
        }
        return result
    }

    /**
     * Wrapper around ModelService.grantWriteAccess
     * @param message List consisting of Authentication, Model and User
     * @return @c true if successfully updated, InvalidArgumentException or AccessDeniedException
     */
    @Queue
    def grantWriteAccess(def message) {
        if (!verifyMessage(message, [Authentication, Model, User])) {
            return new IllegalArgumentException("Authentication, Model and User as arguments expected")
        }

        def result
        try {
            setAuthentication((Authentication)message[0])
            modelService.grantWriteAccess(message[1], message[2])
            result = true
        } catch (AccessDeniedException e) {
            result = e
        } finally {
            restoreAuthentication()
        }
        return result
    }

    /**
     * Wrapper around ModelService.revokeReadAccess
     * @param message List consisting of Authentication, Model and User
     * @return @c true if successfully updated, @c false otherwise, InvalidArgumentException or AccessDeniedException
     */
    @Queue
    def revokeReadAccess(def message) {
        if (!verifyMessage(message, [Authentication, Model, User])) {
            return new IllegalArgumentException("Authentication, Model and User as arguments expected")
        }

        def result
        try {
            setAuthentication((Authentication)message[0])
            result = modelService.revokeReadAccess(message[1], message[2])
        } catch (AccessDeniedException e) {
            result = e
        } finally {
            restoreAuthentication()
        }
        return result
    }

    /**
     * Wrapper around ModelService.revokeWriteAccess
     * @param message List consisting of Authentication, Model and User
     * @return @c true if successfully updated, @c false otherwise, InvalidArgumentException or AccessDeniedException
     */
    @Queue
    def revokeWriteAccess(def message) {
        if (!verifyMessage(message, [Authentication, Model, User])) {
            return new IllegalArgumentException("Authentication, Model and User as arguments expected")
        }

        def result
        try {
            setAuthentication((Authentication)message[0])
            result = modelService.revokeWriteAccess(message[1], message[2])
        } catch (AccessDeniedException e) {
            result = e
        } finally {
            restoreAuthentication()
        }
        return result
    }

    /**
     * Wrapper around ModelService.deleteModel
     * @param message List consisting of Authentication and Model
     * @return @c true if successfully deleted, @c false otherwise, InvalidArgumentException or AccessDeniedException
     */
    @Queue
    def deleteModel(def message) {
        if (!verifyMessage(message, [Authentication, Model])) {
            return new IllegalArgumentException("Authentication and Model as arguments expected")
        }

        def result
        try {
            setAuthentication((Authentication)message[0])
            result = modelService.deleteModel(Model.get(message[1].id))
        } catch (AccessDeniedException e) {
            result = e
        } finally {
            restoreAuthentication()
        }
        return result
    }

    /**
     * Wrapper around ModelService.deleteModel
     * @param message List consisting of Authentication and Model
     * @return @c true if successfully deleted, @c false otherwise, InvalidArgumentException or AccessDeniedException
     */
    @Queue
    def restoreModel(def message) {
        if (!verifyMessage(message, [Authentication, Model])) {
            return new IllegalArgumentException("Authentication and Model as arguments expected")
        }

        def result
        try {
            setAuthentication((Authentication)message[0])
            result = modelService.restoreModel(Model.get(message[1].id))
        } catch (AccessDeniedException e) {
            result = e
        } finally {
            restoreAuthentication()
        }
        return result
    }

    /**
     * Helper function to verify that @p message has correct structure.
     * @param message The message to verify
     * @param classes The structure as List of Class types.
     * @return @c true, if the message structure is valid, @c false otherwise
     */
    private boolean verifyMessage(def message, List<Class<?>> classes) {
        if (!(message instanceof List)) {
            return false
        }
        if (message.size() != classes.size()) {
            return false
        }
        for (int i=0; i<classes.size(); i++) {
            Class clazz = classes[i]
            if (!clazz.isInstance(message[i])) {
                return false
            }
        }
        return true
    }

    /**
     * Helper function to set the Authentication in the current thread
     * @param authentication
     */
    private void setAuthentication(Authentication authentication) {
        SecurityContextHolder.clearContext()
        SecurityContextHolder.context.setAuthentication(authentication)
    }

    /**
     * Helper function to remove the Authentication from current thread.
     */
    private void restoreAuthentication() {
        SecurityContextHolder.clearContext()
    }
}
