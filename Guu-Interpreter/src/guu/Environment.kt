package guu

import scanning.Token

class Environment {

    private val values = HashMap<String, Any>()

    fun getAllVariables(): List<Pair<String, Any>> {
        val entries = mutableListOf<Pair<String, Any>>()
        for (key in values.keys) {
            val value = values[key]
            if (value != null && value !is GuuProcedure) {
                entries.add(key to value)
            }
        }
        return entries
    }

    fun set(name: String, value: Any): Boolean = values.put(name, value) != null

    fun get(token: Token): Any = values[token.lexeme] ?: throw RuntimeError(token, "Undefined variable ${token.lexeme}.")
}