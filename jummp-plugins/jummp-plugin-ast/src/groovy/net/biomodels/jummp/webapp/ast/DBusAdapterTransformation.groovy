package net.biomodels.jummp.webapp.ast

import groovyjarjarasm.asm.Opcodes
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.ast.stmt.TryCatchStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.stmt.CatchStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.stmt.ThrowStatement
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.VariableScope

/**
 * @short AST Transformation for Core DBus Adapter.
 *
 * The Transformation is triggered by the @link DBusAdapter annotation on the class and generates code for all
 * methods annotated with the @link DBusMethod annotation.
 *
 * @see DBusAdapter
 * @see DBusMethod
 * @author Martin Gräßlin <m.graesslin@dkfz.de>
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
class DBusAdapterTransformation implements ASTTransformation {

    void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        // The name of the interface whose methods needs to be implemented, is the value of the Annotation triggering the AST transformation
        AnnotationNode annotationNode = (AnnotationNode)astNodes[0]
        String interfaceName = annotationNode.getMember("interfaceName").getValue()
        // the class has a field with the same name as the serviceName value on the annotation
        String serviceName = annotationNode.getMember("serviceName").getValue()
        ClassNode classNode = ((ClassNode)astNodes[1])

        // search for the interface matching our annotation value
        ClassNode serviceInterface = null
        classNode.getInterfaces().each {
            if (it.getNameWithoutPackage() == interfaceName) {
                serviceInterface = it
            }
        }
        if (!serviceInterface) {
            // we did not find our interface, so return
            return
        }

        FieldNode serviceField = null
        classNode.getFields().each {
            if (it.name == serviceName) {
                serviceField = it
            }
        }
        if (!serviceField) {
            println "Service Field not found"
            return
        }

        classNode.getMethods().each {
            // search for DBusMethod annotation, if not present we do not generate code for the method
            List<AnnotationNode> dbusMethodAnnotations = it.getAnnotations(new ClassNode(DBusMethod))
            if (dbusMethodAnnotations.isEmpty()) {
                return
            }
            BlockStatement code = new BlockStatement()
            // do we need to add an setAuthentication statement?
            boolean authenticate = false
            if (dbusMethodAnnotations.first().getMember("isAuthenticate")) {
                authenticate = dbusMethodAnnotations.first().getMember("isAuthenticate")?.getValue()
            }
            if (authenticate) {
                code.addStatement(setAuthentication(it.parameters[0].name))
            }
            if (dbusMethodAnnotations.first().getMember("isAnonymous")?.getValue()) {
                code.addStatement(new ExpressionStatement(new MethodCallExpression(new VariableExpression("this"), "setAnonymousAuthentication", ArgumentListExpression.EMPTY_ARGUMENTS)))
            }
            // generate the list of arguments for the delegated method call
            List arguments = []
            // if there is a getRevision member in the annotation we use it to generate a getRevision call to modelDelegateService
            // the list contains the indices to the parameters which go into the getRevision call
            List revisionParameters = dbusMethodAnnotations.first().getMember("getRevision")?.getExpressions()
            if (revisionParameters) {
                List revisionArguments = []
                revisionParameters.each { id ->
                    revisionArguments << new VariableExpression(it.parameters[id.getValue()].name)
                }
                arguments << new MethodCallExpression(new VariableExpression("modelDelegateService"), "getRevision", new ArgumentListExpression(revisionArguments))
            }
            // normal parameter wrapping
            it.parameters.eachWithIndex { parameter, i ->
                // skip the first argument, if it is the authentication hash
                if (authenticate && i == 0) {
                    return
                }
                if (revisionParameters && revisionParameters.find { it.getValue() == i }) {
                    // skip parameters which are already used to get the revision
                    return
                }
                if (parameter.type.nameWithoutPackage == "DBusUser") {
                    // convert DBusUser to User
                    arguments << new MethodCallExpression(new VariableExpression(parameter.name), "toUser", ArgumentListExpression.EMPTY_ARGUMENTS)
                } else {
                    // no conversion, just path through
                    arguments << new VariableExpression(parameter.name)
                }
            }
            // add the delegated method call
            code.addStatement delegatedMethodCall(serviceName, it, arguments, dbusMethodAnnotations.first().getMember("delegate")?.getValue(), dbusMethodAnnotations.first().getMember("collect")?.getValue())
            // wrap everything in the tryCatchFinally Statement and replace the code of the method
            it.setCode tryCatchFinallyStatement(code)
        }
    }

    /**
     * Generates the setAuthentication call:
     * setAuthentication(variable)
     * @param variable The variable with the authentication hash
     * @return The code for the method call
     */
    private Statement setAuthentication(String variable) {
        return new ExpressionStatement(new MethodCallExpression(new VariableExpression("this"), "setAuthentication", new ArgumentListExpression(new VariableExpression(variable))))
    }

    /**
     * Generates the call to the service.
     * The method takes care of the conversion of types to:
     * @li DBusModel
     * @li DBusRevision
     * @li DBusPublication
     *
     * Additionally it also takes care of not returning anything if the method's return type is void.
     * If the return type is a list, the method can generate to collect on one of the return values.
     *
     * @param serviceName The name of the service variable
     * @param method The method for which the code is generated
     * @param arguments The list of arguments
     * @param delegate Optional name of the method to delegate to
     * @param collect Optional name of the field to collect list values on
     * @return The code for the delegated method call
     */
    private Statement delegatedMethodCall(String serviceName, MethodNode method, List arguments, String delegate, String collect) {
        String methodName = method.name
        if (delegate) {
            methodName = delegate
        }
        // if our DBusMethod Annotation has the field delegate use this as method name
        MethodCallExpression methodCall = new MethodCallExpression(new VariableExpression(serviceName), methodName, arguments.isEmpty() ? ArgumentListExpression.EMPTY_ARGUMENTS : new ArgumentListExpression(arguments))
        if (method.returnType.nameWithoutPackage == "DBusModel") {
            methodCall = new MethodCallExpression(new ClassExpression(method.returnType), "fromModelTransportCommand", new ArgumentListExpression(methodCall))
        }
        if (method.returnType.nameWithoutPackage == "DBusRevision") {
            methodCall = new MethodCallExpression(new ClassExpression(method.returnType), "fromRevisionTransportCommand", new ArgumentListExpression(methodCall))
        }
        if (method.returnType.nameWithoutPackage == "DBusPublication") {
            methodCall = new MethodCallExpression(new ClassExpression(method.returnType), "fromPublicationTransportCommand", new ArgumentListExpression(methodCall))
        }
        if (method.returnType.nameWithoutPackage == "DBusRole") {
            methodCall = new MethodCallExpression(new ClassExpression(method.returnType), "fromRole", new ArgumentListExpression(methodCall))
        }
        if (method.returnType.nameWithoutPackage == "DBusUser") {
            methodCall = new MethodCallExpression(new ClassExpression(method.returnType), "fromUser", new ArgumentListExpression(methodCall))
        }
        if (collect) {
            methodCall = generateCollectList(methodCall, collect)
        }
        if (method.returnType.name == Void.TYPE.toString()) {
            return new ExpressionStatement(methodCall)
        } else {
            return new ReturnStatement(methodCall)
        }
    }

    /**
     * Wraps the @p statement in a try-catch-finally block.
     *
     * Generates the following code:
     * try {
     *     statement
     * } catch (Exception e) {
     *     throw exceptionMapping(e)
     * } finally {
     *     restoreAuthentication()
     * }
     * @param statement
     * @return
     */
    private Statement tryCatchFinallyStatement(Statement statement) {
        if (!statement) {
            return statement
        }
        TryCatchStatement tc = new TryCatchStatement(statement, new ExpressionStatement(new MethodCallExpression(new VariableExpression("this"), "restoreAuthentication", ArgumentListExpression.EMPTY_ARGUMENTS)))
        CatchStatement catchStatement = new CatchStatement(new Parameter(new ClassNode(Exception), "e"), new Statement())
        catchStatement.setCode(new ThrowStatement(new MethodCallExpression(new VariableExpression("this"), "exceptionMapping", new ArgumentListExpression(new VariableExpression("e")))))
        tc.addCatch(catchStatement)
        return tc
    }

    /**
     * Generates the following code:
     * @code
     * original.collect { it.${field} }
     * @endcode
     *
     * The generated code is without dynamic field resolving.
     *
     * @param original The original method call to take as the base for the collect.
     * @param field The name of the field to collect
     * @return The generated MethodCallExpression
     */
    private MethodCallExpression generateCollectList(MethodCallExpression original, String field) {
        BlockStatement code = new BlockStatement()
        code.addStatement(new ExpressionStatement(new MethodCallExpression(new PropertyExpression(new VariableExpression("it"), field), "toString", ArgumentListExpression.EMPTY_ARGUMENTS)))
        ClosureExpression closure = new ClosureExpression(Parameter.EMPTY_ARRAY, code)
        closure.setVariableScope(new VariableScope())
        return new MethodCallExpression(original, "collect", new ArgumentListExpression(closure))
    }
}
