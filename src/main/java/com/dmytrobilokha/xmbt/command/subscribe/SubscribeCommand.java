package com.dmytrobilokha.xmbt.command.subscribe;

import com.dmytrobilokha.xmbt.api.RequestMessage;
import com.dmytrobilokha.xmbt.api.Response;
import com.dmytrobilokha.xmbt.api.ResponseMessage;
import com.dmytrobilokha.xmbt.command.Command;
import com.dmytrobilokha.xmbt.command.InvalidUserInputException;
import com.dmytrobilokha.xmbt.command.Subcommand;
import com.dmytrobilokha.xmbt.manager.BotRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SubscribeCommand implements Command {

    static final String COMMAND_NAME = "sub";

    private static final Logger LOG = LoggerFactory.getLogger(SubscribeCommand.class);
    private static final long CHECK_PAUSE_SECONDS = 30;

    @Nonnull
    private final BotRegistry botRegistry;
    @Nonnull
    private final ScheduledMessageDao messageDao;
    @Nonnull
    private final Map<String, Subcommand> subcommands;
    @CheckForNull
    private LocalDateTime lastScheduleCheck;

    public SubscribeCommand(@Nonnull BotRegistry botRegistry, @Nonnull ScheduledMessageDao messageDao) {
        this.botRegistry = botRegistry;
        this.messageDao = messageDao;
        this.subcommands = Stream
                .of(new ListSubcommand(botRegistry, messageDao)
                    , new AddSubcommand(botRegistry, messageDao))
                .collect(Collectors.toUnmodifiableMap(Subcommand::getName, subcommand -> subcommand));
    }

    @Nonnull
    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Override
    public void acceptRequest(@Nonnull RequestMessage requestMessage) throws InterruptedException {
        var commandMessageScanner = new Scanner(requestMessage.getTextMessage().getText());
        try {
            validateCommandName(commandMessageScanner);
            if (!commandMessageScanner.hasNext()) {
                sendHelpResponse(requestMessage);
                return;
            }
            var subcommand = subcommands.get(commandMessageScanner.next());
            if (subcommand == null) {
                sendHelpResponse(requestMessage);
            } else {
                subcommand.execute(commandMessageScanner, requestMessage);
            }
        } catch (InvalidUserInputException ex) {
            botRegistry.enqueueResponseMessage(
                    new ResponseMessage(requestMessage, Response.INVALID_COMMAND, ex.getMessage()));
        }
    }

    private void sendHelpResponse(@Nonnull RequestMessage requestMessage) throws InterruptedException {
        botRegistry.enqueueResponseMessage(new ResponseMessage(
                requestMessage
                , Response.OK
                , getHelpMessage()
        ));
    }

    @Nonnull
    private String getHelpMessage() {
        return "Usage: " + System.lineSeparator()
                + COMMAND_NAME + ' '
                + subcommands.keySet().stream().sorted().collect(Collectors.joining(" | "))
                + "...";
    }

    @Override
    public void acceptResponse(@Nonnull ResponseMessage responseMessage) throws InterruptedException {
        for (Subcommand subcommand : subcommands.values()) {
            subcommand.acceptResponse(responseMessage);
        }
    }

    @Override
    public void tick() throws InterruptedException {
        var now = LocalDateTime.now();
        if (lastScheduleCheck != null && lastScheduleCheck.plusSeconds(CHECK_PAUSE_SECONDS).isAfter(now)) {
            return;
        }
        lastScheduleCheck = now;
        List<ScheduledMessage> timedMessages;
        try {
            timedMessages = messageDao.fetchScheduledMessagesBefore(now);
        } catch (SQLException ex) {
            LOG.error("Failed to fetch timed-out scheduled messages", ex);
            return;
        }
        for (ScheduledMessage timedMessage : timedMessages) {
            botRegistry.enqueueRequestMessage(timedMessage.getRequestMessage());
            ScheduledMessage nextSchedule = timedMessage.getNext();
            if (nextSchedule == null) {
                try {
                    messageDao.delete(timedMessage);
                } catch (SQLException ex) {
                    LOG.error("Failed to delete timed out {}", timedMessage, ex);
                    return;
                }
            } else {
                try {
                    messageDao.updateDateTime(timedMessage, nextSchedule.getDateTime());
                } catch (SQLException ex) {
                    LOG.error("Failed to set next datetime {} for message {}"
                            , nextSchedule.getDateTime(), timedMessage, ex);
                    return;
                }
            }
        }
    }

    private void validateCommandName(@Nonnull Scanner commandScanner) throws InvalidUserInputException {
        if (!commandScanner.hasNext()) {
            throw new InvalidUserInputException("Unrecognizable subscribe command");
        }
        String commandName = commandScanner.next();
        if (!getName().equals(commandName)) {
            throw new InvalidUserInputException("Unrecognizable subscribe command '" + commandName + '\'');
        }
    }

}

