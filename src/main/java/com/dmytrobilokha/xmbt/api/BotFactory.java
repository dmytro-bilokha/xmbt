package com.dmytrobilokha.xmbt.api;

import javax.annotation.Nonnull;

public interface BotFactory {

    @Nonnull
    String getBotName();

    @Nonnull
    Runnable produce(@Nonnull BotConnector connector);

}
