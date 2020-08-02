package com.dmytrobilokha.xmbt.bot.weather.dto;

import javax.annotation.CheckForNull;
import javax.json.bind.annotation.JsonbProperty;
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
