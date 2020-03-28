package com.dmytrobilokha.xmbt.bot.sysinfo;

import com.dmytrobilokha.xmbt.api.bot.BotFactory;
import com.dmytrobilokha.xmbt.api.messaging.MessageBus;
import com.dmytrobilokha.xmbt.api.service.ServiceContainer;

import javax.annotation.Nonnull;

public class SysinfoBotFactory implements BotFactory {

    @Override
    @Nonnull
    public String getBotName() {
        return "sysinfo";
    }

    @Override
    @Nonnull
    public SysinfoBot produce(@Nonnull MessageBus connector, @Nonnull ServiceContainer serviceContainer) {
        return new SysinfoBot(connector);
    }

}
