package net.biomodels.jummp.core.events

import net.biomodels.jummp.core.model.RevisionTransportCommand

/**
 * @short Event triggered when a new Model Revision is uploaded.
 *
 * This event is triggered by the ModelService when a user successfully uploaded
 * a new Revision for an existing Model. It is not triggered during upload of a
 * new Model! Interested parties might listen to this event through an
 * ApplicationListener and use the information provided. It is not possible to
 * alter the Revision or the uploaded files in any way. Instead of the Revision
 * a RevisionTransportCommand is included in the event.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
class RevisionCreatedEvent extends JummpEvent {
    /**
     * The newly create Revision
     */
    final RevisionTransportCommand revision
    /**
     * The actual files which were uploaded.
     */
    final List<File> files

    RevisionCreatedEvent(Object source, final RevisionTransportCommand revision, final List<File> files) {
        super(source)
        this.revision = revision
        this.files = files
    }
}
