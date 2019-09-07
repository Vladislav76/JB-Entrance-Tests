package guu

import parsing.Expression
import parsing.Statement
import scanning.Token
import scanning.TokenType

open class Interpreter(protected val console: Console) : Expression.Visitor<Any>, Statement.Visitor<Unit> {

    protected var environment = Environment()
    protected lateinit var entryPoint: Statement.Procedure

    fun prepare(program: List<Statement.Procedure>, entryPoint: Statement.Procedure) {
        this.entryPoint = entryPoint
        environment = Environment()
        for (procedure in program) {
            try {
                procedure.accept(this)
            } catch (e: RuntimeError) {
                Guu.runtimeError(e)
            }
        }
    }

    open fun run() {
        executeBlock(entryPoint.body)
    }

    fun executeBlock(statements: List<Statement>) {
        try {
            for (statement in statements) {
                execute(statement)
            }
        } catch (e: RuntimeError) {
            Guu.runtimeError(e)
        }
    }

    protected fun execute(statement: Statement) = statement.accept(this)

    /* Expression.Visitor implementation */
    override fun visitVariableExpression(expression: Expression.Variable): Any = environment.get(expression.name)

    override fun visitLiteralExpression(expression: Expression.Literal): Any = expression.value

    override fun visitUnaryExpression(expression: Expression.Unary): Any {
        val evaluated = evaluate(expression.expression)
        return when (expression.operator.type) {
            TokenType.MINUS -> {
                checkNumberOperator(expression.operator, evaluated)
                -(evaluated as Int)
            }
            else -> Unit
        }
    }

    /* Statement.Visitor implementation */
    override fun visitVariableSetterStatement(statement: Statement.VariableSetter) {
        val value = evaluate(statement.value)
        environment.set(statement.variable.name.lexeme, value)
    }

    override fun visitPrintStatement(statement: Statement.Print) {
        val evaluated = evaluate(statement.variable)
        if (evaluated is GuuProcedure) throw RuntimeError(statement.variable.name, "No can print GuuCallable object.")
        console.log("${statement.variable.name.lexeme} = ${stringify(evaluated)}")
    }

    override fun visitCallStatement(statement: Statement.Call) {
        val callable = evaluate(statement.variable)
        if (callable is GuuProcedure) {
            callable.call(this)
        } else {
            throw RuntimeError(statement.variable.name, "No such procedure.")
        }
    }

    override fun visitBlockStatement(statement: Statement.Block) {
        executeBlock(statement.statements)
    }

    override fun visitProcedureStatement(statement: Statement.Procedure) {
        val procedure = GuuProcedure(statement)
        if (environment.set(statement.name.lexeme, procedure)) throw RuntimeError(statement.name, "Procedure declaration is duplicated.")
    }

    protected fun evaluate(expression: Expression): Any = expression.accept(this)

    private fun stringify(_object: Any): String = _object.toString()

    private fun checkNumberOperator(operator: Token, operand: Any) {
        if (operand is Int) return
        else throw RuntimeError(operator, "Operand must be a number.")
    }
}