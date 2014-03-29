package by.muna.julia.moep.textanalyser;

import java.io.File;
import java.util.Iterator;
import java.util.Map.Entry;

public class Main {
    public static void main(String[] args) throws Exception {
        TextAnalyserResult result = new TextAnalyser().analyseText(new File("text.txt"));

        System.out.println("Total words count: " + result.getWordsCount());
        System.out.println("Total sentenses count: " + result.getSentensesCount());
        System.out.println("Total paragraphs count: " + result.getParagraphsCount());

        System.out.println("Average word length: " + result.getAverageWordLength());

        for (Entry<Character, Float> entry : result.getRelativeCharOccurrences().entrySet()) {
            System.out.println("Char '" + entry.getKey() + "' occurrences in " + (entry.getValue() * 100) + "%");
        }

        System.out.println("Alphabetic dict:");
        Iterator<String> stringIterator = result.getAlphabeticOrderedDictIterator();

        while (stringIterator.hasNext()) {
            System.out.println(stringIterator.next());
        }

        System.out.println("By word occurrence dict:");
        Iterator<TextAnalyserWordWithOccurrences> occurrencesIterator =
            result.getOccurrencesDescOrderedDictIterator();

        while (occurrencesIterator.hasNext()) {
            System.out.println(occurrencesIterator.next());
        }
    }
}
