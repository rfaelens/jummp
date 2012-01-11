package net.biomodels.jummp.core

import net.biomodels.jummp.core.model.ModelTransportCommand
import net.biomodels.jummp.model.Model
import net.biomodels.jummp.model.ModelHistoryItem
import net.biomodels.jummp.plugins.security.User
import org.perf4j.aop.Profiled

/**
 * @short This class provides the service for the per user Model History.
 * 
 * Whenever a user accesses a Model other services can use this service to
 * track that the user accessed the Model. The Model History itself is a
 * queue of the most recently used Models. The service provides methods to
 * retrieve the list of most recently accessed Models.
 * 
 * The Model History functionality can be disabled through the config option
 * jummp.model.history.maxElements and is implicitly disabled for not logged in
 * users.
 *
 * @author Martin Graesslin <m.graesslin@dkfz.de>
 * @see ModelHistoryItem
 */
class ModelHistoryService {
    /**
     * Dependency Injection of Spring Security Service
     */
    def springSecurityService
    /**
     * Dependency Injection of Grails Application
     */
    @SuppressWarnings("GrailsStatelessService")
    def grailsApplication

    /**
     * Adds a Model to the user's Model history.
     * If the Model is already included in the user's history, it will only be touched,
     * that is, it becomes the most recent accessed Model.
     *
     * If the history contains already the maximum number of elements, the oldest Model
     * is removed from the history.
     * 
     * If the user is not logged in or if the feature is disabled completely this method
     * does nothing.
     * @param model The Model to add to the current user's history
     */
    @Profiled(tag="modelHistoryService.addModelToHistory")
    void addModelToHistory(Model model) {
        // don't do anything for anonymous Users
        if (!springSecurityService.isLoggedIn()) {
            return
        }
        final int maxNumber = grailsApplication.config.jummp.model.history.maxElements
        if (maxNumber == 0) {
            // Model History is disabled - do not return list
            return
        }
        User user = User.findByUsername(springSecurityService.authentication.name)
        ModelHistoryItem item = ModelHistoryItem.findByUserAndModel(user, model)
        if (item) {
            // we have already an item for this user and model: just touch the date
            item.touch()
            return
        }
        // test the number of items in the users history
        while (ModelHistoryItem.countByUser(user) >= maxNumber) {
            // exceeded the maximum number - drop oldest item
            // TODO: write a proper query to faster delete the item
            ModelHistoryItem.findAllByUser(user).sort { a, b -> a.lastAccessedDate <=> b.lastAccessedDate }.first().delete()
        }
        ModelHistoryItem newItem = ModelHistoryItem.create(model, user)
        newItem.save(flush: true)
    }

    /**
     * 
     * @return Latest accessed Model for current user
     */
    @Profiled(tag="modelHistoryService.lastAccessedModel")
    ModelTransportCommand lastAccessedModel() {
        // don't do anything for anonymous Users
        if (!springSecurityService.isLoggedIn()) {
            return new ModelTransportCommand()
        }
        if (grailsApplication.config.jummp.model.history.maxElements == 0) {
            // if feature disabled, return empty ModelTransportCommand
            return new ModelTransportCommand()
        }
        User user = User.findByUsername(springSecurityService.authentication.name)
        List<ModelHistoryItem> history = ModelHistoryItem.findAllByUser(user) as List<ModelHistoryItem>
        if (history.isEmpty()) {
            // history is empty
            return new ModelTransportCommand()
        }
        return history.sort { it.lastAccessedDate }.last().model.toCommandObject()
    }

    /**
     * 
     * @return The current user's Model history
     */
    @Profiled(tag="modelHistoryService.history")
    List<ModelTransportCommand> history() {
        // don't do anything for anonymous Users
        if (!springSecurityService.isLoggedIn()) {
            return []
        }
        if (grailsApplication.config.jummp.model.history.maxElements == 0) {
            // if feature disabled, return empty list
            return []
        }
        User user = User.findByUsername(springSecurityService.authentication.name)
        List<ModelHistoryItem> history = ModelHistoryItem.findAllByUser(user).sort { it.lastAccessedDate }
        List<ModelTransportCommand> retList = []
        history.reverseEach {
            retList << it.model.toCommandObject()
        }
        return retList
    }
}
