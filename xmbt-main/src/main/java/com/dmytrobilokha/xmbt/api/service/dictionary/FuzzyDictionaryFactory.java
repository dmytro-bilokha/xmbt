package com.dmytrobilokha.xmbt.api.service.dictionary;

import javax.annotation.Nonnull;

public interface FuzzyDictionaryFactory {

    @Nonnull
    <T> FuzzyDictionary<T> produceWithCustomAlphabet(@Nonnull String alphabet);

    @Nonnull
    <T> FuzzyDictionary<T> produceWithLatinAlphabet();

}
