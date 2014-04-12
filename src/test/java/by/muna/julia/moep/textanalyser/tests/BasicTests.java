package by.muna.julia.moep.textanalyser.tests;

import by.muna.julia.moep.textanalyser.TextAnalyser;
import by.muna.julia.moep.textanalyser.TextAnalyserResult;
import by.muna.julia.moep.textanalyser.TextAnalyserWordWithOccurrences;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BasicTests {
    @Test
    public void basicTest() {
        String text = "Test text. Second sentense.\nNew paragraph.\nLast paragraph.";

        TextAnalyserResult result = new TextAnalyser().analyseText(text);

        Assert.assertEquals(8, result.getWordsCount());
        Assert.assertEquals(4, result.getSentensesCount());
        Assert.assertEquals(3, result.getParagraphsCount());
        Assert.assertEquals(5.875, result.getAverageWordLength(), 0);

        Iterator<TextAnalyserWordWithOccurrences> occurrencesIterator =
            result.getOccurrencesDescOrderedDictIterator();

        while (occurrencesIterator.hasNext()) {
            TextAnalyserWordWithOccurrences item = occurrencesIterator.next();

            Assert.assertEquals(item.getWord().equals("paragraph") ? 2 : 1, item.getOccurrences());
        }

        List<String> alphabeticDict = Arrays.asList(
            "Last", "New", "Second", "Test", "paragraph", "sentense", "text"
        );

        Iterator<String> expectedStringIterator = alphabeticDict.iterator();
        Iterator<String> actualStringIterator = result.getAlphabeticOrderedDictIterator();

        while (expectedStringIterator.hasNext()) {
            Assert.assertTrue(actualStringIterator.hasNext());

            String expected = expectedStringIterator.next();
            String actual = actualStringIterator.next();

            Assert.assertEquals(expected, actual);
        }

        Assert.assertFalse(actualStringIterator.hasNext());

        class CO {
            public char c;
            public float relativeOccurrences;

            public CO(char c, float relativeOccurrences) {
                this.c = c;
                this.relativeOccurrences = relativeOccurrences;
            }
        }

        List<CO> relativeCharOccurrences = Arrays.asList(
            new CO('a', 0.3888889f),
            new CO('c', 0.055555556f),
            new CO('d', 0.055555556f),
            new CO('e', 0.3888889f),
            new CO('g', 0.11111111f),
            new CO('h', 0.11111111f),
            new CO('L', 0.055555556f),
            new CO('N', 0.055555556f),
            new CO('n', 0.16666667f),
            new CO('o', 0.055555556f),
            new CO('p', 0.22222222f),
            new CO('r', 0.22222222f),
            new CO('S', 0.055555556f),
            new CO('s', 0.22222222f),
            new CO('t', 0.2777778f),
            new CO('T', 0.055555556f),
            new CO('w', 0.055555556f),
            new CO('x', 0.055555556f)
        );

        Map<Character, Float> actualRelativeOccurrences = result.getRelativeCharOccurrences();

        for (CO expected : relativeCharOccurrences) {
            Float actual = actualRelativeOccurrences.get(expected.c);
            Assert.assertNotNull(actual);

            Assert.assertEquals(expected.relativeOccurrences, actual, 0.0001);

            actualRelativeOccurrences.remove(expected.c);
        }

        Assert.assertEquals(0, actualRelativeOccurrences.size());
    }

    @Test
    public void dotsTest() {
        String text = "First... .. .. Second..\nPrelasts sentense.\nLast....";

        TextAnalyserResult result = new TextAnalyser().analyseText(text);

        Assert.assertEquals(3, result.getParagraphsCount());
        Assert.assertEquals(4, result.getSentensesCount());
    }
}
