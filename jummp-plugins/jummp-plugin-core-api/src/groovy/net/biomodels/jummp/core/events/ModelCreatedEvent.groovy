package net.biomodels.jummp.core.events

import net.biomodels.jummp.core.model.ModelTransportCommand

/**
 * @short Event triggered when a new Model is uploaded.
 *
 * This event is triggered by the ModelService when a user successfully uploaded
 * a new Model. Interested parties might listen to this event through an ApplicationListener
 * and use the information provided. It is not possible to alter the Model or the uploaded
 * files in any way. Instead of the Model a ModelTransportCommand is included in the event.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
class ModelCreatedEvent extends JummpEvent {
    /**
     * The newly created model.
     */
    final ModelTransportCommand model
    /**
     * The actual files which were uploaded.
     */
    final List<File> files

    ModelCreatedEvent(Object source, final ModelTransportCommand model, final List<File> files) {
        super(source)
        this.model = model
        this.files = files
    }
}
