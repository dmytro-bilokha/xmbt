package com.dmytrobilokha.xmbt.bot.nullbot;

import com.dmytrobilokha.xmbt.api.bot.BotFactory;
import com.dmytrobilokha.xmbt.api.messaging.MessageBus;
import com.dmytrobilokha.xmbt.api.service.ServiceContainer;

import javax.annotation.Nonnull;

public class NullBotFactory implements BotFactory {

    @Override
    @Nonnull
    public String getBotName() {
        return "null";
    }

    @Override
    @Nonnull
    public NullBot produce(@Nonnull MessageBus connector, @Nonnull ServiceContainer serviceContainer) {
        return new NullBot(connector);
    }

}
