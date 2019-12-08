package com.dmytrobilokha.xmbt.bot.ns;

import javax.annotation.Nonnull;

class NsTrainStation {

    private final long evaCode;
    @Nonnull
    private final String code;
    @Nonnull
    private final String name;

    NsTrainStation(long evaCode, @Nonnull String code, @Nonnull String name) {
        this.evaCode = evaCode;
        this.code = code;
        this.name = name;
    }

    long getEvaCode() {
        return evaCode;
    }

    @Nonnull
    String getCode() {
        return code;
    }

    @Nonnull
    String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "NsTrainStation{"
                + "evaCode=" + evaCode
                + ", code=" + code
                + ", name='" + name + '\''
                + '}';
    }

}
