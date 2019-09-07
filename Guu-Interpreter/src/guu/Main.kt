package guu

import debugging.WorkSource

fun main(args: Array<String>) {
    when (args.size) {
        0 -> Unit
        1 -> Guu.debug(args[0], Console(), WorkSource())
        else -> System.exit(64)
    }
}