package by.muna.julia.moep.textanalyser;

import java.util.Iterator;
import java.util.Map;

public interface TextAnalyserResult {
    int getWordsCount();
    int getSentensesCount();
    int getParagraphsCount();
    float getAverageWordLength();

    Map<Character, Float> getRelativeCharOccurrences();

    Iterator<String> getAlphabeticOrderedDictIterator();
    Iterator<TextAnalyserWordWithOccurrences> getOccurrencesDescOrderedDictIterator();
}
