package com.dmytrobilokha.xmbt.api.bot;

import com.dmytrobilokha.xmbt.api.messaging.MessageBus;
import com.dmytrobilokha.xmbt.api.service.ServiceContainer;

import javax.annotation.Nonnull;

public interface BotFactory {

    @Nonnull
    String getBotName();

    @Nonnull
    Runnable produce(@Nonnull MessageBus connector, @Nonnull ServiceContainer serviceContainer);

}
