package testing

import debugging.TestSource
import guu.Console
import guu.Guu
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

private var testPath = ""
private var outputPath = ""

object Tester {

    val console = Console()

    fun testInterpreter(testName: String, outputName: String) {
        print("$testName: ")
        Guu.run(testPath + testName, console)
        val right = String(Files.readAllBytes(Paths.get(outputPath + outputName)), Charset.defaultCharset()).lines()

        if (isTestPassed(console.infoLog, right)) println("OK") else println("Failed")
        console.clear()
    }

    fun testDebugger(testName: String, commandSourceName: String, outputName: String) {
        print("$testName: ")

        val commands = String(Files.readAllBytes(Paths.get(testPath + commandSourceName)), Charset.defaultCharset()).lines()
        Guu.debug(testPath + testName, console, TestSource(commands))
        val right = String(Files.readAllBytes(Paths.get(outputPath + outputName)), Charset.defaultCharset()).lines()

        if (isTestPassed(console.debugLog, right)) println("OK") else println("Failed")
        console.clear()
    }

    private fun isTestPassed(result: List<String>, right: List<String>): Boolean {
        if (result.size != right.size) return false
        for (i in 0 until result.size) {
            if (result[i] != (right[i])) return false
        }
        return true
    }
}

fun main(args: Array<String>) {
    Tester.console.hidden = true

    //interpreter testing
    testPath = "src/testing/tests/interpreter/"
    outputPath = "src/testing/outs/interpreter/"
    println("Interpretation tests")
    Tester.testInterpreter("test-bad-scanning-1.txt", "test-bad-scanning-1-out.txt")
    Tester.testInterpreter("test-bad-parsing-1.txt", "test-bad-parsing-1-out.txt")
    Tester.testInterpreter("test-bad-parsing-2.txt", "test-bad-parsing-2-out.txt")
    Tester.testInterpreter("test-bad-runtime-1.txt", "test-bad-runtime-1-out.txt")
    Tester.testInterpreter("test-bad-runtime-2.txt", "test-bad-runtime-2-out.txt")
    Tester.testInterpreter("test-good-1.txt", "test-good-1-out.txt")
    Tester.testInterpreter("test-good-2.txt", "test-good-2-out.txt")

    println()

    //debugger testing
    testPath = "src/testing/tests/debugger/"
    outputPath = "src/testing/outs/debugger/"
    println("Debugging tests")
    Tester.testDebugger("test_1.txt", "test_1_commands.txt", "test_1_out.txt")
    Tester.testDebugger("test_2.txt", "test_2_commands.txt", "test_2_out.txt")
}