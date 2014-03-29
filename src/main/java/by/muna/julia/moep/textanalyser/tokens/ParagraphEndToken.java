package by.muna.julia.moep.textanalyser.tokens;

public class ParagraphEndToken implements Token {
    @Override
    public TokenType getType() {
        return TokenType.PARAGRAPH_END;
    }
}
