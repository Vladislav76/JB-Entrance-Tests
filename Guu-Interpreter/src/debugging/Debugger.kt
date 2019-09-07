package debugging

import guu.*
import parsing.Statement
import java.util.*

class Debugger(_console: Console, private val source: InputSource) : Interpreter(_console) {

    private val callStack = Stack<Pair<Statement.Procedure, Int>>()
    private val stackTrace = mutableListOf<Pair<Statement.Procedure, Statement>>()
    private var currentPointer: Int = 0
    private var currentStatement: Statement? = null
    private lateinit var currentProcedure: Statement.Procedure
    private var isWorking = false

    override fun run() {
        stackTrace.clear()
        currentProcedure = entryPoint
        currentStatement = currentProcedure.body.firstOrNull()
        isWorking = true

        while (isWorking) {
            printHeader()
            try {
                val input = source.getInputString()
                when (input) {
                    EXIT_COMMAND -> finish()
                    STEP_OVER_COMMAND -> stepOver()
                    STEP_INTO_COMMAND -> stepInto()
                    PRINT_VARIABLES_COMMAND -> printVariables()
                    PRINT_STACK_TRACE_COMMAND -> printStackTrace()
                    else -> printError()
                }
            } catch (e: RuntimeError) {
                Guu.runtimeError(e)
                break
            } catch (e: InputEndException) {
                break
            }
            console.output("")
        }
    }

    private fun stepInto() {
        val stmt = currentStatement
        if (stmt != null) {
            stackTrace.add(currentProcedure to stmt)
            if (stmt is Statement.Call) {
                val proc = evaluate(stmt.variable) as GuuProcedure
                into(proc.definition)
            } else {
                execute(stmt)
                currentPointer++
            }
            nextStatement()
        } else {
            finish()
        }
    }

    private fun stepOver() {
        val stmt = currentStatement
        if (stmt != null) {
            stackTrace.add(currentProcedure to stmt)
            execute(stmt)
            currentPointer++
            nextStatement()
        } else {
            finish()
        }
    }

    private fun into(called: Statement.Procedure) {
        callStack.push(currentProcedure to currentPointer)
        currentProcedure = called
        currentPointer = 0
    }

    private fun out() {
        val oldProcedure = currentProcedure
        while (isAtBlockEnd() && callStack.isNotEmpty()) {
            val (proc, pointer) = callStack.pop()
            currentProcedure = proc
            currentPointer = pointer
            currentPointer++
        }
        if (oldProcedure == currentProcedure || isAtBlockEnd()) {
            finish()
        }
    }

    private fun nextStatement() {
        if (isAtBlockEnd()) out()
        if (isWorking) {
            val statement = currentProcedure.body[currentPointer]
            currentStatement = statement
        } else {
            currentStatement = null
        }
    }

    private fun finish() {
        isWorking = false
    }

    private fun isAtBlockEnd(): Boolean = currentPointer >= currentProcedure.body.size

    private fun printVariables() {
        console.output("# # # ALL VARIABLES # # #")
        val vars = environment.getAllVariables()
        if (vars.isEmpty()) {
            console.debugLog("No variables")
        }
        for ((variable, value) in vars) {
            console.debugLog("$variable: $value")
        }
        console.output("")
    }

    private fun printStackTrace() {
        console.output("# # # STACK TRACE # # #")
        if (stackTrace.isEmpty()) {
            console.debugLog("No trace")
        }
        for ((proc, stmt) in stackTrace) {
            console.debugLog("[${proc.name.lexeme}] ${stmt.accept(AstPrinter)}")
        }
        console.output("")
    }

    private fun printHeader() {
        console.output("* * * * * GUU DEBUGGER 1.0 * * * * *")
        console.output("$STEP_INTO_COMMAND - step into")
        console.output("$STEP_OVER_COMMAND - step over")
        console.output("$PRINT_VARIABLES_COMMAND - all variables")
        console.output("$PRINT_STACK_TRACE_COMMAND - stack trace")
        console.output("$EXIT_COMMAND - finish working")
        console.output("")
        printOutput()
        console.output("CURRENT LINE:")
        console.output("[${currentProcedure.name.lexeme}] ${currentStatement?.accept(AstPrinter)}")
        console.output("")
        console.output("INPUT:")
    }

    private fun printOutput() {
        console.output("OUTPUT:")
        if (console.infoLog.isEmpty()) {
            console.output("No output")
        }
        for (output in console.infoLog) {
            console.output(output)
        }
        console.output("")
    }

    private fun printError() {
        console.debugLog("Unknown command")
    }

    companion object {
        private const val STEP_INTO_COMMAND = "i"
        private const val STEP_OVER_COMMAND = "o"
        private const val PRINT_VARIABLES_COMMAND = "var"
        private const val PRINT_STACK_TRACE_COMMAND = "trace"
        private const val EXIT_COMMAND = "exit"
    }
}