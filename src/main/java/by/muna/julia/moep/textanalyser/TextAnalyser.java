package by.muna.julia.moep.textanalyser;

import by.muna.julia.moep.textanalyser.tokens.ParagraphEndToken;
import by.muna.julia.moep.textanalyser.tokens.SentenseEndToken;
import by.muna.julia.moep.textanalyser.tokens.Token;
import by.muna.julia.moep.textanalyser.tokens.WordToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

public class TextAnalyser {
    public TextAnalyserResult analyseText(File file) throws IOException {
        InputStreamReader ir = new InputStreamReader(new FileInputStream(file));

        try {
            return this.analyseText(ir);
        } finally {
            ir.close();
        }
    }

    public TextAnalyserResult analyseText(String text) {
        try {
            return this.analyseText(new StringReader(text));
        } catch (IOException ex) {
            // StringReader from String cannot fail, I hope
            throw new RuntimeException(ex);
        }
    }

    public TextAnalyserResult analyseText(Reader reader) throws IOException {
        int sentensesCount = 0,
            paragraphsCount = 0;

        int totalCharsCount = 0,
            totalWordsCount = 0;

        final Map<Character, Integer> charOccurrences = new HashMap<>();
        final Map<String, Integer> wordOccurrences = new HashMap<>();
        final Set<String> alphabeticDict = new TreeSet<>(
            (String a, String b) -> a.compareTo(b)
        );

        IOIterator<Token> tokens = TextAnalyser.tokenize(reader);

        while (tokens.hasNext()) {
            Token token = tokens.next();

            switch (token.getType()) {
            case WORD: {
                WordToken wordToken = (WordToken) token;

                String word = wordToken.getWord();
                int wordLength = word.length();

                // Обновляем численные показатели слов/символов
                totalWordsCount++;
                totalCharsCount += wordLength;

                for (int i = 0; i < wordLength; i++) {
                    // Обновяем частоту встретившихся символов
                    charOccurrences.compute(word.charAt(i), (s, old) -> old != null ? old + 1 : 1);
                }

                // Добавляем в алфавитный словарь
                alphabeticDict.add(word);

                // Использование хештаблицы здесь субоптимально (но сложность не возрастает)
                // и можно было бы обойтись лишь одним деревом,
                // но тогда придётся реализовывать его обход, а влом
                wordOccurrences.compute(word, (s, old) -> old != null ? old + 1 : 1);
            } break;
            case SENTENSE_END: sentensesCount++; break;
            case PARAGRAPH_END: paragraphsCount++; break;
            default: throw new RuntimeException("impossible");
            }
        }

        // Нужно чтобы прокинуть их в анонимный класс
        final int sentensesCountFinal = sentensesCount,
                  paragraphsCountFinal = paragraphsCount,
                  totalCharsCountFinal = totalCharsCount,
                  totalWordsCountFinal = totalWordsCount;

        return new TextAnalyserResult() {
            @Override
            public int getWordsCount() {
                return totalWordsCountFinal;
            }

            @Override
            public int getSentensesCount() {
                return sentensesCountFinal;
            }

            @Override
            public int getParagraphsCount() {
                return paragraphsCountFinal;
            }

            @Override
            public float getAverageWordLength() {
                return ((float) totalCharsCountFinal) / totalWordsCountFinal;
            }

            @Override
            public Map<Character, Float> getRelativeCharOccurrences() {
                int differentCharsCount = charOccurrences.size();

                Map<Character, Float> result = new HashMap<>(differentCharsCount);

                for (Entry<Character, Integer> entry : charOccurrences.entrySet()) {
                    char c = entry.getKey();
                    int occurrences = entry.getValue();

                    result.put(c, ((float) occurrences) / differentCharsCount);
                }

                return result;
            }

            @Override
            public Iterator<String> getAlphabeticOrderedDictIterator() {
                return alphabeticDict.iterator();
            }

            @Override
            public Iterator<TextAnalyserWordWithOccurrences> getOccurrencesDescOrderedDictIterator() {
                Set<TextAnalyserWordWithOccurrences> result = new TreeSet<TextAnalyserWordWithOccurrences>(
                    (a, b) -> {
                        int diff = b.getOccurrences() - a.getOccurrences();
                        if (diff != 0) return diff;
                        else return a.getWord().compareTo(b.getWord());
                    }
                );

                for (String word : alphabeticDict) {
                    result.add(new TextAnalyserWordWithOccurrences(word, wordOccurrences.get(word)));
                }

                return result.iterator();
            }
        };
    }

    public static interface IOIterator<T> {
        boolean hasNext() throws IOException;
        T next() throws IOException;
    }
    private static enum SkippedType {
        NEWLINE, PUNCTUATION_END
    }
    public static IOIterator<Token> tokenize(final Reader reader) {
        return new IOIterator<Token>() {
            private char[] buffer = new char[1024];
            private int charsReaded = 0;
            private int offset = 0;

            private Queue<Token> nextTokens = new LinkedList<>();
            private boolean haveAnyWords = false;
            private boolean sentenseClosed = true;
            private boolean eofTokensAdded = false;

            // Возвращает текущий символ не переходя к следующему
            private char peekNextChar() throws IOException {
                if (this.offset >= this.charsReaded) {
                    this.charsReaded = reader.read(this.buffer);
                }

                return this.charsReaded != -1 ? this.buffer[this.offset] : 0;
            }
            // Переходит к следующему символу
            private void skipChar() throws IOException {
                this.offset++;
            }
            private boolean isEof() {
                return this.charsReaded == -1;
            }

            private boolean isLetterChar(char c) {
                return ((c >= 'a') && (c <= 'z')) ||
                    ((c >= 'A') && (c <= 'Z')) ||
                    ((c >= 'а') && (c <= 'я')) ||
                    ((c >= 'А') && (c <= 'Я')) ||
                    ((c >= '0') && (c <= '9')) ||
                    (c == '-') ||
                    (c == '_');
            }

            // Возвращает множество с элементами что пропустили (знаки конца предложения/новая строка)
            private EnumSet<SkippedType> skipNonLetters() throws IOException {
                EnumSet<SkippedType> result = EnumSet.noneOf(SkippedType.class);

                while (true) {
                    char c = this.peekNextChar();
                    if (this.isEof()) break;

                    if (this.isLetterChar(c)) {
                        break;
                    } else {
                        switch (c) {
                        case '\n': result.add(SkippedType.NEWLINE); break;
                        case '.':
                        case '!':
                        case '?':
                            result.add(SkippedType.PUNCTUATION_END);
                            break;
                        }
                        if (c == '\n') result.add(SkippedType.NEWLINE);

                        this.skipChar();
                    }
                }

                return result;
            }
            private boolean makeNextTokens() throws IOException {
                // Пропускаем пустые символы
                EnumSet<SkippedType> skipped = this.skipNonLetters();

                boolean closeSentense = false;

                if (skipped.contains(SkippedType.PUNCTUATION_END) && !this.sentenseClosed) {
                    closeSentense = true;
                    this.nextTokens.add(new SentenseEndToken());
                }
                if (skipped.contains(SkippedType.NEWLINE) && !this.sentenseClosed) {
                    // Если до этого не было знака конца предложения,
                    // будем считать что предложение закончили, а знак забыли.
                    // Если такая функциональность не нужна -- нужно в этом if'е сделать return,
                    // тогда не закрытое предложение не будет заканчивать абзац даже если
                    // в нём есть переносы строк
                    if (!closeSentense) this.nextTokens.add(new SentenseEndToken());

                    closeSentense = true;

                    this.nextTokens.add(new ParagraphEndToken());
                }

                if (closeSentense) this.sentenseClosed = true;

                if (!this.nextTokens.isEmpty()) return true;

                if (this.isEof()) {
                    if (this.eofTokensAdded) return false;

                    this.eofTokensAdded = true;

                    if (this.haveAnyWords) {
                        // Закрываем предложение, если в конце файла не хватает знака конца предложения
                        if (!this.sentenseClosed) {
                            this.nextTokens.add(new SentenseEndToken());
                        }

                        this.nextTokens.add(new ParagraphEndToken());
                    } else {
                        // Если в файле не было никаких слов, то очевидно,
                        // что не нужно закрывать абзац.
                        return false;
                    }
                } else {
                    // Здесь мы находимся на буквенном символе, нужно считать слово
                    // и остановиться после него

                    StringBuilder sb = new StringBuilder();

                    while (true) {
                        char c = this.peekNextChar();
                        if (this.isEof()) break;

                        if (this.isLetterChar(c)) {
                            sb.append(c);
                            this.skipChar();
                        } else {
                            break;
                        }
                    }

                    String word = sb.toString();

                    this.nextTokens.add(new WordToken(word));

                    if (this.sentenseClosed) this.sentenseClosed = false;
                    if (!this.haveAnyWords) this.haveAnyWords = true;
                }

                return true;
            }

            @Override
            public boolean hasNext() throws IOException {
                if (!this.nextTokens.isEmpty()) return true;

                return this.makeNextTokens();
            }

            @Override
            public Token next() throws IOException {
                if (!this.nextTokens.isEmpty()) {
                    return this.nextTokens.poll();
                } else {
                    if (this.makeNextTokens()) {
                        return this.nextTokens.poll();
                    } else {
                        throw new NoSuchElementException();
                    }
                }
            }
        };
    }
}
