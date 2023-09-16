package com.dmytrobilokha.xmbt.bot.weather.dto;

import jakarta.json.bind.annotation.JsonbProperty;

import javax.annotation.CheckForNull;

@SuppressWarnings("PMD")
public class LiveWeather {

    @JsonbProperty("plaats")
    @CheckForNull
    private String place;

    @JsonbProperty("temp")
    @CheckForNull
    private String temperature;

    @JsonbProperty("gtemp")
    @CheckForNull
    private String feelsLikeTemperature;

    @JsonbProperty("samenv")
    @CheckForNull
    private String weatherDescription;

    @JsonbProperty("lv")
    @CheckForNull
    private String relativeHumidity;

    @JsonbProperty("windr")
    @CheckForNull
    private String windDirection;

    @JsonbProperty("windms")
    @CheckForNull
    private String windSpeedMps;

    @JsonbProperty("winds")
    @CheckForNull
    private String windForceBeaufort;

    @JsonbProperty("windk")
    @CheckForNull
    private String windSpeedKnots;

    @JsonbProperty("windkmh")
    @CheckForNull
    private String windSpeedKmh;

    @JsonbProperty("luchtd")
    @CheckForNull
    private String airPressureMbar;

    @JsonbProperty("ldmmhg")
    @CheckForNull
    private String airPressureMmHg;

    @JsonbProperty("dauwp")
    @CheckForNull
    private String dewPoint;

    @JsonbProperty("zicht")
    @CheckForNull
    private String sightKm;

    @JsonbProperty("verw")
    @CheckForNull
    private String dayForecastText;

    @JsonbProperty("sup")
    @CheckForNull
    private String sunupTime;

    @JsonbProperty("sunder")
    @CheckForNull
    private String sundownTime;

    @JsonbProperty("image")
    @CheckForNull
    private String weatherImageName;

    @JsonbProperty("d0weer")
    @CheckForNull
    private String d0WeatherType;

    @JsonbProperty("d0tmax")
    @CheckForNull
    private String d0MaxTemperature;

    @JsonbProperty("d0tmin")
    @CheckForNull
    private String d0MinTemperature;

    @JsonbProperty("d0windk")
    @CheckForNull
    private String d0WindForceBeaufort;

    @JsonbProperty("d0windknp")
    @CheckForNull
    private String d0WindSpeedKnots;

    @JsonbProperty("d0windms")
    @CheckForNull
    private String d0WindSpeedMps;

    @JsonbProperty("d0windkmh")
    @CheckForNull
    private String d0WindSpeedKmh;

    @JsonbProperty("d0windr")
    @CheckForNull
    private String d0WindDirection;

    @JsonbProperty("d0neerslag")
    @CheckForNull
    private String d0PrecipitationProbability;

    @JsonbProperty("d0zon")
    @CheckForNull
    private String d0SunProbability;

    @JsonbProperty("d1weer")
    @CheckForNull
    private String d1WeatherType;

    @JsonbProperty("d1tmax")
    @CheckForNull
    private String d1MaxTemperature;

    @JsonbProperty("d1tmin")
    @CheckForNull
    private String d1MinTemperature;

    @JsonbProperty("d1windk")
    @CheckForNull
    private String d1WindForceBeaufort;

    @JsonbProperty("d1windknp")
    @CheckForNull
    private String d1WindSpeedKnots;

    @JsonbProperty("d1windms")
    @CheckForNull
    private String d1WindSpeedMps;

    @JsonbProperty("d1windkmh")
    @CheckForNull
    private String d1WindSpeedKmh;

    @JsonbProperty("d1windr")
    @CheckForNull
    private String d1WindDirection;

    @JsonbProperty("d1neerslag")
    @CheckForNull
    private String d1PrecipitationProbability;

    @JsonbProperty("d1zon")
    @CheckForNull
    private String d1SunProbability;

    @JsonbProperty("d2weer")
    @CheckForNull
    private String d2WeatherType;

    @JsonbProperty("d2tmax")
    @CheckForNull
    private String d2MaxTemperature;

    @JsonbProperty("d2tmin")
    @CheckForNull
    private String d2MinTemperature;

    @JsonbProperty("d2windk")
    @CheckForNull
    private String d2WindForceBeaufort;

    @JsonbProperty("d2windknp")
    @CheckForNull
    private String d2WindSpeedKnots;

    @JsonbProperty("d2windms")
    @CheckForNull
    private String d2WindSpeedMps;

    @JsonbProperty("d2windkmh")
    @CheckForNull
    private String d2WindSpeedKmh;

    @JsonbProperty("d2windr")
    @CheckForNull
    private String d2WindDirection;

    @JsonbProperty("d2neerslag")
    @CheckForNull
    private String d2PrecipitationProbability;

    @JsonbProperty("d2zon")
    @CheckForNull
    private String d2SunProbability;

    @JsonbProperty("alarm")
    @CheckForNull
    private String alarmPresence;

    @JsonbProperty("alarmtxt")
    @CheckForNull
    private String alarmText;

    @CheckForNull
    public String getPlace() {
        return place;
    }

    public void setPlace(@CheckForNull String place) {
        this.place = place;
    }

    @CheckForNull
    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(@CheckForNull String temperature) {
        this.temperature = temperature;
    }

    @CheckForNull
    public String getFeelsLikeTemperature() {
        return feelsLikeTemperature;
    }

    public void setFeelsLikeTemperature(@CheckForNull String feelsLikeTemperature) {
        this.feelsLikeTemperature = feelsLikeTemperature;
    }

    @CheckForNull
    public String getWeatherDescription() {
        return weatherDescription;
    }

    public void setWeatherDescription(@CheckForNull String weatherDescription) {
        this.weatherDescription = weatherDescription;
    }

    @CheckForNull
    public String getRelativeHumidity() {
        return relativeHumidity;
    }

    public void setRelativeHumidity(@CheckForNull String relativeHumidity) {
        this.relativeHumidity = relativeHumidity;
    }

    @CheckForNull
    public String getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(@CheckForNull String windDirection) {
        this.windDirection = windDirection;
    }

    @CheckForNull
    public String getWindSpeedMps() {
        return windSpeedMps;
    }

    public void setWindSpeedMps(@CheckForNull String windSpeedMps) {
        this.windSpeedMps = windSpeedMps;
    }

    @CheckForNull
    public String getWindForceBeaufort() {
        return windForceBeaufort;
    }

    public void setWindForceBeaufort(@CheckForNull String windForceBeaufort) {
        this.windForceBeaufort = windForceBeaufort;
    }

    @CheckForNull
    public String getWindSpeedKnots() {
        return windSpeedKnots;
    }

    public void setWindSpeedKnots(@CheckForNull String windSpeedKnots) {
        this.windSpeedKnots = windSpeedKnots;
    }

    @CheckForNull
    public String getWindSpeedKmh() {
        return windSpeedKmh;
    }

    public void setWindSpeedKmh(@CheckForNull String windSpeedKmh) {
        this.windSpeedKmh = windSpeedKmh;
    }

    @CheckForNull
    public String getAirPressureMbar() {
        return airPressureMbar;
    }

    public void setAirPressureMbar(@CheckForNull String airPressureMbar) {
        this.airPressureMbar = airPressureMbar;
    }

    @CheckForNull
    public String getAirPressureMmHg() {
        return airPressureMmHg;
    }

    public void setAirPressureMmHg(@CheckForNull String airPressureMmHg) {
        this.airPressureMmHg = airPressureMmHg;
    }

    @CheckForNull
    public String getDewPoint() {
        return dewPoint;
    }

    public void setDewPoint(@CheckForNull String dewPoint) {
        this.dewPoint = dewPoint;
    }

    @CheckForNull
    public String getSightKm() {
        return sightKm;
    }

    public void setSightKm(@CheckForNull String sightKm) {
        this.sightKm = sightKm;
    }

    @CheckForNull
    public String getDayForecastText() {
        return dayForecastText;
    }

    public void setDayForecastText(@CheckForNull String dayForecastText) {
        this.dayForecastText = dayForecastText;
    }

    @CheckForNull
    public String getSunupTime() {
        return sunupTime;
    }

    public void setSunupTime(@CheckForNull String sunupTime) {
        this.sunupTime = sunupTime;
    }

    @CheckForNull
    public String getSundownTime() {
        return sundownTime;
    }

    public void setSundownTime(@CheckForNull String sundownTime) {
        this.sundownTime = sundownTime;
    }

    @CheckForNull
    public String getWeatherImageName() {
        return weatherImageName;
    }

    public void setWeatherImageName(@CheckForNull String weatherImageName) {
        this.weatherImageName = weatherImageName;
    }

    @CheckForNull
    public String getD0WeatherType() {
        return d0WeatherType;
    }

    public void setD0WeatherType(@CheckForNull String d0WeatherType) {
        this.d0WeatherType = d0WeatherType;
    }

    @CheckForNull
    public String getD0MaxTemperature() {
        return d0MaxTemperature;
    }

    public void setD0MaxTemperature(@CheckForNull String d0MaxTemperature) {
        this.d0MaxTemperature = d0MaxTemperature;
    }

    @CheckForNull
    public String getD0MinTemperature() {
        return d0MinTemperature;
    }

    public void setD0MinTemperature(@CheckForNull String d0MinTemperature) {
        this.d0MinTemperature = d0MinTemperature;
    }

    @CheckForNull
    public String getD0WindForceBeaufort() {
        return d0WindForceBeaufort;
    }

    public void setD0WindForceBeaufort(@CheckForNull String d0WindForceBeaufort) {
        this.d0WindForceBeaufort = d0WindForceBeaufort;
    }

    @CheckForNull
    public String getD0WindSpeedKnots() {
        return d0WindSpeedKnots;
    }

    public void setD0WindSpeedKnots(@CheckForNull String d0WindSpeedKnots) {
        this.d0WindSpeedKnots = d0WindSpeedKnots;
    }

    @CheckForNull
    public String getD0WindSpeedMps() {
        return d0WindSpeedMps;
    }

    public void setD0WindSpeedMps(@CheckForNull String d0WindSpeedMps) {
        this.d0WindSpeedMps = d0WindSpeedMps;
    }

    @CheckForNull
    public String getD0WindSpeedKmh() {
        return d0WindSpeedKmh;
    }

    public void setD0WindSpeedKmh(@CheckForNull String d0WindSpeedKmh) {
        this.d0WindSpeedKmh = d0WindSpeedKmh;
    }

    @CheckForNull
    public String getD0WindDirection() {
        return d0WindDirection;
    }

    public void setD0WindDirection(@CheckForNull String d0WindDirection) {
        this.d0WindDirection = d0WindDirection;
    }

    @CheckForNull
    public String getD0PrecipitationProbability() {
        return d0PrecipitationProbability;
    }

    public void setD0PrecipitationProbability(@CheckForNull String d0PrecipitationProbability) {
        this.d0PrecipitationProbability = d0PrecipitationProbability;
    }

    @CheckForNull
    public String getD0SunProbability() {
        return d0SunProbability;
    }

    public void setD0SunProbability(@CheckForNull String d0SunProbability) {
        this.d0SunProbability = d0SunProbability;
    }

    @CheckForNull
    public String getD1WeatherType() {
        return d1WeatherType;
    }

    public void setD1WeatherType(@CheckForNull String d1WeatherType) {
        this.d1WeatherType = d1WeatherType;
    }

    @CheckForNull
    public String getD1MaxTemperature() {
        return d1MaxTemperature;
    }

    public void setD1MaxTemperature(@CheckForNull String d1MaxTemperature) {
        this.d1MaxTemperature = d1MaxTemperature;
    }

    @CheckForNull
    public String getD1MinTemperature() {
        return d1MinTemperature;
    }

    public void setD1MinTemperature(@CheckForNull String d1MinTemperature) {
        this.d1MinTemperature = d1MinTemperature;
    }

    @CheckForNull
    public String getD1WindForceBeaufort() {
        return d1WindForceBeaufort;
    }

    public void setD1WindForceBeaufort(@CheckForNull String d1WindForceBeaufort) {
        this.d1WindForceBeaufort = d1WindForceBeaufort;
    }

    @CheckForNull
    public String getD1WindSpeedKnots() {
        return d1WindSpeedKnots;
    }

    public void setD1WindSpeedKnots(@CheckForNull String d1WindSpeedKnots) {
        this.d1WindSpeedKnots = d1WindSpeedKnots;
    }

    @CheckForNull
    public String getD1WindSpeedMps() {
        return d1WindSpeedMps;
    }

    public void setD1WindSpeedMps(@CheckForNull String d1WindSpeedMps) {
        this.d1WindSpeedMps = d1WindSpeedMps;
    }

    @CheckForNull
    public String getD1WindSpeedKmh() {
        return d1WindSpeedKmh;
    }

    public void setD1WindSpeedKmh(@CheckForNull String d1WindSpeedKmh) {
        this.d1WindSpeedKmh = d1WindSpeedKmh;
    }

    @CheckForNull
    public String getD1WindDirection() {
        return d1WindDirection;
    }

    public void setD1WindDirection(@CheckForNull String d1WindDirection) {
        this.d1WindDirection = d1WindDirection;
    }

    @CheckForNull
    public String getD1PrecipitationProbability() {
        return d1PrecipitationProbability;
    }

    public void setD1PrecipitationProbability(@CheckForNull String d1PrecipitationProbability) {
        this.d1PrecipitationProbability = d1PrecipitationProbability;
    }

    @CheckForNull
    public String getD1SunProbability() {
        return d1SunProbability;
    }

    public void setD1SunProbability(@CheckForNull String d1SunProbability) {
        this.d1SunProbability = d1SunProbability;
    }

    @CheckForNull
    public String getD2WeatherType() {
        return d2WeatherType;
    }

    public void setD2WeatherType(@CheckForNull String d2WeatherType) {
        this.d2WeatherType = d2WeatherType;
    }

    @CheckForNull
    public String getD2MaxTemperature() {
        return d2MaxTemperature;
    }

    public void setD2MaxTemperature(@CheckForNull String d2MaxTemperature) {
        this.d2MaxTemperature = d2MaxTemperature;
    }

    @CheckForNull
    public String getD2MinTemperature() {
        return d2MinTemperature;
    }

    public void setD2MinTemperature(@CheckForNull String d2MinTemperature) {
        this.d2MinTemperature = d2MinTemperature;
    }

    @CheckForNull
    public String getD2WindForceBeaufort() {
        return d2WindForceBeaufort;
    }

    public void setD2WindForceBeaufort(@CheckForNull String d2WindForceBeaufort) {
        this.d2WindForceBeaufort = d2WindForceBeaufort;
    }

    @CheckForNull
    public String getD2WindSpeedKnots() {
        return d2WindSpeedKnots;
    }

    public void setD2WindSpeedKnots(@CheckForNull String d2WindSpeedKnots) {
        this.d2WindSpeedKnots = d2WindSpeedKnots;
    }

    @CheckForNull
    public String getD2WindSpeedMps() {
        return d2WindSpeedMps;
    }

    public void setD2WindSpeedMps(@CheckForNull String d2WindSpeedMps) {
        this.d2WindSpeedMps = d2WindSpeedMps;
    }

    @CheckForNull
    public String getD2WindSpeedKmh() {
        return d2WindSpeedKmh;
    }

    public void setD2WindSpeedKmh(@CheckForNull String d2WindSpeedKmh) {
        this.d2WindSpeedKmh = d2WindSpeedKmh;
    }

    @CheckForNull
    public String getD2WindDirection() {
        return d2WindDirection;
    }

    public void setD2WindDirection(@CheckForNull String d2WindDirection) {
        this.d2WindDirection = d2WindDirection;
    }

    @CheckForNull
    public String getD2PrecipitationProbability() {
        return d2PrecipitationProbability;
    }

    public void setD2PrecipitationProbability(@CheckForNull String d2PrecipitationProbability) {
        this.d2PrecipitationProbability = d2PrecipitationProbability;
    }

    @CheckForNull
    public String getD2SunProbability() {
        return d2SunProbability;
    }

    public void setD2SunProbability(@CheckForNull String d2SunProbability) {
        this.d2SunProbability = d2SunProbability;
    }

    @CheckForNull
    public String getAlarmPresence() {
        return alarmPresence;
    }

    public void setAlarmPresence(@CheckForNull String alarmPresence) {
        this.alarmPresence = alarmPresence;
    }

    @CheckForNull
    public String getAlarmText() {
        return alarmText;
    }

    public void setAlarmText(@CheckForNull String alarmText) {
        this.alarmText = alarmText;
    }

    @Override
    public String toString() {
        return "LiveWeather{"
                + "place='" + place + '\''
                + ", temperature='" + temperature + '\''
                + ", feelsLikeTemperature='" + feelsLikeTemperature + '\''
                + ", weatherDescription='" + weatherDescription + '\''
                + ", relativeHumidity='" + relativeHumidity + '\''
                + ", windDirection='" + windDirection + '\''
                + ", windSpeedMps='" + windSpeedMps + '\''
                + ", windForceBeaufort='" + windForceBeaufort + '\''
                + ", windSpeedKnots='" + windSpeedKnots + '\''
                + ", windSpeedKmh='" + windSpeedKmh + '\''
                + ", airPressureMbar='" + airPressureMbar + '\''
                + ", airPressureMmHg='" + airPressureMmHg + '\''
                + ", dewPoint='" + dewPoint + '\''
                + ", sightKm='" + sightKm + '\''
                + ", dayForecastText='" + dayForecastText + '\''
                + ", sunupTime='" + sunupTime + '\''
                + ", sundownTime='" + sundownTime + '\''
                + ", weatherImageName='" + weatherImageName + '\''
                + ", d0WeatherType='" + d0WeatherType + '\''
                + ", d0MaxTemperature='" + d0MaxTemperature + '\''
                + ", d0MinTemperature='" + d0MinTemperature + '\''
                + ", d0WindForceBeaufort='" + d0WindForceBeaufort + '\''
                + ", d0WindSpeedKnots='" + d0WindSpeedKnots + '\''
                + ", d0WindSpeedMps='" + d0WindSpeedMps + '\''
                + ", d0WindSpeedKmh='" + d0WindSpeedKmh + '\''
                + ", d0WindDirection='" + d0WindDirection + '\''
                + ", d0PrecipitationProbability='" + d0PrecipitationProbability + '\''
                + ", d0SunProbability='" + d0SunProbability + '\''
                + ", d1WeatherType='" + d1WeatherType + '\''
                + ", d1MaxTemperature='" + d1MaxTemperature + '\''
                + ", d1MinTemperature='" + d1MinTemperature + '\''
                + ", d1WindForceBeaufort='" + d1WindForceBeaufort + '\''
                + ", d1WindSpeedKnots='" + d1WindSpeedKnots + '\''
                + ", d1WindSpeedMps='" + d1WindSpeedMps + '\''
                + ", d1WindSpeedKmh='" + d1WindSpeedKmh + '\''
                + ", d1WindDirection='" + d1WindDirection + '\''
                + ", d1PrecipitationProbability='" + d1PrecipitationProbability + '\''
                + ", d1SunProbability='" + d1SunProbability + '\''
                + ", d2WeatherType='" + d2WeatherType + '\''
                + ", d2MaxTemperature='" + d2MaxTemperature + '\''
                + ", d2MinTemperature='" + d2MinTemperature + '\''
                + ", d2WindForceBeaufort='" + d2WindForceBeaufort + '\''
                + ", d2WindSpeedKnots='" + d2WindSpeedKnots + '\''
                + ", d2WindSpeedMps='" + d2WindSpeedMps + '\''
                + ", d2WindSpeedKmh='" + d2WindSpeedKmh + '\''
                + ", d2WindDirection='" + d2WindDirection + '\''
                + ", d2PrecipitationProbability='" + d2PrecipitationProbability + '\''
                + ", d2SunProbability='" + d2SunProbability + '\''
                + ", alarmPresence='" + alarmPresence + '\''
                + ", alarmText='" + alarmText + '\''
                + '}';
    }

}
