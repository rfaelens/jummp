package net.biomodels.jummp.webapp.ast

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.ast.stmt.TryCatchStatement
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.stmt.CatchStatement
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.ast.expr.BooleanExpression
import org.codehaus.groovy.ast.expr.NotExpression
import org.codehaus.groovy.ast.stmt.IfStatement
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ListExpression

/**
 * @short AST Transformation for core JMS Adapters.
 *
 * The transformation is triggered by the @link JmsAdapter.
 * The transformation looks for all methods annotated with the Queue annotation
 * and wraps the code in a try-catch-finally block. All Exceptions thrown in the
 * original code are caught and returned, so that JMS does not end in a timeout.
 * The finally block calls restoreAuthentication() to reset the Authentication
 * bound to the current thread.
 *
 * By using the @link JmsQueueMethod annotation on a method the Transformation can
 * generate the verifyMessage block.
 *
 * @see JmsAdapter
 * @see JmsQueueMethod
 * @author Martin Gräßlin <m.graesslin@dkfz.de>
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
class JmsAdapterTransformation implements ASTTransformation {

    void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        ClassNode classNode = ((ClassNode)astNodes[1])

        classNode.getMethods().each {
            // search for a JmsQueueMethod annotation on the list
            AnnotationNode verify = null
            List<AnnotationNode> matchedAnnotations = it.getAnnotations(new ClassNode(JmsQueueMethod))
            if (!matchedAnnotations.isEmpty()) {
                verify = matchedAnnotations.first()
            }
            // does the method have a Queue annotation?
            if (it.getAnnotations().find { it.classNode.name == "grails.plugin.jms.Queue"}) {
                // yes, so let's wrap the code in a try-catch-finally block
                it.setCode(tryCatchFinallyStatement(it.getCode()))
                if (verify) {
                    // the method is annotated with the JmsQueueMethod annotation.
                    // We need to prepend the code with a if statement
                    BlockStatement block = new BlockStatement()
                    block.addStatement(verifyMessage(verify))
                    block.addStatement(it.getCode())
                    it.setCode(block)
                }
                // add Profiled annotation to method. Annotation has a "tag" with String value: className.methodName
                // looks like: @Profiled(tag="${classNode.getNameWithoutPackage()}.${it.name}")
                AnnotationNode profiled = new AnnotationNode(new ClassNode(this.getClass().classLoader.loadClass("org.perf4j.aop.Profiled")))
                profiled.addMember("tag", new ConstantExpression(classNode.getNameWithoutPackage() + "." + it.name))
                it.addAnnotation(profiled)
            }
        }
    }

    /**
     * Wraps the @p statement in a try-catch-finally block.
     *
     * Generates the following code:
     * try {
     *     statement
     * } catch (Exception e) {
     *     return e
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
        CatchStatement catchStatement = new CatchStatement(new Parameter(new ClassNode(this.getClass().classLoader.loadClass("java.lang.Exception")), "e"), new Statement())
        catchStatement.setCode(new ReturnStatement(new VariableExpression("e")))
        tc.addCatch(catchStatement)
        return tc
    }

    /**
     * Generates code to verify the message parameter of the method.
     * The arguments to check are read from the @p annotation. The code takes
     * care of the case that the message needs to include an authentication hash.
     *
     * The method cannot generate code for the case that the method takes a different set of arguments.
     *
     * Generates the following block code:
     * @code
     * if (!verifyMessage(message, ListOfArgumentsInAnnotation)) {
     *     return new IllegalArgumentException("Argument mismatch")
     * }
     * @endcode
     *
     * @param annotation The Annotation describing the parameters
     * @return If Statement with the check
     */
    private Statement verifyMessage(AnnotationNode annotation) {
        List classes = annotation.getMember("arguments").getExpressions()
        if (annotation.getMember("isAuthenticate").getValue()) {
            // if authenticate we need a String parameter as first element in the parameter list
            classes.add(0, new ClassExpression(new ClassNode(String)))
        }
        BlockStatement ifBlock = new BlockStatement()
        ifBlock.addStatement(new ReturnStatement(new ConstructorCallExpression(new ClassNode(IllegalArgumentException.class), new ArgumentListExpression(new ConstantExpression("Argument mismatch")))))
        BooleanExpression condition = new BooleanExpression(new NotExpression(new MethodCallExpression(new VariableExpression("this"), "verifyMessage", new ArgumentListExpression(new VariableExpression("message"), new ListExpression(classes)))))
        return new IfStatement(condition, ifBlock, new EmptyStatement())
    }
}
