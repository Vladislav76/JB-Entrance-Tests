package scanning

enum class TokenType {

    // characters
    MINUS,

    //literals
    IDENTIFIER, NUMBER,

    //keywords
    SUB, SET, CALL, PRINT,

    EOF, NEW_LINE
}