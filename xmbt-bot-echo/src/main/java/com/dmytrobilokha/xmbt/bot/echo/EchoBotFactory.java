package com.dmytrobilokha.xmbt.bot.echo;

import com.dmytrobilokha.xmbt.api.bot.BotFactory;
import com.dmytrobilokha.xmbt.api.messaging.MessageBus;
import com.dmytrobilokha.xmbt.api.service.ServiceContainer;

import javax.annotation.Nonnull;

public class EchoBotFactory implements BotFactory {

    @Override
    @Nonnull
    public String getBotName() {
        return "echo";
    }

    @Override
    @Nonnull
    public EchoBot produce(@Nonnull MessageBus connector, @Nonnull ServiceContainer serviceContainer) {
        return new EchoBot(connector);
    }

}
