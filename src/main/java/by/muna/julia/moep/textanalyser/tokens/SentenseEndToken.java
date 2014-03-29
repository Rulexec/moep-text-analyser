package by.muna.julia.moep.textanalyser.tokens;

public class SentenseEndToken implements Token {
    @Override
    public TokenType getType() {
        return TokenType.SENTENSE_END;
    }
}
