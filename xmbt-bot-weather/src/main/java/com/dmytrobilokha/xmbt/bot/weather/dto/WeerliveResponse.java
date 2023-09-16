package com.dmytrobilokha.xmbt.bot.weather.dto;

import jakarta.json.bind.annotation.JsonbProperty;

import javax.annotation.CheckForNull;
import java.util.List;

public class WeerliveResponse {

    @JsonbProperty("liveweer")
    @CheckForNull
    private List<LiveWeather> liveWeather;

    @CheckForNull
    public List<LiveWeather> getLiveWeather() {
        return liveWeather;
    }

    public void setLiveWeather(@CheckForNull List<LiveWeather> liveWeather) {
        this.liveWeather = liveWeather;
    }

    @Override
    public String toString() {
        return "WeerliveResponse{"
                + "liveWeather=" + liveWeather
                + '}';
    }

}
