package com.dmytrobilokha.xmbt.command.subscribe;

import com.dmytrobilokha.xmbt.api.RequestMessage;
import com.dmytrobilokha.xmbt.api.Response;
import com.dmytrobilokha.xmbt.api.ResponseMessage;
import com.dmytrobilokha.xmbt.command.Subcommand;
import com.dmytrobilokha.xmbt.manager.BotRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.Scanner;

import static com.dmytrobilokha.xmbt.command.subscribe.SubscribeCommand.COMMAND_NAME;

class DeleteSubcommand implements Subcommand {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteSubcommand.class);
    private static final String COMMAND_SUBNAME = "delete";
    private static final String USAGE = "Usage: " + System.lineSeparator()
            + COMMAND_NAME  + ' ' + COMMAND_SUBNAME + " subscription_number";

    @Nonnull
    private final BotRegistry botRegistry;
    @Nonnull
    private final ScheduledMessageDao messageDao;

    DeleteSubcommand(@Nonnull BotRegistry botRegistry, @Nonnull ScheduledMessageDao messageDao) {
        this.botRegistry = botRegistry;
        this.messageDao = messageDao;
    }

    @Nonnull
    @Override
    public String getName() {
        return COMMAND_SUBNAME;
    }

    @Override
    public void execute(@Nonnull Scanner commandMessageScanner
            , @Nonnull RequestMessage requestMessage) throws InterruptedException {
        if (!commandMessageScanner.hasNext()) {
            sendResponse(requestMessage
                    , Response.INVALID_COMMAND
                    , "Missing mandatory subscription_number parameter. " + USAGE);
            return;
        }
        var subscriptionNumberString = commandMessageScanner.next();
        int subscriptionNumber;
        try {
            subscriptionNumber = Integer.parseInt(subscriptionNumberString);
        } catch (NumberFormatException ex) {
            sendResponse(requestMessage
                    , Response.INVALID_COMMAND
                    , "Failed to parse '" + subscriptionNumberString + "' to subscription_number. " + USAGE);
            return;
        }
        if (subscriptionNumber <= 0) {
            sendResponse(requestMessage
                    , Response.INVALID_COMMAND
                    , "Subscription_number must be a positive value - order of the "
                            + "subscription in the list, but got '" + subscriptionNumber + '\'');
            return;
        }
        int deleted = -1;
        try {
            deleted = messageDao
                    .deleteSubscriptionByAddressAndOrder(
                            requestMessage.getTextMessage().getAddress(), subscriptionNumber);
        } catch (SQLException ex) {
            sendResponse(requestMessage, Response.INTERNAL_ERROR, "Failed to delete subscription"
                    + " because of internal error");
            LOG.error("Failed to delete subscription for '" + requestMessage + "'", ex);
            return;
        }
        if (deleted == 0) {
            sendResponse(requestMessage, Response.INVALID_COMMAND, "The subscription hasn't been"
                    + " deleted. Check if provided subscription_number=" + subscriptionNumber
                    + " is correct.");
        } else {
            sendResponse(requestMessage, Response.OK, "The subscription has been deleted");
        }
    }

    private void sendResponse(
            @Nonnull RequestMessage requestMessage
            , @Nonnull Response response
            , @Nonnull String text
    ) throws InterruptedException {
        botRegistry.enqueueResponseMessage(new ResponseMessage(requestMessage, response, text));
    }

}

