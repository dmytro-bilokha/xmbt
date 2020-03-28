package com.dmytrobilokha.xmbt.command;

import com.dmytrobilokha.xmbt.api.service.dictionary.FuzzyDictionaryFactory;
import com.dmytrobilokha.xmbt.api.service.persistence.PersistenceService;
import com.dmytrobilokha.xmbt.command.list.ListCommand;
import com.dmytrobilokha.xmbt.command.subscribe.ScheduledMessageDao;
import com.dmytrobilokha.xmbt.command.subscribe.SubscribeCommand;
import com.dmytrobilokha.xmbt.manager.BotRegistry;

import javax.annotation.Nonnull;
import java.util.Set;

public class CommandFactory {

    @Nonnull
    private final PersistenceService persistenceService;
    @Nonnull
    private final FuzzyDictionaryFactory dictionaryFactory;

    public CommandFactory(
            @Nonnull PersistenceService persistenceService
            , @Nonnull FuzzyDictionaryFactory dictionaryFactory
    ) {
        this.persistenceService = persistenceService;
        this.dictionaryFactory = dictionaryFactory;
    }

    @Nonnull
    public Set<Command> produceAll(@Nonnull BotRegistry botRegistry) {
        return Set.of(
                new ListCommand(botRegistry)
                , new SubscribeCommand(botRegistry, new ScheduledMessageDao(persistenceService), dictionaryFactory)
        );
    }

}
