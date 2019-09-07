package debugging

import java.lang.RuntimeException

class InputEndException : RuntimeException()

class TestSource(private val data: List<String>) : InputSource {

    private var current = 0

    override fun getInputString(): String = if (current < data.size) data[current++] else throw InputEndException()
}