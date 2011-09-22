package net.biomodels.jummp.model

import net.biomodels.jummp.plugins.security.User

/**
 * @short Domain class for storing when a user accessed a Model.
 * 
 * A history item consists of a Model, the user who accessed it and
 * when it was last accessed.
 * 
 * Do not use this domain class directly, but the ModelHistoryService.
 * This service ensures that the History is correct.
 *
 * @author Martin Graesslin <m.graesslin@dkfz.de>
 * @see net.biomodels.jummp.core.ModelHistoryService
 */
class ModelHistoryItem implements Serializable {
    /**
     * The Model the user accessed
     */
    Model model
    /**
     * The User who accessed the Model
     */
    User user
    /**
     * The Timestamp of last access to this history element
     */
    long lastAccessedDate

    static mapping = {
        id(composite:['model', 'user'])
        // disable the version field as not needed in this domain class
        version false
    }

    /**
     * Updates the last accessed date of this history item.
     */
    public void touch() {
        this.lastAccessedDate = System.currentTimeMillis()
        save(flush: true)
    }

    /**
     * Factory method to ensure that the history item is setup correctly.
     * Always use this method instead of the constructor.
     * @param model The Model which has to be in the history
     * @param user The User who accessed the Model
     * @return A history item for the Model and User
     */
    static ModelHistoryItem create(Model model, User user) {
        ModelHistoryItem item = new ModelHistoryItem()
        item.model = model
        item.user = user
        item.lastAccessedDate = System.currentTimeMillis()
        return item
    }
}
