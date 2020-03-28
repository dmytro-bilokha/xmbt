package com.dmytrobilokha.xmbt.bot.ns;

import javax.annotation.Nonnull;

class NsApiException extends NsServiceException {

    NsApiException(@Nonnull String message) {
        super(message);
    }

    NsApiException(@Nonnull String message, @Nonnull Exception ex) {
        super(message, ex);
    }

}
