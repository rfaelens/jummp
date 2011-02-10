package net.biomodels.jummp.core.events

/**
 * @short Enum defining the type of event to be logged.
 *
 * This enum is used by the PostLoggingAdvice to define what kind of method
 * has been executed, that is whether it retrieved data, updated data, created
 * new data and so on.
 * @see PostLoggingAdvice
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
public enum LoggingEventType {
    RETRIEVAL, ///< Data is retrieved either from database or VCS
    CREATION, ///< New Data is created (e.g. new Model uploaded)
    DELETION, ///< Existing Data is deleted or marked as deleted
    UPDATE ///< Existing Data is changed/updated
}
