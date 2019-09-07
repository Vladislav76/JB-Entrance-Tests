package parsing

import scanning.Token

sealed class Expression {

    interface Visitor<T> {
        fun visitLiteralExpression(expression: Literal): T
        fun visitUnaryExpression(expression: Unary): T
        fun visitVariableExpression(expression: Variable): T
    }

    abstract fun <T> accept(visitor: Visitor<T>): T

    data class Literal(val value: Any) : Expression() {
        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitLiteralExpression(this)
    }

    data class Unary(val operator: Token, val expression: Expression) : Expression() {
        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitUnaryExpression(this)
    }

    data class Variable(val name: Token) : Expression() {
        override fun <T> accept(visitor: Visitor<T>): T = visitor.visitVariableExpression(this)
    }
}