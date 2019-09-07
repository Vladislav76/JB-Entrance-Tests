package parsing

import guu.Guu
import scanning.Token
import scanning.TokenType
import java.lang.RuntimeException

class Parser(private val tokens: List<Token>) {

    private class ParseError : RuntimeException()

    private var current = 0

    fun parse(): List<Statement.Procedure> = program()

    private fun synchronizeProcedure() {
        while (!isAtEnd() && !check(TokenType.SUB) && !checkNext(TokenType.SUB)) { nextToken() }
    }

    private fun synchronize() {
        if (peek().type == TokenType.IDENTIFIER || peek().type == TokenType.MINUS || peek().type == TokenType.NUMBER) nextToken()
        while (!isAtEnd()) {
            when (peek().type) {
                TokenType.SET, TokenType.CALL, TokenType.PRINT, TokenType.NEW_LINE -> return
                else -> nextToken()
            }
        }
    }

    private fun program(): List<Statement.Procedure> {
        val procedures = mutableListOf<Statement.Procedure>()
        while (!isAtEnd()) {
            try {
                if (!match(TokenType.NEW_LINE)) {
                    Guu.error(peek(), "Procedure declaration must be at a new line.")
                    nextToken()
                    throw ParseError()
                }
                procedure().also {
                    procedures.add(it)
                }
            } catch (e: ParseError) {
                synchronizeProcedure()
            }
        }
        return procedures
    }

    private fun procedure(): Statement.Procedure {
        if (match(TokenType.SUB)) {
            val name = consume(TokenType.IDENTIFIER, "Expected procedure name.")
            val body = block()
            return Statement.Procedure(name, body)
        } else {
            throw error(peek(), "'sub' is omitted.")
        }
    }

    private fun block(): List<Statement> {
        val statements = mutableListOf<Statement>()
        while (!check(TokenType.SUB) && !checkNext(TokenType.SUB) && !isAtEnd()) {
            try {
                if (!match(TokenType.NEW_LINE)) {
                    Guu.error(peek(), "Statement must be at a new line.")
                    nextToken()
                    throw ParseError()
                }
                statement().also {
                    statements.add(it)
                }
            } catch(e: ParseError) {
                synchronize()
            }
        }
        return statements
    }

    private fun statement(): Statement {
        return when {
            match(TokenType.PRINT) -> printStatement()
            match(TokenType.SET) -> variableSetterStatement()
            match(TokenType.CALL) -> callStatement()
            else -> throw error(peek(), "Unknown statement.")
        }
    }

    private fun printStatement(): Statement = Statement.Print(variable("Expected variable name."))

    private fun callStatement(): Statement = Statement.Call(variable("Expected procedure name."))

    private fun variableSetterStatement(): Statement = Statement.VariableSetter(variable("Expected variable name."), expression())

    private fun expression(): Expression = if (match(TokenType.MINUS)) unary() else primary()

    private fun unary(): Expression = Expression.Unary(previous(), primary())

    private fun primary(): Expression =
        when {
            match(TokenType.NUMBER) -> Expression.Literal(previous().original!!)
            else -> throw error(peek(), "Expected number.")
        }

    private fun variable(message: String): Expression.Variable =
        when {
            match(TokenType.IDENTIFIER) -> Expression.Variable(previous())
            else -> throw error(previous(), message)
        }

    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                nextToken()
                return true
            }
        }
        return false
    }

    private fun check(type: TokenType): Boolean = if (isAtEnd()) false else peek().type == type

    private fun checkNext(type: TokenType): Boolean = if (isAtEnd()) false else next().type == type

    private fun consume(type: TokenType, message: String): Token = if (check(type)) nextToken() else throw error(peek(), message)

    private fun error(token: Token, message: String): ParseError {
        Guu.error(token, message)
        return ParseError()
    }

    private fun nextToken(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun peek() = tokens[current]

    private fun previous() = tokens[current - 1]

    private fun next() = if (current < tokens.size - 1) tokens[current + 1] else peek()

    private fun isAtEnd() = peek().type == TokenType.EOF
}