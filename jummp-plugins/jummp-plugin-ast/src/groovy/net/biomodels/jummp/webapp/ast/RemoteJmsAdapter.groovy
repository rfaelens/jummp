package net.biomodels.jummp.webapp.ast

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target
import org.codehaus.groovy.transform.GroovyASTTransformationClass

/**
 * @short Marker Annotation to trigger a @link RemoteJmsAdapterTransformation.
 *
 * Takes the name of the RemoteAdapter Interface as the value.
 */
@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.TYPE])
@GroovyASTTransformationClass(["net.biomodels.jummp.webapp.ast.RemoteJmsAdapterTransformation"])
public @interface RemoteJmsAdapter {
    String value()
}