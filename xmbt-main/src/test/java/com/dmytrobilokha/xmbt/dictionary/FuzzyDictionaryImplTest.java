package com.dmytrobilokha.xmbt.dictionary;

import com.dmytrobilokha.xmbt.api.service.dictionary.FuzzyDictionary;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

@Test(groups = "unit")
public class FuzzyDictionaryImplTest {

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
                , {List.of("Maastricht", "Maastricht Noord"), "Maastricht", List.of("Maastricht")}
                , {List.of("Maastricht", "Maastricht Noord"), "maastricht", List.of("Maastricht")}
        };
    }

    @Test(dataProvider = "dictionaryCases")
    public void testDictionarySearchWithLatinChars(List<String> phrases, String lookupString, List<String> result) {
        FuzzyDictionary<String> dictionary = new FuzzyDictionaryFactoryImpl().produceWithLatinAlphabet();
        phrases.forEach(phrase -> dictionary.put(phrase, phrase));
        Assert.assertEquals(dictionary.get(lookupString), result);
    }

}
