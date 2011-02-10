package net.biomodels.jummp.core.events

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * @short Annotation for defining that a method execution should be logged.
 *
 * If a method of a spring-managed bean is annotated with this Annotation
 * it's execution will be intercepted after-return and execute the
 * PostLoggingAdvice, which will broadcast a LoggingEvent. By using this
 * Annotation it is possible to specify that the method execution may be
 * logged.
 * @see PostLoggingAdvice
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PostLogging {
    /**
     *
     * @return The LoggingEventType of the annotated method
     */
    LoggingEventType value()
}
