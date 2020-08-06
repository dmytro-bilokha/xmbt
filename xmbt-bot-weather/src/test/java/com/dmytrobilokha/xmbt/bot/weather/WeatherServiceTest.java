package com.dmytrobilokha.xmbt.bot.weather;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

@Test(groups = "unit")
public class WeatherServiceTest {

    private WeerliveApiClient weerliveMock;
    private BuienradarApiClient buienradarMock;
    private WeatherService service;

    @BeforeClass
    public void init() {
        weerliveMock = Mockito.mock(WeerliveApiClient.class);
        buienradarMock = Mockito.mock(BuienradarApiClient.class);
        service = new WeatherService(weerliveMock, buienradarMock);
    }

    @BeforeMethod
    public void resetMocks() {
        Mockito.reset(weerliveMock, buienradarMock);
    }

    @DataProvider(name = "buienradarCases")
    public Object[][] getBuienradarCases() {
        return new Object[][]{
                {"_", 0.1}
                , {"L", 2.45}
                , {"M", 8.8}
                , {"H", 48.8}
                , {"V", 57.0}
                , {"_LMHVVHML_", 0.05, 2.4, 5.5, 11.0, 50.1, 60.1, 49.9, 9.99, 1.1, 0.01}
        };
    }

    @Test(dataProvider = "buienradarCases")
    public void formatsRainForecast(String expectedLevelsString, Double... precipitations) throws Exception {
        final var startTime = "12:34";
        final var endTime = "56:78";
        final var cityName = "testCity";
        final var expectedOutput = "Weather in " + cityName + "\n"
                + startTime + '|' + expectedLevelsString + '|' + endTime;
        Mockito.when(weerliveMock.fetch(Mockito.any())).thenThrow(new WeatherApiException("test"));
        var testForecast = new RainForecast(
                startTime
                , endTime
                , Arrays.stream(precipitations).collect(Collectors.toList())
        );
        Mockito.when(buienradarMock.fetch(Mockito.any())).thenReturn(testForecast);
        Assert.assertEquals(service.fetchWeatherReport(new City(cityName, "", "")), expectedOutput);
    }

}
