package by.muna.julia.moep.textanalyser;

public class TextAnalyserWordWithOccurrences {
    private String word;
    private int occurrences;

    public TextAnalyserWordWithOccurrences(String word) {
        this(word, 0);
    }
    public TextAnalyserWordWithOccurrences(String word, int occurrences) {
        this.word = word;
        this.occurrences = occurrences;
    }

    public String getWord() {
        return this.word;
    }
    public int getOccurrences() {
        return this.occurrences;
    }

    public void incrementOccurrences() {
        this.occurrences++;
    }
    public void incrementOccurrences(int by) {
        this.occurrences += by;
    }

    @Override
    public String toString() {
        return this.word + " " + this.occurrences;
    }
}
