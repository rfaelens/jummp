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
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.ast.expr.CastExpression

/**
 * @short AST Transformation to generate code for remote JMS Adapters.
 *
 * This class provides the AST Transformation (http://groovy.codehaus.org/Compile-time+Metaprogramming+-+AST+Transformations)
 * to implement all the delegating methods in remote JMS Adapters to the core. Each remote JMS Adapter implements an
 * Remote*Adapter interface and delegates the calls to the core with most often the same name. In such a case this
 * transformation can be used to auto-generate the code.
 *
 * The transformation is triggered by the annotation @link RemoteJmsAdapter which takes the name of the Interface as
 * the value.
 *
 * @see RemoteJmsAdapter
 * @author Martin Gräßlin <m.graesslin@dkfz.de>
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
class RemoteJmsAdapterTransformation implements ASTTransformation {
    void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        // The name of the interface whose methods needs to be implemented, is the value of the Annotation triggering the AST transformation
        AnnotationNode annotationNode = (AnnotationNode)astNodes[0]
        String interfaceName = annotationNode.getMember("value").getValue()
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

        // generate all the methods of the interface
        serviceInterface.getMethods().each {
            List arguments = []
            // the methods arguments become parameters
            arguments << new ConstantExpression(it.name)
            if (it.parameters.length == 1) {
                arguments << new VariableExpression(it.parameters.first().name)
            } else if (it.parameters.length > 1) {
                ListExpression list = new ListExpression()
                it.parameters.each { parameter ->
                    list.addExpression(new VariableExpression(parameter.name))
                }
                arguments << list
            }
            Statement statement = null
            if (it.returnType.name == Void.TYPE.toString()) {
                statement = voidMethod(new ArgumentListExpression(arguments))
            } else {
                statement = returningMethod(new ArgumentListExpression(arguments), it.returnType)
            }
            // the complete method: same name, same return type, same parameters, same exceptions and either just a method call (return type void) or a return statement
            MethodNode method = new MethodNode(it.name, Opcodes.ACC_PUBLIC, it.returnType, it.parameters, it.exceptions, statement)
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
     * This method generates the method body for the case of a void method. It generates:
     * @code
     * validateReturnValue(send("${methodName}", [arguments]), Boolean)
     * @endcode
     * @param arguments
     * @return
     */
    Statement voidMethod(ArgumentListExpression arguments) {
        MethodCallExpression sendMethodCall = new MethodCallExpression(new  VariableExpression("this"), "send", arguments)
        MethodCallExpression validateCall = new MethodCallExpression(new VariableExpression("this"), "validateReturnValue",
                new ArgumentListExpression(sendMethodCall, new ClassExpression(new ClassNode(Boolean.class))))
        return new ExpressionStatement(validateCall)
    }

    /**
     * This method generates the method body for the case of a method returning a value. It generates:
     * @code
     * def retVal = send("${methodName}", [arguments])
     * validateReturnValue(retVal, returnType)
     * return (returnType)retVal
     * @endcode
     * @param arguments
     * @param returnType
     * @return
     */
    Statement returningMethod(ArgumentListExpression arguments, ClassNode returnType) {
        BlockStatement block = new BlockStatement()
        MethodCallExpression sendMethodCall = new MethodCallExpression(new  VariableExpression("this"), "send", arguments)
        DeclarationExpression declaration = new DeclarationExpression(new VariableExpression("retVal"), new Token(Types.ASSIGNMENT_OPERATOR, "=", -1, -1), sendMethodCall)
        block.addStatement(new ExpressionStatement(declaration))
        block.addStatement(new ExpressionStatement(new MethodCallExpression(new VariableExpression("this"), "validateReturnValue", new ArgumentListExpression(new VariableExpression("retVal"), new ClassExpression(returnType)))))
        block.addStatement(new ReturnStatement(new CastExpression(returnType, new VariableExpression("retVal"))))
        return block
    }
}
