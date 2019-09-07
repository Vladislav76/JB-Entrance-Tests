package debugging

import java.io.BufferedReader
import java.io.InputStreamReader

class WorkSource : InputSource {

    private val reader = BufferedReader(InputStreamReader(System.`in`))

    override fun getInputString(): String = reader.readLine()
}