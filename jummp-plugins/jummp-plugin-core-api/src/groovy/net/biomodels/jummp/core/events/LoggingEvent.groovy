package net.biomodels.jummp.core.events

/**
 * @short Event class for intercepted methods whose execution may be logged.
 *
 * This event is triggered by the PostLoggingAdvice whenever a method got intercepted.
 * Interested parties may listen to this Event and log it. It contains all important
 * information such as who executed the method, when, with what arguments and what
 * were the returned results.
 *
 * @see LoggingEventType
 * @see PostLoggingAdvice
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class LoggingEvent extends JummpEvent {
    /**
     * The name of the user who executed a method.
     */
    String user
    /**
     * The return value of the executed method
     */
    Object returnValue
    /**
     * The arguments passed into the executed method.
     */
    Object[] arguments
    /**
     * The type of the logging event.
     */
    net.biomodels.jummp.core.events.LoggingEventType type

    /**
     * Constructor for a LoggingEvent raised when an intercepted method was executed.
     * @param source The Method object which was intercepted.
     * @param user The name of the user who executed the method
     * @param returnValue The return value of the executed method
     * @param args The arguments passed into the executed method
     * @param type The type of the executed method
     */
    LoggingEvent(Object source, String user, Object returnValue, Object[] args, net.biomodels.jummp.core.events.LoggingEventType type) {
        super(source)
        this.user = user
        this.returnValue = returnValue
        this.arguments = args
        this.type = type
    }
}
