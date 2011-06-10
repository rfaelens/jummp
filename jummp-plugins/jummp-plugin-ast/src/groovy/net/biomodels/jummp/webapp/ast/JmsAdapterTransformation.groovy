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
 * @see JmsAdapter
 * @author Martin Gräßlin <m.graesslin@dkfz.de>
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
class JmsAdapterTransformation implements ASTTransformation {

    void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        ClassNode classNode = ((ClassNode)astNodes[1])

        classNode.getMethods().each {
            // does the method have a Queue annotation?
            if (it.getAnnotations().find { it.classNode.name == "grails.plugin.jms.Queue"}) {
                // yes, so let's wrap the code in a try-catch-finally block
                it.setCode(tryCatchFinallyStatement(it.getCode()))
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
}
