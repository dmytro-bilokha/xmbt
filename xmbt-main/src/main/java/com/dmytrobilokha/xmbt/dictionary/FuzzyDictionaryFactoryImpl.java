package com.dmytrobilokha.xmbt.dictionary;

import com.dmytrobilokha.xmbt.api.service.dictionary.FuzzyDictionary;
import com.dmytrobilokha.xmbt.api.service.dictionary.FuzzyDictionaryFactory;

import javax.annotation.Nonnull;

public class FuzzyDictionaryFactoryImpl implements FuzzyDictionaryFactory {

    @Nonnull
    @Override
    public <T> FuzzyDictionary<T> produceWithCustomAlphabet(@Nonnull String alphabet) {
        return new FuzzyDictionaryImpl<>(alphabet);
    }

    @Nonnull
    @Override
    public <T> FuzzyDictionary<T> produceWithLatinAlphabet() {
        return new FuzzyDictionaryImpl<>("qwertyuioplkjhgfdsazxcvbnm");
    }
}
