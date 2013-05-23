package net.biomodels.jummp.core.events

import net.biomodels.jummp.core.model.ModelVersionTransportCommand
import java.util.List

/**
 * @short Event triggered when a new ModelVersion is uploaded.
 *
 * This event is triggered by the ModelService when a user successfully uploaded
 * a new ModelVersion for an existing Model. It is not triggered during upload of a
 * new Model! Interested parties might listen to this event through an
 * ApplicationListener and use the information provided. It is not possible to
 * alter the ModelVersion or the uploaded file in any way. Instead of the ModelVersion
 * a ModelVersionTransportCommand is included in the event.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class ModelVersionCreatedEvent extends JummpEvent {
    /**
     * The newly created ModelVersion
     */
    final ModelVersionTransportCommand version
    /**
     * The actual file(s) which was uploaded.
     */
    final List<File> files 

    ModelVersionCreatedEvent(Object source, final ModelVersionTransportCommand version, final List<File> files) {
        super(source)
        this.version = version
        this.files = files
    }
}
