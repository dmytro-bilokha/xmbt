package com.dmytrobilokha.xmbt.bot.rain;

import javax.annotation.Nonnull;

class RainApiException extends Exception {

    RainApiException(@Nonnull String message) {
        super(message);
    }

    RainApiException(@Nonnull String message, @Nonnull Exception ex) {
        super(message, ex);
    }

}
