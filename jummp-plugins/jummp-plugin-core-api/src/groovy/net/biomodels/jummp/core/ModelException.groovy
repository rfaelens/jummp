package net.biomodels.jummp.core

import net.biomodels.jummp.core.model.ModelTransportCommand

/**
 * @short Exception thrown when manipulating a Model fails.
 *
 * This exception should be thrown by all service methods manipulating Models or their revisions.
 * It indicates that the operation on the Model failed.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class ModelException extends JummpException implements Serializable {
    private static final long serialVersionUID = 1L
    /**
     * The Model which failed to be manipulated.
     */
    private ModelTransportCommand model

    public ModelException(ModelTransportCommand model) {
        this(model, "Failed to manipulate Model ${model?.id}".toString())
    }

    public ModelException(ModelTransportCommand model, String message) {
        super(message)
        this.model = model
    }

    public ModelException(ModelTransportCommand model, Throwable cause) {
        this(model, "Failed to manipulate Model ${model?.id}".toString(), cause)
    }

    public ModelException(ModelTransportCommand model, String message, Throwable cause) {
        super(message, cause)
        this.model = model
    }

    /**
     *
     * @return The Model whose manipulation failed
     */
    public ModelTransportCommand getModel() {
        return model
    }
}
