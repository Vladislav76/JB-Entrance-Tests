package guu

class Console {

    private val _infoLog = mutableListOf<String>()
    private val _debugLog = mutableListOf<String>()
    val infoLog: List<String> = _infoLog
    val debugLog: List<String> = _debugLog
    var hidden = false

    fun output(s: String) {
        if (!hidden) println(s)
    }

    fun log(s: String) {
        _infoLog.add(s)
        output(s)
    }

    fun debugLog(s: String) {
        _debugLog.add(s)
        output(s)
    }

    fun error(s: String) {
        _infoLog.add(s)
        if (!hidden) System.err.println(s)
    }

    fun clear() {
        _infoLog.clear()
        _debugLog.clear()
    }
}