package com.dmytrobilokha.xmbt.bot.ns.dto;

import javax.annotation.CheckForNull;
import javax.json.bind.annotation.JsonbProperty;

public class StationInfo {

    @JsonbProperty("namen")
    @CheckForNull
    private StationNames names;
    @JsonbProperty("land")
    @CheckForNull
    private String countryCode;
    @JsonbProperty("EVACode")
    @CheckForNull
    private Long evaCode;
    @CheckForNull
    private String code;

    @CheckForNull
    public StationNames getNames() {
        return names;
    }

    public void setNames(@CheckForNull StationNames names) {
        this.names = names;
    }

    @CheckForNull
    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(@CheckForNull String countryCode) {
        this.countryCode = countryCode;
    }

    @CheckForNull
    public Long getEvaCode() {
        return evaCode;
    }

    public void setEvaCode(@CheckForNull Long evaCode) {
        this.evaCode = evaCode;
    }

    @CheckForNull
    public String getCode() {
        return code;
    }

    public void setCode(@CheckForNull String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "StationInfo{"
                + "names=" + names
                + ", countryCode='" + countryCode + '\''
                + ", evaCode=" + evaCode
                + ", code='" + code + '\''
                + '}';
    }

}
