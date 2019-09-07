package guu

import debugging.Debugger
import debugging.InputSource
import debugging.WorkSource
import parsing.Parser
import parsing.Statement
import scanning.Scanner
import scanning.Token
import scanning.TokenType
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

class RuntimeError(val token: Token, message: String) : RuntimeException(message)

object Guu {

    private var console: Console? = null
    private var hadError = false
    private var hadRuntimeError = false

    fun error(message: String) {
        report(message)
    }

    fun error(line: Int, where: String, message: String) {
        report(line, "at $where", message)
    }

    fun error(token: Token, message: String) {
        when (token.type) {
            TokenType.EOF -> report(token.line, "at end", message)
            TokenType.NEW_LINE -> report(token.line, "at end of line", message)
            else -> report(token.line, "at ${token.lexeme}", message)
        }
    }

    fun runtimeError(e: RuntimeError) {
        console?.error("Line ${e.token.line}. ${e.message}")
        hadRuntimeError = true
    }

    fun run(path: String, console: Console) {
        val preparedData = prepare(path, console)
        if (preparedData != null) {
            val interpreter = Interpreter(console)
            interpreter.prepare(preparedData.first, preparedData.second)
            if (!hadRuntimeError) interpreter.run()
        }
    }

    fun debug(path: String, console: Console, commandSource: InputSource) {
        val preparedData = prepare(path, console)
        if (preparedData != null) {
            val debugger = Debugger(console, commandSource)
            debugger.prepare(preparedData.first, preparedData.second)
            if (!hadRuntimeError) debugger.run()
        }
    }

    private fun prepare(path: String, console: Console): Pair<List<Statement.Procedure>, Statement.Procedure>? {
        this.hadError = false
        this.hadRuntimeError = false
        this.console = console
        val inputString = String(Files.readAllBytes(Paths.get(path)), Charset.defaultCharset())

        val scanner = Scanner(inputString)
        val tokens = scanner.scanTokens()
        if (hadError) return null

        val parser = Parser(tokens)
        val program = parser.parse()
        if (hadError) return null

        val entryPoint = program.find { procedure -> procedure.name.lexeme == "main" }
        return if (entryPoint == null) {
            error("No 'main' procedure.")
            null
        } else {
            program to entryPoint
        }
    }

    private fun report(message: String) {
        console?.error("Error in source file: $message")
        hadError = true
    }

    private fun report(line: Int, where: String, message: String) {
        console?.error("Line: $line. Error $where: $message")
        hadError = true
    }
}