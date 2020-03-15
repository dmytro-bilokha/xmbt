package com.dmytrobilokha.xmbt.bot.ns.dto;

import javax.annotation.CheckForNull;
import javax.json.bind.annotation.JsonbProperty;

public class StationNamesDto {

    @JsonbProperty("lang")
    @CheckForNull
    private String full;
    @JsonbProperty("middel")
    @CheckForNull
    private String medium;
    @JsonbProperty("kort")
    @CheckForNull
    private String brief;

    @CheckForNull
    public String getFull() {
        return full;
    }

    public void setFull(@CheckForNull String full) {
        this.full = full;
    }

    @CheckForNull
    public String getMedium() {
        return medium;
    }

    public void setMedium(@CheckForNull String medium) {
        this.medium = medium;
    }

    @CheckForNull
    public String getBrief() {
        return brief;
    }

    public void setBrief(@CheckForNull String brief) {
        this.brief = brief;
    }

    @Override
    public String toString() {
        return "StationNamesDto{"
                + "full='" + full + '\''
                + ", medium='" + medium + '\''
                + ", brief='" + brief + '\''
                + '}';
    }

}
