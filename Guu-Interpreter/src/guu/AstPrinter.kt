package guu

import parsing.Expression
import parsing.Statement
import java.lang.StringBuilder

object AstPrinter : Expression.Visitor<String>, Statement.Visitor<String> {

    fun print(program: List<Statement.Procedure>): String {
        return StringBuilder().apply {
            for (proc in program) {
                append(proc.accept(this@AstPrinter))
                append("\n")
            }
        }.toString()
    }

    override fun visitLiteralExpression(expression: Expression.Literal): String = expression.value.toString()

    override fun visitUnaryExpression(expression: Expression.Unary): String = parenthesize(expression.operator.lexeme, expression.expression)

    override fun visitVariableExpression(expression: Expression.Variable): String = expression.name.lexeme

    override fun visitVariableSetterStatement(statement: Statement.VariableSetter): String =
        "[line:${statement.variable.name.line}] set ${statement.variable.accept(this)} ${statement.value.accept(this)}"

    override fun visitPrintStatement(statement: Statement.Print): String =
        "[line:${statement.variable.name.line}] print ${statement.variable.accept(this)}"

    override fun visitCallStatement(statement: Statement.Call): String =
        "[line:${statement.variable.name.line}] call ${statement.variable.accept(this)}"

    override fun visitBlockStatement(statement: Statement.Block): String {
        return StringBuilder().apply {
            for (stmt in statement.statements) {
                append(stmt.accept(this@AstPrinter))
                append("\n")
            }
        }.toString()
    }

    override fun visitProcedureStatement(statement: Statement.Procedure): String = "sub ${statement.name.lexeme}\n${Statement.Block(statement.body).accept(this)}"

    private fun parenthesize(s: String, vararg expressions: Expression) =
        StringBuilder().apply {
            append(s)
            for (expression in expressions) append(expression.accept(this@AstPrinter))
        }.toString()
}