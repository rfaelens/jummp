package net.biomodels.jummp.webapp.ast

import groovyjarjarasm.asm.Opcodes
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.VariableScope
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.syntax.Token
import org.codehaus.groovy.syntax.Types
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.ast.stmt.TryCatchStatement
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.stmt.ThrowStatement
import org.codehaus.groovy.ast.stmt.CatchStatement
import org.codehaus.groovy.ast.stmt.EmptyStatement

/**
 * @short AST Transformation to generate code for remote DBus Adapters.
 *
 * This class provides the AST Transformation (http://groovy.codehaus.org/Compile-time+Metaprogramming+-+AST+Transformations)
 * to implement all the delegating methods in remote DBus Adapters to the core. Each remote DBus Adapter implements an
 * Remote*Adapter interface and delegates the calls to the core with most often the same name. In such a case this
 * transformation can be used to auto-generate the code. The transformation skips methods which are already implemented
 * in the DBus Adapter. It cannot be used to generate code for overloaded methods as DBus doesn't support them and
 * by that the name differs and at least one method has to be implemented. Nevertheless the code of such implemented
 * methods are wrapped in a try/catch block (see below).
 *
 * The generated code handles the cases that an Authentication Token has to be passed as first argument to the core
 * and the case where it is not needed. It handles the case that a User object is passed in as an argument and needs
 * to be converted to a DBusUser and vice versa. If the core returns a String and the interface requires to return
 * a List or a Map, the core's return value is expected to be JSON and parsed to the required type.
 *
 * Possible thrown DBusExecutionExceptions are caught and mapped to a "normal" Exception.
 *
 * Example code that can be generated:
 *
 * @code
 * class RemoteFooAdapterDBusImpl implements RemoteFooAdapter {
 *     FooDBusAdapter fooDBusAdapter
 *
 *     Bar getFooBar(Foo parameter1, Baz parameter2) {
 *         try {
 *             return fooDBusAdapter.getFooBar(parameter1, parameter2)
 *         } catch (DBusExecutionException e) {
 *             throw mapException(e)
 *         }
 *     }
 *
 *     Bar getFooBarBaz(Foo parameter1, Baz parameter2) {
 *         try {
 *             return fooDBusAdapter.getFooBarBaz(authenticationToken(), parameter1, parameter2)
 *         } catch (DBusExecutionException e) {
 *             throw mapException(e)
 *         }
 *     }
 *
 *     void operateOnUser(User user) {
 *         try {
 *             fooDBusAdapter.operateOnUser(authenticationToken(), DBusUser.fromUser(user))
 *         } catch (DBusExecutionException e) {
 *             throw mapException(e)
 *         }
 *     }
 *
 *     User getSomeUser() {
 *         try {
 *             return fooDBusAdapter.getSomeUser(authenticationToken()).toUser()
 *         } catch (DBusExecutionException e) {
 *             throw mapException(e)
 *         }
 *     }
 *
 *     List<Map> getSomeJSON() {
 *         try {
 *             return listOfMapFromJSON(fooDBusAdapter.getSomeJSON(authenticationToken()))
 *         } catch (DBusExecutionException e) {
 *             throw mapException(e)
 *         }
 *     }
 *
 *     Map getSomeOtherJSON() {
 *         try {
 *             return mapFromJSON(fooDBusAdapter.getSomeOtherJSON(authenticationToken()))
 *         } catch (DBusExecutionException e) {
 *             throw mapException(e)
 *         }
 *     }
 * }
 * @endcode
 *
 * The code for all such methods is generated by this transformation, removing the need to implement the code in
 * the actual implementation.
 *
 * As the transformation is injected in the compile phase the resulting code will compile and the Adapter functions
 * as expected.
 *
 * The transformation is triggered by the annotation @link RemoteDBusAdapter which takes the name of the Interface as
 * "interfaceName" and the name of the DBusAdapter field as "dbusAdapterName".
 *
 * @see RemoteDBusAdapter
 * @author Martin Gräßlin <m.graesslin@dkfz.de>
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
class RemoteDBusAdapterTransformation implements ASTTransformation {

    void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        // The name of the interface whose methods needs to be implemented, is the value of the Annotation triggering the AST transformation
        AnnotationNode annotationNode = (AnnotationNode)astNodes[0]
        String interfaceName = annotationNode.getMember("interfaceName").getValue()
        // the class has a field with the same name as the dbusAdapterName value on the annotation
        String serviceName = annotationNode.getMember("dbusAdapterName").getValue()
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

        FieldNode dbusField = null
        classNode.getFields().each {
            if (it.name == serviceName) {
                dbusField = it
            }
        }
        if (!dbusField) {
            println "DBusAdapter Field not found"
            return
        }
        // generate all the methods of the interface
        serviceInterface.getMethods().each {
            // we skip methods implemented in the class
            List<MethodNode> methodsSameName = classNode.getMethods(it.name)
            if (!methodsSameName.isEmpty()) {
                // TODO: properly find same methods, this just excludes all methods with the name which are implemented
                // wrap code in try/catch block
                methodsSameName.each {
                    // TODO: this is actually not correct as we might match too many methods, so we put the try/catch into a try/catch and so on
                    it.setCode(tryCatchStatment(it.getCode()))
                }
                return
            }
            List arguments = []
            int numberDelegateParameters = dbusField.getType().getMethods(it.name).first().parameters.length
            String delegateReturnType = dbusField.getType().getMethods(it.name).first().returnType.name
            if (numberDelegateParameters == it.parameters.length + 1) {
                // add the call to authenticationToken() only if the number of parameters is not the same
                arguments << new MethodCallExpression(new VariableExpression("this"), "authenticationToken", ArgumentListExpression.EMPTY_ARGUMENTS)
            }
            // the methods arguments become parameters
            it.parameters.eachWithIndex { parameter, i ->
                if (parameter.type.name == "net.biomodels.jummp.plugins.security.User") {
                    // if the parameter is of type user, we need to generate DBusUser.fromUser(${parameter.name})
                    int parameterIndex = i
                    if (numberDelegateParameters == it.parameters.length + 1) {
                        parameterIndex++
                    }
                    arguments << new MethodCallExpression(new ClassExpression(dbusField.getType().getMethods(it.name).first().parameters[parameterIndex].type), "fromUser", new ArgumentListExpression(new VariableExpression(parameter.name)))
                } else {
                    // normal arguments can be passed through
                    arguments << new VariableExpression(parameter.name)
                }
            }
            // the delegated method call: ${serviceName}.${it.name}(${arguments})
            MethodCallExpression delegatedMethodCall = new MethodCallExpression(new VariableExpression(serviceName), it.name, new ArgumentListExpression(arguments))
            Statement statement = null
            if (it.returnType.name == "net.biomodels.jummp.plugins.security.User") {
                // if the method returns a User, we need to call ".toUser()" as the delegated method returns a DBusUser
                statement = toUser(delegatedMethodCall)
            } else if (delegateReturnType == "java.lang.String" && it.returnType.name == "java.util.List") {
                // the delegated method returns a String and we need to return a List
                // we assume that the delegated method returns JSON and we need to convert it to a list, we generate:
                // listOfMapFromJSON(delegatedMethodCall())
                statement = new ReturnStatement(new MethodCallExpression(new VariableExpression("this"), "listOfMapFromJSON", delegatedMethodCall))
            }  else if (delegateReturnType == "java.lang.String" && it.returnType.name == "java.util.Map") {
                // the delegated method returns a String and we need to return a Map
                // we assume that the delegated method returns JSON and we need to convert it to a Map, we generate:
                // mapFromJSON(delegatedMethodCall())
                statement = new ReturnStatement(new MethodCallExpression(new VariableExpression("this"), "mapFromJSON", delegatedMethodCall))
            } else if (it.returnType.name == Void.TYPE.toString()) {
                // if the method returns void, we may not create a return statement
                statement = new ExpressionStatement(delegatedMethodCall)
            } else if (it.returnType.name == "java.util.List") {
                if (it.returnType.isUsingGenerics()) {
                    switch (it.returnType.genericsTypes[0].type.name) {
                    case "net.biomodels.jummp.plugins.security.Role":
                        // generates the code to retrieve all roles
                        // return retrieveAllElements(${serviceName}, "getRoleByAuthority", "Role", delegateMethodCall())
                        ArgumentListExpression retrieveArguments = new ArgumentListExpression()
                        retrieveArguments.addExpression(new VariableExpression(serviceName))
                        retrieveArguments.addExpression(new ConstantExpression("getRoleByAuthority"))
                        retrieveArguments.addExpression(new ConstantExpression("Role"))
                        retrieveArguments.addExpression(delegatedMethodCall)
                        statement = new ReturnStatement(new MethodCallExpression(new VariableExpression("this"), "retrieveAllElements", retrieveArguments))
                        break
                    case "net.biomodels.jummp.plugins.security.User":
                        // generates the code to retrieve all users
                        // return retrieveAllElements(${serviceName}, "getUserById", "User", delegateMethodCall(), Long.class)
                        ArgumentListExpression retrieveArguments = new ArgumentListExpression()
                        retrieveArguments.addExpression(new VariableExpression(serviceName))
                        retrieveArguments.addExpression(new ConstantExpression("getUserById"))
                        retrieveArguments.addExpression(new ConstantExpression("User"))
                        retrieveArguments.addExpression(delegatedMethodCall)
                        retrieveArguments.addExpression(new ClassExpression(new ClassNode(Long.class)))
                        statement = new ReturnStatement(new MethodCallExpression(new VariableExpression("this"), "retrieveAllElements", retrieveArguments))
                        break
                    }
                }
            }
            if (!statement) {
                // if it returns a value we just return the result of the delegated method
                statement = new ReturnStatement(delegatedMethodCall)
            }
            // the complete method: same name, same return type, same parameters, same exceptions and either just a method call (return type void) or a return statement
            MethodNode method = new MethodNode(it.name, Opcodes.ACC_PUBLIC, it.returnType, it.parameters, it.exceptions, tryCatchStatment(statement))
            // add Profiled annotation to method. Annotation has a "tag" with String value: className.methodName
            // looks like: @Profiled(tag="${classNode.getNameWithoutPackage()}.${it.name}")
            AnnotationNode profiled = new AnnotationNode(new ClassNode(this.getClass().classLoader.loadClass("org.perf4j.aop.Profiled")))
            profiled.addMember("tag", new ConstantExpression(classNode.getNameWithoutPackage() + "." + it.name))
            method.addAnnotation(profiled)
            // and finally add the method to our class
            classNode.addMethod(method)
        }
    }

    /**
     * This method generates the conversion from a DBusUser to a User object.
     * It generated the following code:
     * @code
     * def temp = delegatedMethodCall()
     * return temp.toUser()
     * @endcode
     *
     * Obviously the code can still be improved.
     * @param delegatedMethodCall
     * @return Block Statement containing the generated code
     */
    private Statement toUser(MethodCallExpression delegatedMethodCall) {
        DeclarationExpression declaration = new DeclarationExpression(new VariableExpression("temp"), new Token(Types.ASSIGNMENT_OPERATOR, "=", -1, -1), delegatedMethodCall)
        MethodCallExpression toUserCall = new MethodCallExpression(new VariableExpression("temp"), "toUser", ArgumentListExpression.EMPTY_ARGUMENTS)
        List<Statement> statements = []
        statements << new ExpressionStatement(declaration)
        statements << new ReturnStatement(toUserCall)
        return new BlockStatement(statements, new VariableScope())
    }

    /**
     * Wraps the @p statement in a try-catch block to translate the DBusExecutionException.
     * Generates the following code:
     * @code
     * try {
     *     statement
     * } catch (DBusExceutionException e) {
     *     throw mapException(e)
     * }
     * @endcode
     * @param statement
     * @return A Statement containing the try-catch block around the passed in Statment
     */
    private Statement tryCatchStatment(Statement statement) {
        if (!statement) {
            return statement
        }
        TryCatchStatement tc = new TryCatchStatement(statement, new EmptyStatement())
        CatchStatement catchStatement = new CatchStatement(new Parameter(new ClassNode(this.getClass().classLoader.loadClass("org.freedesktop.dbus.exceptions.DBusExecutionException")), "e"), new Statement())
        catchStatement.setCode(new ThrowStatement(new MethodCallExpression(new VariableExpression("this"), "mapException", new ArgumentListExpression(new VariableExpression("e")))))
        tc.addCatch(catchStatement)
        return tc
    }
}
