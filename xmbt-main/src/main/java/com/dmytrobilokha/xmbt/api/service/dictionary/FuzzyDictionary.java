package com.dmytrobilokha.xmbt.api.service.dictionary;

import javax.annotation.Nonnull;
import java.util.List;

public interface FuzzyDictionary<T> {

    void put(@Nonnull String phraseText, @Nonnull T phraseObject);

    List<T> get(@Nonnull String fuzzyPhrase);

    void clear();

}
