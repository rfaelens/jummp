package net.biomodels.jummp.webapp.ast

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target
import org.codehaus.groovy.transform.GroovyASTTransformationClass

/**
 * @short Marker Annotation to trigger a @link RemoteDBusAdapterTransformation.
 *
 * The annotation may only be used for classes and it triggers an AST Transformation.
 * The annotation has two parameters:
 * @li interfaceName: The name of the interface for which methods should be generated
 * @li dbusAdapterName: The name of the field holding the reference to the DBusAdapter
 *
 * @see RemoteDBusAdapterTransformation
 * @author Martin Gräßlin <m.graesslin@dkfz.de>
 */
@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.TYPE])
@GroovyASTTransformationClass(["net.biomodels.jummp.webapp.ast.RemoteDBusAdapterTransformation"])
public @interface RemoteDBusAdapter {
    String interfaceName()
    String dbusAdapterName()
}