package net.biomodels.jummp.core.events

import java.lang.reflect.Method
import org.springframework.aop.AfterReturningAdvice
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.security.core.context.SecurityContextHolder

/**
 * @short Advice for logging events post method execution.
 *
 * The advice is used as an advisor for methods annotated by the
 * PostLogging Annotation. It's main purpose is to create a LoggingEvent
 * broadcasted to all interested parties. The LoggingEvent informs about
 * which method was executed with which arguments and the return value.
 * Additionally from the Annotation the LoggingEventType is retrieved and
 * from the SpringSecurityContext the user who accessed the method.
 * 
 * @see LoggingEventType
 * @see PostLogging
 * @see LoggingEvent
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class PostLoggingAdvice implements AfterReturningAdvice, ApplicationContextAware {
    /**
     * The application context needed for publishing events
     */
    private ApplicationContext ctx

    public void setApplicationContext(ApplicationContext applicationContext) {
        ctx = applicationContext
    }

    public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
        PostLogging annotation = (PostLogging)method.getAnnotation(PostLogging)
        LoggingEvent event = new LoggingEvent(method,
                SecurityContextHolder.context.authentication.principal.toString(),
                returnValue, args, annotation.value())
        ctx.publishEvent(event)
    }
}
