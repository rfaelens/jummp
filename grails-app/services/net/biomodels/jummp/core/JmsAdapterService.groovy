package net.biomodels.jummp.core

import grails.plugin.jms.Queue
import net.biomodels.jummp.model.ModelFormat
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
import org.perf4j.aop.Profiled
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import net.biomodels.jummp.core.model.ModelTransportCommand
import net.biomodels.jummp.core.model.RevisionTransportCommand
import net.biomodels.jummp.core.model.ModelFormatTransportCommand
import net.biomodels.jummp.core.model.ModelListSorting
import net.biomodels.jummp.model.Publication
import net.biomodels.jummp.core.model.PublicationTransportCommand
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl
import org.springframework.security.authentication.BadCredentialsException

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
     * Dependency injection of UserService
     */
    def userService

    /**
     * Performs an Authentication.
     * @param message The Authentication, mostly a UsernamePasswordAuthenticationToken
     * @return A fully propagated Authentication or an AuthenticationException or an IllegalArgumentException if @p message is not an Authentication
     */
    @Queue
    @Profiled(tag="jmsAdapterService.authenticate")
    def authenticate(def message) {
        if (message instanceof Authentication) {
            try {
                Authentication auth = authenticationManager.authenticate(message)
                if (auth.principal instanceof LdapUserDetailsImpl) {
                    return auth
                }
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
     * @return List of ModelTransportCommands or IllegalArgumentException for incorrect arguments
     */
    @Queue
    @Profiled(tag="jmsAdapterService.getAllModels")
    def getAllModels(def message) {
        if (!verifyMessage(message, [Authentication.class]) &&
                !verifyMessage(message, [Authentication.class, ModelListSorting.class]) &&
                !verifyMessage(message, [Authentication.class, Integer.class, Integer.class]) &&
                !verifyMessage(message, [Authentication.class, Integer.class, Integer.class, Boolean.class]) &&
                !verifyMessage(message, [Authentication.class, Integer.class, Integer.class, ModelListSorting.class]) &&
                !verifyMessage(message, [Authentication.class, Integer.class, Integer.class, Boolean.class, ModelListSorting.class])) {
            return new IllegalArgumentException("Invalid arguments passed to method. Allowed is Authentication or Authentication, Integer, Integer or Authentication, Integer, Integer, Boolean")
        }
        List arguments = (List)message
        List<Model> modelList = []
        List<ModelTransportCommand> returnList = []
        // set authentication
        try {
            setAuthentication((Authentication)arguments[0])
            switch (arguments.size()) {
            case 1:
                modelList = modelService.getAllModels()
                break
            case 2:
                modelList = modelService.getAllModels((ModelListSorting)arguments[1])
                break
            case 3:
                modelList = modelService.getAllModels((Integer)arguments[1], (Integer)arguments[2])
                break
            case 4:
                if (arguments[3] instanceof ModelListSorting) {
                    modelList = modelService.getAllModels((Integer)arguments[1], (Integer)arguments[2], (ModelListSorting)arguments[3])
                } else {
                    modelList = modelService.getAllModels((Integer)arguments[1], (Integer)arguments[2], (Boolean)arguments[3])
                }
                break
            case 5:
                modelList = modelService.getAllModels((Integer)arguments[1], (Integer)arguments[2], (Boolean)arguments[3], (ModelListSorting)arguments[4])
                break
            default:
                // nothing
                break
            }
        } finally {
            restoreAuthentication()
        }
        modelList.each {
            returnList << it.toCommandObject()
        }
        return returnList
    }

    /**
     * Wrapper for ModelService.getModelCount
     * @param message Authentication
     * @return Number of Models or IllegalArgumentException for missing Authentication
     */
    @Queue
    @Profiled(tag="jmsAdapterService.getModelCount")
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
     * @param message List consisting of Authentication and ModelTransportCommand
     * @return A RevisionTransportCommand or IllegalArgumentException or AccessDeniedException if user does not have access to any Revision.
     */
    @Queue
    @Profiled(tag="jmsAdapterService.getLatestRevision")
    def getLatestRevision(def message) {
        if (!verifyMessage(message, [Authentication, ModelTransportCommand])) {
            return new IllegalArgumentException("Authentication and Model as arguments expected")
        }
        Revision revision = null
        try {
            setAuthentication((Authentication)message[0])
            revision = modelService.getLatestRevision(Model.get(message[1].id))
        } finally {
            restoreAuthentication()
        }

        if (revision == null) {
            return new AccessDeniedException("No access to any revision of Model ${message[1].id}")
        } else {
            return revision.toCommandObject()
        }
    }

    /**
     * Wrapper around ModelService.getAllRevisions
     * @param message List consisting of Authentication and Model
     * @return List of Revisions wrapped in RevisionTransportCommand or IllegalArgumentException
     */
    @Queue
    @Profiled(tag="jmsAdapterService.getAllRevisions")
    def getAllRevisions(def message) {
        if (!verifyMessage(message, [Authentication, ModelTransportCommand])) {
            return new IllegalArgumentException("Authentication and Model as arguments expected")
        }
        List<Revision> result = []
        try {
            setAuthentication((Authentication)message[0])
            result = modelService.getAllRevisions(Model.get(message[1].id))
        } finally {
            restoreAuthentication()
        }
        List<RevisionTransportCommand> revisions = []
        result.each {
            revisions << it.toCommandObject()
        }
        return revisions
    }

    /**
     * Wrapper around ModelService.getPublication
     * @param message List consisting of Authentication and Model
     * @return PublicationTransportCommand or IllegalArgumentException or AccessDeniedException
     */
    @Queue
    @Profiled(tag="jmsAdapterService.getPublication")
    def getPublication(def message) {
        if (!verifyMessage(message, [Authentication, ModelTransportCommand])) {
            return new IllegalArgumentException("Authentication and Model as arguments expected")
        }
        def result
        try {
            setAuthentication((Authentication)message[0])
            Publication publication = modelService.getPublication(Model.get(message[1].id))
            if (publication) {
                result = publication.toCommandObject()
            } else {
                result = new PublicationTransportCommand()
            }
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
     * Wrapper around ModelService.uploadModel
     * @param message List consisting of Authentication, content of file and command object
     * @return New created Model as ModelTransportCommand, InvalidArgumentException, AccessDeniedException or ModelException
     */
    @Queue
    @Profiled(tag="jmsAdapterService.uploadModel")
    def uploadModel(def message) {
        // TODO: replace Map by the proper type
        if (!verifyMessage(message, [Authentication, byte[], ModelTransportCommand])) {
            return new IllegalArgumentException("Authentication, Byte Array and CommandObject as arguments excepted")
        }
        def result

        try {
            setAuthentication((Authentication)message[0])
            File file = File.createTempFile("jummpJms", null)
            file.append(message[1])
            result = modelService.uploadModel(file, (ModelTransportCommand)message[2]).toCommandObject()
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
     * @param message List consisting of Authentication, ModelTransportCommand, content of file as Byte Array, ModelFormatTransportCommand and String
     * @return New created Revision as RevisionTransportCommand, InvalidArgumentException, AccessDeniedException or ModelException
     */
    @Queue
    @Profiled(tag="jmsAdapterService.addRevision")
    def addRevision(def message) {
        if (!verifyMessage(message, [Authentication, ModelTransportCommand, byte[], ModelFormatTransportCommand, String])) {
            return new IllegalArgumentException("Authentication, Model, Byte Array, ModelFormatTransportCommand and String as arguments expected")
        }
        def result

        try {
            setAuthentication((Authentication)message[0])
            File file = File.createTempFile("jummpJms", null)
            file.append(message[2])
            result = modelService.addRevision(Model.get((message[1]).id), file, ModelFormat.findByIdentifier(((ModelFormatTransportCommand)message[3]).identifier), (String)message[4]).toCommandObject()
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
     * Wrapper around ModelService.canAddRevision
     * @param message List consisting of Authentication and ModelTransportCommand
     * @return Boolean or IllegalArgumentException
     */
    @Queue
    @Profiled(tag="jmsAdapterService.canAddRevision")
    def canAddRevision(def message) {
        if (!verifyMessage(message, [Authentication, ModelTransportCommand])) {
            return new IllegalArgumentException("Authentication and Model as arguments expected")
        }

        def result
        try {
            setAuthentication((Authentication)message[0])
            result = modelService.canAddRevision(Model.get((message[1]).id))
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
    @Profiled(tag="jmsAdapterService.retrieveModelFile")
    def retrieveModelFile(def message) {
        if (!verifyMessage(message, [Authentication, RevisionTransportCommand]) &&
            !verifyMessage(message, [Authentication, ModelTransportCommand])) {
            return new IllegalArgumentException("Authentication and Revision or Model as arguments expected")
        }

        def result
        try {
            setAuthentication((Authentication)message[0])
            if (message[1] instanceof RevisionTransportCommand) {
                result = modelService.retrieveModelFile(Revision.get(message[1].id))
            } else {
                result = modelService.retrieveModelFile(Model.get(message[1].id))
            }
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
    @Profiled(tag="jmsAdapterService.grantReadAccess")
    def grantReadAccess(def message) {
        if (!verifyMessage(message, [Authentication, ModelTransportCommand, User])) {
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
    @Profiled(tag="jmsAdapterService.grantWriteAccess")
    def grantWriteAccess(def message) {
        if (!verifyMessage(message, [Authentication, ModelTransportCommand, User])) {
            return new IllegalArgumentException("Authentication, Model and User as arguments expected")
        }

        def result
        try {
            setAuthentication((Authentication)message[0])
            modelService.grantWriteAccess(Model.get(message[1].id), message[2])
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
    @Profiled(tag="jmsAdapterService.revokeReadAccess")
    def revokeReadAccess(def message) {
        if (!verifyMessage(message, [Authentication, ModelTransportCommand, User])) {
            return new IllegalArgumentException("Authentication, Model and User as arguments expected")
        }

        def result
        try {
            setAuthentication((Authentication)message[0])
            result = modelService.revokeReadAccess(Model.get(message[1].id), message[2])
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
    @Profiled(tag="jmsAdapterService.revokeWriteAccess")
    def revokeWriteAccess(def message) {
        if (!verifyMessage(message, [Authentication, ModelTransportCommand, User])) {
            return new IllegalArgumentException("Authentication, Model and User as arguments expected")
        }

        def result
        try {
            setAuthentication((Authentication)message[0])
            result = modelService.revokeWriteAccess(Model.get(message[1].id), message[2])
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
    @Profiled(tag="jmsAdapterService.deleteModel")
    def deleteModel(def message) {
        if (!verifyMessage(message, [Authentication, ModelTransportCommand])) {
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
    @Profiled(tag="jmsAdapterService.restoreModel")
    def restoreModel(def message) {
        if (!verifyMessage(message, [Authentication, ModelTransportCommand])) {
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
     * Wraper around UserService.changePassword
     * @param message List consisting of Authentication, oldPassword and newPassword
     * @return @c true if password changed, InvalidArgumentException of BadCredentialsException if old password is incorrect
     */
    @Queue
    @Profiled(tag="jmsAdapterService.changePassword")
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
    @Profiled(tag="jmsAdapterService.editUser")
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
    @Profiled(tag="jmsAdapterService.getCurrentUser")
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
    @Profiled(tag="jmsAdapterService.getUser")
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
    @Profiled(tag="jmsAdapterService.getAllUser")
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
    @Profiled(tag="jmsAdapterService.enableUser")
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
    @Profiled(tag="jmsAdapterService.lockAccount")
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
    @Profiled(tag="jmsAdapterService.expireAccount")
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
    @Profiled(tag="jmsAdapterService.expirePassword")
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
    @Profiled(tag="jmsAdapterService.register")
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
    @Profiled(tag="jmsAdapterService.validateRegistration")
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
