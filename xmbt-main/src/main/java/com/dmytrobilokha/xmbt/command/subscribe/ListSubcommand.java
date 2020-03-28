package com.dmytrobilokha.xmbt.command.subscribe;

import com.dmytrobilokha.xmbt.api.messaging.RequestMessage;
import com.dmytrobilokha.xmbt.api.messaging.Response;
import com.dmytrobilokha.xmbt.api.messaging.ResponseMessage;
import com.dmytrobilokha.xmbt.api.messaging.TextMessage;
import com.dmytrobilokha.xmbt.command.Subcommand;
import com.dmytrobilokha.xmbt.manager.BotRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.Scanner;

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
        var textBuilder = new StringBuilder();
        try {
            int num = 1;
            for (ScheduledMessage subscription : messageDao.fetchSubscriptionsByAddress(userMessage.getAddress())) {
                textBuilder.append(System.lineSeparator())
                        .append(num++)
                        .append(". ")
                        .append(subscription.getDisplayString());
            }
        } catch (SQLException ex) {
            LOG.error("Failed to get subscriptions for request {}", userMessage, ex);
            return "Failed to fetch your subscriptions because of a DB error";
        }
        return textBuilder.length() == 0 ? "You have no subscriptions yet"
                : textBuilder.insert(0, "Your subscriptions: ").toString();
    }

}
