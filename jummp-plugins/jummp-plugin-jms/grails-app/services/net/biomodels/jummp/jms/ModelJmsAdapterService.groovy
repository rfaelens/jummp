package net.biomodels.jummp.jms

import grails.plugin.jms.Queue
import net.biomodels.jummp.core.ModelException
import net.biomodels.jummp.core.model.ModelFormatTransportCommand
import net.biomodels.jummp.core.model.ModelListSorting
import net.biomodels.jummp.core.model.ModelTransportCommand
import net.biomodels.jummp.core.model.PublicationTransportCommand
import net.biomodels.jummp.core.model.RevisionTransportCommand
import net.biomodels.jummp.plugins.security.User
import org.apache.commons.io.FileUtils
import org.perf4j.aop.Profiled
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.Authentication
import net.biomodels.jummp.jms.AbstractJmsAdapter
import net.biomodels.jummp.core.IModelService

/**
 * @short Wrapper class around the ModelService exposed to JMS.
 *
 * For more documentation about the idea of exporting a service to JMS please refer to
 * @link ApplicationJmsAdapterService.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class ModelJmsAdapterService extends AbstractJmsAdapter {

    @SuppressWarnings("GrailsStatelessService")
    static exposes = ['jms']
    @SuppressWarnings("GrailsStatelessService")
    static destination = "jummpModelJms"
    static transactional = false
    /**
     * Dependency injection of ModelService
     */
    IModelService modelService

    /**
     * Wrapper for ModelService.getAllModels
     * @param message List with first element the Authentication, further arguments as required
     * @return List of ModelTransportCommands or IllegalArgumentException for incorrect arguments
     */
    @Queue
    @Profiled(tag="modelJmsAdapterService.getAllModels")
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
        List<ModelTransportCommand> modelList = []
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
        return modelList
    }

    /**
     * Wrapper for ModelService.getModelCount
     * @param message Authentication
     * @return Number of Models or IllegalArgumentException for missing Authentication
     */
    @Queue
    @Profiled(tag="modelJmsAdapterService.getModelCount")
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
    @Profiled(tag="modelJmsAdapterService.getLatestRevision")
    def getLatestRevision(def message) {
        if (!verifyMessage(message, [Authentication, ModelTransportCommand])) {
            return new IllegalArgumentException("Authentication and Model as arguments expected")
        }
        RevisionTransportCommand revision = null
        try {
            setAuthentication((Authentication)message[0])
            revision = modelService.getLatestRevision(message[1])
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
     * @return List of Revisions wrapped in RevisionTransportCommand or IllegalArgumentException
     */
    @Queue
    @Profiled(tag="modelJmsAdapterService.getAllRevisions")
    def getAllRevisions(def message) {
        if (!verifyMessage(message, [Authentication, ModelTransportCommand])) {
            return new IllegalArgumentException("Authentication and Model as arguments expected")
        }
        List<RevisionTransportCommand> result = []
        try {
            setAuthentication((Authentication)message[0])
            result = modelService.getAllRevisions(message[1])
        } finally {
            restoreAuthentication()
        }
        return result
    }

    /**
     * Wrapper around ModelService.getPublication
     * @param message List consisting of Authentication and Model
     * @return PublicationTransportCommand or IllegalArgumentException or AccessDeniedException
     */
    @Queue
    @Profiled(tag="modelJmsAdapterService.getPublication")
    def getPublication(def message) {
        if (!verifyMessage(message, [Authentication, ModelTransportCommand])) {
            return new IllegalArgumentException("Authentication and Model as arguments expected")
        }
        def result
        try {
            setAuthentication((Authentication)message[0])
            result = modelService.getPublication(message[1])
        } catch (AccessDeniedException e) {
            result = e
        } catch (IllegalArgumentException e) {
            result = e
        } finally {
            restoreAuthentication()
        }
        if (!result) {
            result = new PublicationTransportCommand()
        }
        return result
    }

    /**
     * Wrapper around ModelService.uploadModel
     * @param message List consisting of Authentication, content of file and command object
     * @return New created Model as ModelTransportCommand, InvalidArgumentException, AccessDeniedException or ModelException
     */
    @Queue
    @Profiled(tag="modelJmsAdapterService.uploadModel")
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
            result = modelService.uploadModel(file, (ModelTransportCommand)message[2])
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
    @Profiled(tag="modelJmsAdapterService.addRevision")
    def addRevision(def message) {
        if (!verifyMessage(message, [Authentication, ModelTransportCommand, byte[], ModelFormatTransportCommand, String])) {
            return new IllegalArgumentException("Authentication, Model, Byte Array, ModelFormatTransportCommand and String as arguments expected")
        }
        def result

        try {
            setAuthentication((Authentication)message[0])
            File file = File.createTempFile("jummpJms", null)
            file.append(message[2])
            result = modelService.addRevision(message[1], file, message[3], (String)message[4])
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
    @Profiled(tag="modelJmsAdapterService.canAddRevision")
    def canAddRevision(def message) {
        if (!verifyMessage(message, [Authentication, ModelTransportCommand])) {
            return new IllegalArgumentException("Authentication and Model as arguments expected")
        }

        def result
        try {
            setAuthentication((Authentication)message[0])
            result = modelService.canAddRevision(message[1])
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
    @Profiled(tag="modelJmsAdapterService.retrieveModelFile")
    def retrieveModelFile(def message) {
        if (!verifyMessage(message, [Authentication, RevisionTransportCommand]) &&
            !verifyMessage(message, [Authentication, ModelTransportCommand])) {
            return new IllegalArgumentException("Authentication and Revision or Model as arguments expected")
        }

        def result
        try {
            setAuthentication((Authentication)message[0])
            if (message[1] instanceof RevisionTransportCommand) {
                result = modelService.retrieveModelFile(message[1].id)
            } else {
                result = modelService.retrieveModelFile(message[1].id)
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
    @Profiled(tag="modelJmsAdapterService.grantReadAccess")
    def grantReadAccess(def message) {
        if (!verifyMessage(message, [Authentication, ModelTransportCommand, User])) {
            return new IllegalArgumentException("Authentication, Model and User as arguments expected")
        }

        def result
        try {
            setAuthentication((Authentication)message[0])
            modelService.grantReadAccess(message[1], message[2])
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
    @Profiled(tag="modelJmsAdapterService.grantWriteAccess")
    def grantWriteAccess(def message) {
        if (!verifyMessage(message, [Authentication, ModelTransportCommand, User])) {
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
    @Profiled(tag="modelJmsAdapterService.revokeReadAccess")
    def revokeReadAccess(def message) {
        if (!verifyMessage(message, [Authentication, ModelTransportCommand, User])) {
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
    @Profiled(tag="modelJmsAdapterService.revokeWriteAccess")
    def revokeWriteAccess(def message) {
        if (!verifyMessage(message, [Authentication, ModelTransportCommand, User])) {
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
    @Profiled(tag="modelJmsAdapterService.deleteModel")
    def deleteModel(def message) {
        if (!verifyMessage(message, [Authentication, ModelTransportCommand])) {
            return new IllegalArgumentException("Authentication and Model as arguments expected")
        }

        def result
        try {
            setAuthentication((Authentication)message[0])
            result = modelService.deleteModel(message[1])
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
    @Profiled(tag="modelJmsAdapterService.restoreModel")
    def restoreModel(def message) {
        if (!verifyMessage(message, [Authentication, ModelTransportCommand])) {
            return new IllegalArgumentException("Authentication and Model as arguments expected")
        }

        def result
        try {
            setAuthentication((Authentication)message[0])
            result = modelService.restoreModel(message[1])
        } catch (AccessDeniedException e) {
            result = e
        } finally {
            restoreAuthentication()
        }
        return result
    }

}
