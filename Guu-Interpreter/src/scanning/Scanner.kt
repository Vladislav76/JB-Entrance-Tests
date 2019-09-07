package scanning

import guu.Guu

class Scanner(private val sourceCode: String) {

    private val tokens = mutableListOf<Token>()
    private var start = 0
    private var current = 0
    private var line = 1

    fun scanTokens(): List<Token> {
        addToken(TokenType.NEW_LINE)
        while (!isAtEnd()) {
            start = current
            scanToken()
        }
        if (tokens.last().type == TokenType.NEW_LINE) tokens.removeAt(tokens.size - 1)
        tokens.add(Token(TokenType.EOF, "", null, line))
        return tokens
    }

    private fun scanToken() {
        val c = nextCharacter()
        when (c) {
            ' ', '\t', '\r' -> Unit
            '\n' -> {
                if (tokens.isEmpty() || tokens.last().type != TokenType.NEW_LINE) addToken(TokenType.NEW_LINE)
                line++
            }
            '-' -> addToken(TokenType.MINUS)
            else ->
                when {
                    isDigit(c) -> number()
                    isAlpha(c) -> identifier()
                    else -> Guu.error(line, c.toString(), "Unexpected character.")
                }
        }
    }

    private fun nextCharacter(): Char {
        current++
        return sourceCode[current - 1]
    }

    private fun peek(): Char = if (isAtEnd()) 0.toChar() else sourceCode[current]

    private fun number() {
        while (isDigit(peek())) nextCharacter()
        addToken(TokenType.NUMBER, sourceCode.substring(start, current).toInt())
    }

    private fun identifier() {
        while (isAlphaNumeric(peek())) nextCharacter()
        val lexeme = sourceCode.substring(start, current)
        addToken(keywords[lexeme] ?: TokenType.IDENTIFIER)
    }

    private fun addToken(type: TokenType) {
        addToken(type, null)
    }

    private fun addToken(type: TokenType, original: Any?) {
        val lexeme = sourceCode.substring(start, current)
        tokens.add(Token(type, lexeme, original, line))
    }

    private fun isDigit(c: Char) = c in '0'..'9'

    private fun isAlpha(c: Char) = c in 'a'..'z' || c in 'A'..'Z' || c == '_'

    private fun isAlphaNumeric(c: Char) = isAlpha(c) || isDigit(c)

    private fun isAtEnd() = current >= sourceCode.length

    companion object {
        private val keywords = HashMap<String, TokenType>().apply {
            put("sub", TokenType.SUB)
            put("set", TokenType.SET)
            put("call", TokenType.CALL)
            put("print", TokenType.PRINT)
        }
    }
}