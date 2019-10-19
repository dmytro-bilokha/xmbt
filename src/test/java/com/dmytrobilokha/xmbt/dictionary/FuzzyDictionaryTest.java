package com.dmytrobilokha.xmbt.dictionary;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

@Test(groups = "unit")
public class FuzzyDictionaryTest {

    @DataProvider(name = "dictionaryCases")
    public Object[][] getDictionaryCases() {
        return new Object[][]{
                {List.of("quit", "exit"), "q", List.of("quit")}
                , {List.of("quit", "exit"), "x", List.of("exit")}
                , {List.of("quit", "exit"), "m", List.of()}
                , {List.of("QUIT", "quit"), "t", List.of("QUIT", "quit")}
                , {List.of("quit", "exit"), "Q", List.of("quit")}
                , {List.of("quit", "exit"), "i", List.of("quit", "exit")}
                , {List.of("quit", "exit"), "xi", List.of("exit")}
                , {List.of("q1uit", "e xit "), "13i42", List.of("q1uit", "e xit ")}
                , {List.of("quit", "exit"), "itx", List.of()}
                , {List.of("quit", "exit"), "ti", List.of()}
        };
    }

    @Test(dataProvider = "dictionaryCases")
    public void testDictionarySearchWithLatinChars(List<String> phrases, String lookupString, List<String> result) {
        FuzzyDictionary<String> dictionary = FuzzyDictionary.withLatinLetters();
        phrases.forEach(phrase -> dictionary.put(phrase, phrase));
        Assert.assertEquals(result, dictionary.get(lookupString));
    }

}
