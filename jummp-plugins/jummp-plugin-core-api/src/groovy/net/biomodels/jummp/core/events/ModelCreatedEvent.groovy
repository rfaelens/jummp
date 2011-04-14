package net.biomodels.jummp.core.events

import net.biomodels.jummp.core.model.ModelTransportCommand

/**
 * @short Event triggered when a new Model is uploaded.
 *
 * This event is triggered by the ModelService when a user successfully uploaded
 * a new Model. Interested parties might listen to this event through an ApplicationListener
 * and use the information provided. It is not possible to alter the Model or the uploaded
 * file in any way. Instead of the Model a ModelTransportCommand is included in the even.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class ModelCreatedEvent extends JummpEvent {
    /**
     * The newly created model.
     */
    final ModelTransportCommand model
    /**
     * The actual file which was uploaded.
     */
    final File file

    ModelCreatedEvent(Object source, final ModelTransportCommand model, final File file) {
        super(source)
        this.model = model
        this.file = file
    }
}
