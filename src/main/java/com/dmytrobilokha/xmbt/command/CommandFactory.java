package com.dmytrobilokha.xmbt.command;

import com.dmytrobilokha.xmbt.command.list.ListCommand;
import com.dmytrobilokha.xmbt.command.subscribe.SubscribeCommand;
import com.dmytrobilokha.xmbt.manager.BotRegistry;

import javax.annotation.Nonnull;
import java.util.Set;

public class CommandFactory {

    @Nonnull
    public Set<Command> produceAll(@Nonnull BotRegistry botRegistry) {
        return Set.of(
                new ListCommand(botRegistry)
                , new SubscribeCommand(botRegistry)
        );
    }

}
