package by.muna.julia.moep.textanalyser.tokens;

public class WordToken implements Token {
    private String word;

    public WordToken(String word) {
        this.word = word;
    }

    public String getWord() {
        return this.word;
    }

    @Override
    public TokenType getType() {
        return TokenType.WORD;
    }
}
