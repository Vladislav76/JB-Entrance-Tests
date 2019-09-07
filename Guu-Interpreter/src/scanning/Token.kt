package scanning

data class Token(
    val type: TokenType,
    val lexeme: String,
    val original: Any?,
    val line: Int
)
