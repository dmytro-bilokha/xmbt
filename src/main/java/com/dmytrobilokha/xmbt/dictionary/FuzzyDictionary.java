package com.dmytrobilokha.xmbt.dictionary;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;

public class FuzzyDictionary<T> {

    private final int[] alphabet;
    private final List<Phrase<T>> dictionaryStore;

    private FuzzyDictionary(@Nonnull String alphabetString) {
        this.alphabet = alphabetString.toLowerCase().codePoints().sorted().distinct().toArray();
        this.dictionaryStore = new ArrayList<>();
    }

    public static <T> FuzzyDictionary<T> withLatinLetters() {
        return new FuzzyDictionary<>("qwertyuioplkjhgfdsazxcvbnm");
    }

    public void put(@Nonnull String phraseText, @Nonnull T phraseObject) {
        dictionaryStore.add(new Phrase<>(phraseText, phraseObject));
    }

    @Nonnull
    public List<T> get(@Nonnull String fuzzyPhrase) {
        Phrase fuzzy = new Phrase<>(fuzzyPhrase, null);
        return dictionaryStore.stream()
                .filter(p -> p.matches(fuzzy))
                .map(p -> p.phraseObject)
                .collect(Collectors.toList());
    }

    private class Phrase<T> {

        final T phraseObject;
        @Nonnull
        final int[] codePoints;
        @Nonnull
        final BitSet key;

        Phrase(@Nonnull String phraseText, T phraseObject) {
            this.phraseObject = phraseObject;
            this.codePoints = phraseText.toLowerCase()
                    .codePoints()
                    .filter(codePoint -> Arrays.binarySearch(alphabet, codePoint) >= 0)
                    .toArray();
            this.key = calculateKey(codePoints);
        }

        @Nonnull
        private BitSet calculateKey(@Nonnull int[] phraseCodePoints) {
            BitSet calculatedKey = new BitSet(alphabet.length);
            for (int codePoint : phraseCodePoints) {
                int positionInAlphabet = Arrays.binarySearch(alphabet, codePoint);
                if (positionInAlphabet >= 0) {
                    calculatedKey.set(positionInAlphabet);
                }
            }
            return calculatedKey;
        }

        boolean matches(@Nonnull Phrase lookup) {
            if (this.codePoints.length == 0
                    || lookup.codePoints.length == 0
                    || lookup.codePoints.length > this.codePoints.length) {
                return false;
            }
            BitSet lookupAndKey = (BitSet) lookup.key.clone();
            lookupAndKey.and(key);
            if (!lookup.key.equals(lookupAndKey)) {
                return false;
            }
            int searchFrom = 0;
            for (int codePoint : lookup.codePoints) {
                searchFrom = findCodePoint(this.codePoints, codePoint, searchFrom);
                if (searchFrom < 0) {
                    return false;
                }
            }
            return true;
        }

        private int findCodePoint(@Nonnull int[] codePoints, int codeValue, int startingIndex) {
            if (startingIndex >= codePoints.length) {
                return -1;
            }
            for (int index = startingIndex; index < codePoints.length; index++) {
                if (codePoints[index] == codeValue) {
                    return index + 1;
                }
            }
            return -1;
        }

    }

}
