package guu

import parsing.Statement

class GuuProcedure (val definition: Statement.Procedure) {

    fun call(interpreter: Interpreter) {
        interpreter.executeBlock(definition.body)
    }
}