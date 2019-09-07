package parsing

import scanning.Token

sealed class Statement {

    interface Visitor<T> {
        fun visitVariableSetterStatement(statement: VariableSetter): T
        fun visitPrintStatement(statement: Print): T
        fun visitCallStatement(statement: Call): T
        fun visitBlockStatement(statement: Block): T
        fun visitProcedureStatement(statement: Procedure): T
    }

    abstract fun <T> accept(visitor: Visitor<T>): T

    data class VariableSetter(val variable: Expression.Variable, val value: Expression) : Statement() {
        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitVariableSetterStatement(this)
    }

    data class Print(val variable: Expression.Variable) : Statement() {
        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitPrintStatement(this)
    }

    data class Call(val variable: Expression.Variable) : Statement() {
        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitCallStatement(this)
    }

    data class Block(val statements: List<Statement>) : Statement() {
        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitBlockStatement(this)
    }

    data class Procedure(val name: Token, val body: List<Statement>) : Statement() {
        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitProcedureStatement(this)
    }
}