package com.dmytrobilokha.xmbt.manager;

import javax.annotation.Nonnull;

public interface Bot extends Runnable {

    @Nonnull
    String getName();

    void setConnector(@Nonnull BotConnector messageQueueClient);

}
