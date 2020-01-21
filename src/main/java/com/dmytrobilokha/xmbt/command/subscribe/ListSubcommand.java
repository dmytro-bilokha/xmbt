package com.dmytrobilokha.xmbt.command.subscribe;

import com.dmytrobilokha.xmbt.api.RequestMessage;
import com.dmytrobilokha.xmbt.api.Response;
import com.dmytrobilokha.xmbt.api.ResponseMessage;
import com.dmytrobilokha.xmbt.api.TextMessage;
import com.dmytrobilokha.xmbt.command.Subcommand;
import com.dmytrobilokha.xmbt.manager.BotRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.stream.Collectors;

class ListSubcommand implements Subcommand {

    private static final Logger LOG = LoggerFactory.getLogger(ListSubcommand.class);

    @Nonnull
    private final BotRegistry botRegistry;
    @Nonnull
    private final ScheduledMessageDao messageDao;

    ListSubcommand(@Nonnull BotRegistry botRegistry, @Nonnull ScheduledMessageDao messageDao) {
        this.botRegistry = botRegistry;
        this.messageDao = messageDao;
    }

    @Nonnull
    @Override
    public String getName() {
        return "list";
    }

    @Override
    public void execute(@Nonnull Scanner commandMessageScanner
            , @Nonnull RequestMessage requestMessage) throws InterruptedException {
        botRegistry.enqueueResponseMessage(new ResponseMessage(
                requestMessage
                , Response.OK
                , getUserSubscriptionsText(requestMessage.getTextMessage())
        ));
    }

    @Nonnull
    private String getUserSubscriptionsText(@Nonnull TextMessage userMessage) {
        String subscriptions;
        try {
            subscriptions = messageDao.fetchSubscriptionsByAddress(userMessage.getAddress())
                    .stream()
                    .map(ScheduledMessage::getDisplayString)
                    .collect(Collectors.joining(System.lineSeparator()));
        } catch (SQLException ex) {
            LOG.error("Failed to get subscriptions for request {}", userMessage, ex);
            return "Failed to fetch your subscriptions because of a DB error";
        }
        var mainMessage = subscriptions.isEmpty() ? "You have no subscriptions yet"
                : "Your subscriptions: " + System.lineSeparator() + subscriptions;
        return mainMessage + System.lineSeparator();
    }

}
