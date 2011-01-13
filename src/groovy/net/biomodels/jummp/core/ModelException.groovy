package net.biomodels.jummp.core

import net.biomodels.jummp.model.Model

/**
 * @short Exception thrown when manipulating a Model fails.
 *
 * This exception should be thrown by all service methods manipulating Models or their revisions.
 * It indicates that the operation on the Model failed.
 * @see Model
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class ModelException extends JummpException {
    /**
     * The Model which failed to be manipulated.
     */
    private Model model

    public ModelException(Model model) {
        this(model, "Failed to manipulate Model ${model?.id}")
    }

    public ModelException(Model model, String message) {
        super(message)
        this.model = model
    }

    public ModelException(Model model, Throwable cause) {
        this(model, "Failed to manipulate Model ${model?.id}", cause)
    }

    public ModelException(Model model, String message, Throwable cause) {
        super(message, cause)
        this.model = model
    }

    /**
     *
     * @return The Model whose manipulation failed
     */
    public Model getModel() {
        return model
    }
}
