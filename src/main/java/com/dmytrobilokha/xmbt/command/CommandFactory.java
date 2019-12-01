package com.dmytrobilokha.xmbt.command;

import com.dmytrobilokha.xmbt.command.list.ListCommand;
import com.dmytrobilokha.xmbt.command.subscribe.ScheduledMessageDao;
import com.dmytrobilokha.xmbt.command.subscribe.SubscribeCommand;
import com.dmytrobilokha.xmbt.manager.BotRegistry;
import com.dmytrobilokha.xmbt.persistence.PersistenceService;

import javax.annotation.Nonnull;
import java.util.Set;

public class CommandFactory {

    @Nonnull
    private final PersistenceService persistenceService;

    public CommandFactory(@Nonnull PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    @Nonnull
    public Set<Command> produceAll(@Nonnull BotRegistry botRegistry) {
        return Set.of(
                new ListCommand(botRegistry)
                , new SubscribeCommand(botRegistry, new ScheduledMessageDao(persistenceService))
        );
    }

}
