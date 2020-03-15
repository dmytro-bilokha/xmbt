package com.dmytrobilokha.xmbt.bot.ns;

import javax.annotation.Nonnull;

class NsServiceException extends Exception {

    NsServiceException(@Nonnull String message) {
        super(message);
    }

    NsServiceException(@Nonnull String message, @Nonnull Exception ex) {
        super(message, ex);
    }

}
