package net.biomodels.jummp.webapp.ast

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target
import org.codehaus.groovy.transform.GroovyASTTransformationClass

/**
 * @short Marker Annotation to trigger a @link DBusAdapterTransformation.
 *
 * The annotation may only be used for classes and it triggers an AST Transformation.
 * The transformation generates methods for DBusAdapters.
 *
 * @see DBusAdapterTransformation
 * @author Martin Gräßlin <m.graesslin@dkfz.de>
 */
@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.TYPE])
@GroovyASTTransformationClass(["net.biomodels.jummp.webapp.ast.DBusAdapterTransformation"])
public @interface DBusAdapter {
    String interfaceName()
    String serviceName()
}
