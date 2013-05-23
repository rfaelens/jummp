package net.biomodels.jummp.jms

import grails.plugin.jms.Queue
import net.biomodels.jummp.core.model.ModelFormatTransportCommand
import net.biomodels.jummp.core.model.ModelListSorting
import net.biomodels.jummp.core.model.ModelTransportCommand
import net.biomodels.jummp.core.model.PublicationTransportCommand
import net.biomodels.jummp.core.model.RevisionTransportCommand
import net.biomodels.jummp.plugins.security.User
import org.apache.commons.io.FileUtils
import org.springframework.security.access.AccessDeniedException
import net.biomodels.jummp.core.IModelService
import net.biomodels.jummp.webapp.ast.JmsAdapter
import net.biomodels.jummp.webapp.ast.JmsQueueMethod

/**
 * @short Wrapper class around the ModelService exposed to JMS.
 *
 * For more documentation about the idea of exporting a service to JMS please refer to
 * @link ApplicationJmsAdapterService.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
@JmsAdapter
class ModelJmsAdapterService extends AbstractJmsAdapter {

    @SuppressWarnings("GrailsStatelessService")
    static exposes = ['jms']
    @SuppressWarnings("GrailsStatelessService")
    static destination = "jummpModelJms"
    static transactional = false
    /**
     * Dependency injection of ModelDelegateService
     */
    IModelService modelDelegateService

    /**
     * Wrapper for ModelService.getAllModels
     * @param message List with first element the AuthenticationHash, further arguments as required
     * @return List of ModelTransportCommands or IllegalArgumentException for incorrect arguments
     */
    @Queue
    def getAllModels(def message) {
        if (!verifyMessage(message, [String.class]) &&
                !verifyMessage(message, [String.class, ModelListSorting.class]) &&
                !verifyMessage(message, [String.class, Integer.class, Integer.class]) &&
                !verifyMessage(message, [String.class, Integer.class, Integer.class, Boolean.class]) &&
                !verifyMessage(message, [String.class, Integer.class, Integer.class, ModelListSorting.class]) &&
                !verifyMessage(message, [String.class, Integer.class, Integer.class, Boolean.class, ModelListSorting.class])) {
            return new IllegalArgumentException("Invalid arguments passed to method. Allowed is AuthenticationHash or AuthenticationHash, Integer, Integer or AuthenticationHash, Integer, Integer, Boolean")
        }
        List arguments = (List)message
        def modelList = []
        // set authentication
        setAuthentication((String)arguments[0])
        switch (arguments.size()) {
        case 1:
            modelList = modelDelegateService.getAllModels()
            break
        case 2:
            modelList = modelDelegateService.getAllModels((ModelListSorting)arguments[1])
            break
        case 3:
            modelList = modelDelegateService.getAllModels((Integer)arguments[1], (Integer)arguments[2])
            break
        case 4:
            if (arguments[3] instanceof ModelListSorting) {
                modelList = modelDelegateService.getAllModels((Integer)arguments[1], (Integer)arguments[2], (ModelListSorting)arguments[3])
            } else {
                modelList = modelDelegateService.getAllModels((Integer)arguments[1], (Integer)arguments[2], (Boolean)arguments[3])
            }
            break
        case 5:
            modelList = modelDelegateService.getAllModels((Integer)arguments[1], (Integer)arguments[2], (Boolean)arguments[3], (ModelListSorting)arguments[4])
            break
        default:
            // nothing
            break
        }
        return modelList
    }

    /**
     * Wrapper for ModelService.getModelCount
     * @param message AuthenticationHash
     * @return Number of Models or IllegalArgumentException for missing AuthenticationHash
     */
    @Queue
    def getModelCount(def message) {
        if (!(message instanceof String)) {
            return new IllegalArgumentException("AuthenticationHash as argument expected")
        }
        def result = 0
        setAuthentication((String)message)
        result = modelDelegateService.getModelCount()
        return result
    }

    /**
     * Wrapper around ModelService.getLatestRevision
     * @param message List consisting of AuthenticationHash and ModelTransportCommand
     * @return A RevisionTransportCommand or IllegalArgumentException or AccessDeniedException if user does not have access to any Revision.
     */
    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[Long])
    def getLatestRevision(def message) {
        def revision = null
        revision = modelDelegateService.getLatestRevision(message[1])

        if (revision == null) {
            return new AccessDeniedException("No access to any revision of Model ${message[1].id}")
        } else {
            return revision
        }
    }

    /**
     * Wrapper around ModelService.getAllRevisions
     * @param message List consisting of AuthenticationHash and Model
     * @return List of Revisions wrapped in RevisionTransportCommand or IllegalArgumentException
     */
    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[Long])
    def getAllRevisions(def message) {
        def result = []
        result = modelDelegateService.getAllRevisions(message[1])
        return result
    }

    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[Long, Integer])
    def getRevision(def message) {
        return modelDelegateService.getRevision((Long)message[1], (Integer)message[2])
    }

    /**
     * Wrapper around ModelService.getPublication
     * @param message List consisting of AuthenticationHash and Model
     * @return PublicationTransportCommand or IllegalArgumentException or AccessDeniedException
     */
    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[Long])
    def getPublication(def message) {
        def result
        result = modelDelegateService.getPublication(message[1])
        if (!result) {
            result = new PublicationTransportCommand()
        }
        return result
    }

    /**
     * Wrapper around ModelService.uploadModel
     * @param message List consisting of AuthenticationHash, content of file and command object
     * @return New created Model as ModelTransportCommand, InvalidArgumentException, AccessDeniedException or ModelException
     */
    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[byte[], ModelTransportCommand])
    def uploadModel(def message) {
        def result

        File file = File.createTempFile("jummpJms", null)
        file.append(message[1])
        result = modelDelegateService.uploadModel(file, (ModelTransportCommand)message[2])
        FileUtils.deleteQuietly(file)

        return result
    }

    /**
     * Wrapper around ModelService.addRevision
     * @param message List consisting of AuthenticationHash, ModelTransportCommand, content of file as Byte Array, ModelFormatTransportCommand and String
     * @return New created Revision as RevisionTransportCommand, InvalidArgumentException, AccessDeniedException or ModelException
     */
    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[Long, byte[], ModelFormatTransportCommand, String])
    def addRevision(def message) {
        def result

        File file = File.createTempFile("jummpJms", null)
        file.append(message[2])
        result = modelDelegateService.addRevision(message[1], file, message[3], (String)message[4])
        FileUtils.deleteQuietly(file)

        return result
    }

    /**
     * Wrapper around ModelService.canAddRevision
     * @param message List consisting of AuthenticationHash and ModelTransportCommand
     * @return Boolean or IllegalArgumentException
     */
    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[Long])
    def canAddRevision(def message) {
        def result
        result = modelDelegateService.canAddRevision(message[1])
        return result
    }

    /**
     * Wrapper around ModelService.retrieveModelFile
     * @param message List consisting of AuthenticationHash and Revision
     * @return Byte Array, InvalidArgumentException, AccessDeniedException or ModelException
     */
    @Queue
    def retrieveModelFile(def message) {
        if (!verifyMessage(message, [String, RevisionTransportCommand]) &&
            !verifyMessage(message, [String, Long])) {
            return new IllegalArgumentException("AuthenticationHash and Revision or Model as arguments expected")
        }

        setAuthentication((String)message[0])
        if (message[1] instanceof RevisionTransportCommand) {
            return modelDelegateService.retrieveModelFile((RevisionTransportCommand)message[1])
        } else {
            return modelDelegateService.retrieveModelFile(message[1])
        }
    }

    /**
     * Wrapper around ModelService.grantReadAccess
     * @param message List consisting of AuthenticationHash, Model and User
     * @return @c true if successfully updated, InvalidArgumentException or AccessDeniedException
     */
    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[ModelTransportCommand, User])
    def grantReadAccess(def message) {
        modelDelegateService.grantReadAccess(message[1].id, message[2])
        return true
    }

    /**
     * Wrapper around ModelService.grantWriteAccess
     * @param message List consisting of AuthenticationHash, Model and User
     * @return @c true if successfully updated, InvalidArgumentException or AccessDeniedException
     */
    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[ModelTransportCommand, User])
    def grantWriteAccess(def message) {
        modelDelegateService.grantWriteAccess(message[1].id, message[2])
        return true
    }

    /**
     * Wrapper around ModelService.revokeReadAccess
     * @param message List consisting of AuthenticationHash, Model and User
     * @return @c true if successfully updated, @c false otherwise, InvalidArgumentException or AccessDeniedException
     */
    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[ModelTransportCommand, User])
    def revokeReadAccess(def message) {
        return modelDelegateService.revokeReadAccess(message[1].id, message[2])
    }

    /**
     * Wrapper around ModelService.revokeWriteAccess
     * @param message List consisting of AuthenticationHash, Model and User
     * @return @c true if successfully updated, @c false otherwise, InvalidArgumentException or AccessDeniedException
     */
    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[ModelTransportCommand, User])
    def revokeWriteAccess(def message) {
        return modelDelegateService.revokeWriteAccess(message[1].id, message[2])
    }

    /**
     * Wrapper around ModelService.deleteModel
     * @param message List consisting of AuthenticationHash and Model
     * @return @c true if successfully deleted, @c false otherwise, InvalidArgumentException or AccessDeniedException
     */
    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[Long])
    def deleteModel(def message) {
        return modelDelegateService.deleteModel(message[1])
    }

    /**
     * Wrapper around ModelService.deleteModel
     * @param message List consisting of AuthenticationHash and Model
     * @return @c true if successfully deleted, @c false otherwise, InvalidArgumentException or AccessDeniedException
     */
    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[Long])
    def restoreModel(def message) {
        return modelDelegateService.restoreModel(message[1])
    }

    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[Long, Integer])
    def deleteRevision(def message) {
        return modelDelegateService.deleteRevision(modelDelegateService.getRevision(message[1], message[2]))
    }

    @Queue
    @JmsQueueMethod(isAuthenticate=true, arguments=[Long, Integer])
    def publishModelRevision(def message) {
        modelDelegateService.publishModelRevision(modelDelegateService.getRevision(message[1], message[2]))
        return true
    }

}
