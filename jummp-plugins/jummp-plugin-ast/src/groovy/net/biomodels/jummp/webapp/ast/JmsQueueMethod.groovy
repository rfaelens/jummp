package net.biomodels.jummp.webapp.ast

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * @short Marker Annotation to generate a verifyMessage block in a JMS Queue method.
 *
 * The annotation may only be used for methods in classes annotated with @link JmsAdapter.
 *
 * If present the AST Transformation generates an if block in the method to verify if the message
 * matches the types specified by this annotation.
 *
 * The annotation takes two parameters:
 * @li isAuthenticate to indicate whether the generated code has to check for the AuthenticationHash
 * @li arguments Array of Classes of parameters <b>without</b> the AuthenticationHash 
 *
 * @see JmsAdapter
 * @author Martin Gräßlin <m.graesslin@dkfz.de>
 */
@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.METHOD])
public @interface JmsQueueMethod {
    boolean isAuthenticate()
    Class[] arguments()
}
