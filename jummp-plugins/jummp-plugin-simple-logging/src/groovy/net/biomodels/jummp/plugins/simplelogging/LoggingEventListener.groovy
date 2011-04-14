package net.biomodels.jummp.plugins.simplelogging

import net.biomodels.jummp.core.events.LoggingEvent
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationListener
import org.apache.log4j.Logger

/**
 * @short Listener for LoggingEvents.
 *
 * This is an example class for showing how a plugin can be notified about
 * LoggingEvents and handle them. This listener just logs all events in a not
 * very useful manner.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class LoggingEventListener implements ApplicationListener {
    /**
     * The logger for this class
     */
    Logger log = Logger.getLogger(getClass())

    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof LoggingEvent) {
            log.info("$event.user did $event.source")
        }
    }
}
