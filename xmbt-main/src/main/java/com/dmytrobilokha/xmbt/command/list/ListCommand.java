package com.dmytrobilokha.xmbt.command.list;

import com.dmytrobilokha.xmbt.api.messaging.RequestMessage;
import com.dmytrobilokha.xmbt.api.messaging.Response;
import com.dmytrobilokha.xmbt.api.messaging.ResponseMessage;
import com.dmytrobilokha.xmbt.command.Command;
import com.dmytrobilokha.xmbt.manager.BotRegistry;

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
    public void acceptRequest(@Nonnull RequestMessage requestMessage) throws InterruptedException {
        botRegistry.enqueueResponseMessage(new ResponseMessage(requestMessage, Response.OK
                , "Have following bots initialized:" + System.lineSeparator()
                + botRegistry.getBotNames().stream().sorted().collect(Collectors.joining(System.lineSeparator()))));
    }

    @Override
    public void acceptResponse(@Nonnull ResponseMessage responseMessage) throws InterruptedException {
        //This command doesn't send any request, so it doesn't expects any responses
    }

}
