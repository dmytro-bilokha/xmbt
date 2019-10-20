package com.dmytrobilokha.xmbt.command.list;

import com.dmytrobilokha.xmbt.command.Command;
import com.dmytrobilokha.xmbt.manager.BotRegistry;
import com.dmytrobilokha.xmbt.xmpp.TextMessage;

import javax.annotation.Nonnull;
import java.util.stream.Collectors;

public class ListCommand implements Command {

    @Nonnull
    private final BotRegistry botRegistry;

    public ListCommand(@Nonnull BotRegistry botRegistry) {
        this.botRegistry = botRegistry;
    }

    @Override
    @Nonnull
    public String getName() {
        return "list";
    }

    @Override
    public void execute(@Nonnull TextMessage commandMessage) throws InterruptedException {
        botRegistry.enqueueMessageForUser(new TextMessage(commandMessage.getAddress()
                , "Have following bots initialized:" + System.lineSeparator()
                + botRegistry.getBotNames().stream().sorted().collect(Collectors.joining(System.lineSeparator()))));
    }

}
