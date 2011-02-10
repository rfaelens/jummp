package net.biomodels.jummp.core.events

import org.springframework.context.ApplicationEvent

/**
 * @short Base class for all JummpEvents.
 *
 * Like the parent class abstract as we need to be more specific what an event
 * should look like.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
abstract class JummpEvent extends ApplicationEvent {

    JummpEvent(Object source) {
        super(source)
    }
}
