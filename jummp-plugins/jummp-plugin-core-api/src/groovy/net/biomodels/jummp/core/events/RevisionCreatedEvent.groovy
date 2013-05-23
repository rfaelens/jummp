package net.biomodels.jummp.core.events

import net.biomodels.jummp.core.model.RevisionTransportCommand

/**
 * @short Event triggered when a new Model Revision is uploaded.
 *
 * This event is triggered by the ModelService when a user successfully uploaded
 * a new Revision for an existing Model. It is not triggered during upload of a
 * new Model! Interested parties might listen to this event through an
 * ApplicationListener and use the information provided. It is not possible to
 * alter the Revision or the uploaded file in any way. Instead of the Revision
 * a RevisionTransportCommand is included in the event.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class RevisionCreatedEvent extends JummpEvent {
    /**
     * The newly create Revision
     */
    final RevisionTransportCommand revision
    /**
     * The actual file which was uploaded.
     */
    final File file

    RevisionCreatedEvent(Object source, final RevisionTransportCommand revision, final File file) {
        super(source)
        this.revision = revision
        this.file = file
    }
}
