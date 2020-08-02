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
                {" ", 0}
                , {" ", 27}
                , {"▁", 29}
                , {"▁", 56}
                , {"▂", 57}
                , {" ▁▂▃▄▅▆▇█ ", 15, 30, 60, 90, 120, 150, 180, 210, 240, 20}
        };
    }

    @Test(dataProvider = "buienradarCases")
    public void formatsRainForecast(String expectedLevelsString, Integer... precipitations) throws Exception {
        final var startTime = "12:34";
        final var endTime = "56:78";
        final var cityName = "testCity";
        final var expectedOutput = "Weather in " + cityName + "\n"
                + startTime + '|' + expectedLevelsString + '|' + endTime;
        Mockito.when(weerliveMock.fetchWeatherData(Mockito.any())).thenThrow(new WeatherApiException("test"));
        var testForecast = new RainForecast(
                startTime
                , endTime
                , Arrays.stream(precipitations).collect(Collectors.toList())
        );
        Mockito.when(buienradarMock.fetchRainForecast(Mockito.any())).thenReturn(testForecast);
        Assert.assertEquals(service.fetchWeatherReport(new City(cityName, "", "")), expectedOutput);
    }

}
