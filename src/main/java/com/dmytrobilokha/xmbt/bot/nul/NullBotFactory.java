package com.dmytrobilokha.xmbt.bot.nul;

import com.dmytrobilokha.xmbt.api.BotConnector;
import com.dmytrobilokha.xmbt.api.BotFactory;

import javax.annotation.Nonnull;

public class NullBotFactory implements BotFactory {

    @Override
    @Nonnull
    public String getBotName() {
        return "null";
    }

    @Override
    @Nonnull
    public NullBot produce(@Nonnull BotConnector connector) {
        return new NullBot(connector);
    }

}
