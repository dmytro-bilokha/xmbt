package com.dmytrobilokha.xmbt.bot.sysinfo;

import com.dmytrobilokha.xmbt.api.BotConnector;
import com.dmytrobilokha.xmbt.api.BotFactory;

import javax.annotation.Nonnull;

public class SysinfoBotFactory implements BotFactory {

    @Override
    @Nonnull
    public String getBotName() {
        return "sysinfo";
    }

    @Override
    @Nonnull
    public SysinfoBot produce(@Nonnull BotConnector connector) {
        return new SysinfoBot(connector);
    }

}
