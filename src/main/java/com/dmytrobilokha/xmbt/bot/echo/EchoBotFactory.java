package com.dmytrobilokha.xmbt.bot.echo;

import com.dmytrobilokha.xmbt.api.BotConnector;
import com.dmytrobilokha.xmbt.api.BotFactory;

import javax.annotation.Nonnull;

public class EchoBotFactory implements BotFactory {

    @Override
    @Nonnull
    public String getBotName() {
        return "echo";
    }

    @Override
    @Nonnull
    public EchoBot produce(@Nonnull BotConnector connector) {
        return new EchoBot(connector);
    }

}
