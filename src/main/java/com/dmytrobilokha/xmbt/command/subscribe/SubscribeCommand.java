package com.dmytrobilokha.xmbt.command.subscribe;

import com.dmytrobilokha.xmbt.api.Persistable;
import com.dmytrobilokha.xmbt.api.Request;
import com.dmytrobilokha.xmbt.api.RequestMessage;
import com.dmytrobilokha.xmbt.api.Response;
import com.dmytrobilokha.xmbt.api.ResponseMessage;
import com.dmytrobilokha.xmbt.api.TextMessage;
import com.dmytrobilokha.xmbt.command.Command;
import com.dmytrobilokha.xmbt.dictionary.FuzzyDictionary;
import com.dmytrobilokha.xmbt.manager.BotManager;
import com.dmytrobilokha.xmbt.manager.BotRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Scanner;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SubscribeCommand implements Command, Persistable {

    private static final Logger LOG = LoggerFactory.getLogger(SubscribeCommand.class);
    private static final String COMMAND_NAME = "subscribe";
    private static final String USAGE = "Usage: " + System.lineSeparator()
            + COMMAND_NAME + " HH:mm [space_separated_days_of_week] "
            + BotManager.BOT_NAME_PREFIX + "botname [message_to_bot...]";
    private static final RequestMessage EMPTY_MESSAGE = new RequestMessage(
            0, "", "", Request.RESPOND, new TextMessage("", ""));
    private static final int ONE = 1;
    private static final Pattern READ_LEAST_OF_SCANNER_PATTERN = Pattern.compile("\\A");

    @Nonnull
    private final BotRegistry botRegistry;
    @Nonnull
    private final ScheduledMessage earliestMessage;
    @Nonnull
    private final Map<Long, ScheduledMessage> validationWaitingMessagesById;
    @Nonnull
    private final NavigableSet<ScheduledMessage> scheduledMessages;
    @Nonnull
    private final FuzzyDictionary<DayOfWeek> dayOfWeekDictionary;

    public SubscribeCommand(BotRegistry botRegistry) {
        this.botRegistry = botRegistry;
        this.scheduledMessages = new TreeSet<>();
        this.validationWaitingMessagesById = new HashMap<>();
        this.earliestMessage = createEmptyJitMessage();
        this.dayOfWeekDictionary = FuzzyDictionary.withLatinLetters();
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            dayOfWeekDictionary.put(dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH), dayOfWeek);
        }
    }

    @Nonnull
    private ScheduledMessage createEmptyJitMessage() {
        return new ScheduledMessage(
                LocalDateTime.now()
                , new Schedule(LocalTime.MIDNIGHT, EnumSet.noneOf(DayOfWeek.class))
                , EMPTY_MESSAGE
        );
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
            LocalTime scheduleTime = parseScheduleTime(commandMessageScanner); //NOPMD
            if (!commandMessageScanner.hasNext()) {
                throw new InvalidUserInputException("Missing mandatory botname time parameter. " + USAGE);
            }
            var daysOfWeek = EnumSet.noneOf(DayOfWeek.class);
            String botName = "";
            while (commandMessageScanner.hasNext()) {
                var nextToken = commandMessageScanner.next();
                if (nextToken.charAt(0) == BotManager.BOT_NAME_PREFIX) {
                    botName = nextToken;
                    break;
                }
                DayOfWeek dayOfWeek = parseDayOfWeek(nextToken);
                daysOfWeek.add(dayOfWeek);
            }
            var stagingScheduleMessage = createScheduledMessage(
                    requestMessage
                    , botName
                    , scheduleTime
                    , daysOfWeek
                    , commandMessageScanner
            );
            var validationRequestMessage = new RequestMessage(
                    stagingScheduleMessage.getRequestMessage().getId()
                    , getName()
                    , stagingScheduleMessage.getRequestMessage().getReceiver()
                    , Request.VALIDATE
                    , stagingScheduleMessage.getRequestMessage().getTextMessage()
            );
            if (!botRegistry.enqueueRequestMessage(validationRequestMessage)) {
                throw new InvalidUserInputException("Bot with name '" + botName + "' doesn't exist");
            }
            validationWaitingMessagesById.put(requestMessage.getId(), stagingScheduleMessage);
        } catch (InvalidUserInputException ex) {
            botRegistry.enqueueResponseMessage(
                    new ResponseMessage(requestMessage, Response.INVALID_COMMAND, ex.getMessage()));
        }
    }

    @Override
    public void acceptResponse(@Nonnull ResponseMessage responseMessage) throws InterruptedException {
        var toBeValidatedScheduledMessage = validationWaitingMessagesById.get(responseMessage.getId());
        if (toBeValidatedScheduledMessage == null) {
            LOG.error("Got response non matching any request {}", responseMessage);
            return;
        }
        if (responseMessage.getResponse() == Response.OK) {
            scheduledMessages.add(toBeValidatedScheduledMessage);
            validationWaitingMessagesById.remove(responseMessage.getId());
            botRegistry.enqueueResponseMessage(new ResponseMessage(
                    toBeValidatedScheduledMessage.getRequestMessage(), Response.OK, "You have been subscribed"));
            return;
        }
        botRegistry.enqueueResponseMessage(new ResponseMessage(
                toBeValidatedScheduledMessage.getRequestMessage()
                , responseMessage.getResponse()
                , responseMessage.getTextMessage().getText()));
    }

    @Override
    public void tick() throws InterruptedException {
        var jitMessage = createEmptyJitMessage();
        var timedMessages = new HashSet<>(scheduledMessages.subSet(earliestMessage, jitMessage));
        for (ScheduledMessage timedMessage : timedMessages) {
            botRegistry.enqueueRequestMessage(timedMessage.getRequestMessage());
            scheduledMessages.remove(timedMessage);
            ScheduledMessage nextSchedule = timedMessage.getNext();
            if (nextSchedule != null) {
                scheduledMessages.add(nextSchedule);
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

    @Nonnull
    private LocalTime parseScheduleTime(@Nonnull Scanner commandScanner) throws InvalidUserInputException {
        if (!commandScanner.hasNext()) {
            throw new InvalidUserInputException("Missing mandatory schedule time parameter. " + USAGE);
        }
        var scheduleTimeString = commandScanner.next();
        LocalTime scheduleTime;
        try {
            scheduleTime = LocalTime.parse(scheduleTimeString, DateTimeFormatter.ISO_LOCAL_TIME);
        } catch (DateTimeParseException ex) {
            throw new InvalidUserInputException("Unable to parse time from string '" + scheduleTimeString + '\'', ex);
        }
        return scheduleTime;
    }

    @Nonnull
    private DayOfWeek parseDayOfWeek(@Nonnull String token) throws InvalidUserInputException {
        List<DayOfWeek> matchingDays = dayOfWeekDictionary.get(token);
        if (matchingDays.isEmpty()) {
            throw new InvalidUserInputException("Failed to convert '" + token + "' to day of week. " + USAGE);
        }
        if (matchingDays.size() > ONE) {
            throw new InvalidUserInputException(
                    "Found following days of week coresponding to '" + token + "': "
                    + matchingDays.stream()
                    .map(day -> day.getDisplayName(TextStyle.FULL, Locale.ENGLISH))
                    .collect(Collectors.joining(" "))
                    + ". Try to specify more characters");
        }
        return matchingDays.get(0);
    }

    private ScheduledMessage createScheduledMessage(
            @Nonnull RequestMessage requestMessage
            , @Nonnull String botName
            , @Nonnull LocalTime scheduleTime
            , @Nonnull EnumSet<DayOfWeek> daysOfWeek
            , @Nonnull Scanner contentScanner
    ) throws InvalidUserInputException {
        if (botName.isEmpty()) {
            throw new InvalidUserInputException("Missing mandatory botname parameter. " + USAGE);
        }
        Schedule messageSchedule = new Schedule(scheduleTime, daysOfWeek);
        LocalDateTime nextDateTime = messageSchedule.getNext();
        if (nextDateTime == null) {
            throw new InvalidUserInputException("Cannot schedule in past");
        }
        String scheduleMessageText = contentScanner.hasNext()
                ? contentScanner.useDelimiter(READ_LEAST_OF_SCANNER_PATTERN).next() : "";
        var rewrittenRequest = new RequestMessage(
                requestMessage.getId()
                , requestMessage.getSender()
                , botName
                , requestMessage.getRequest()
                , new TextMessage(requestMessage.getTextMessage().getAddress(), scheduleMessageText)
        );
        return new ScheduledMessage(
                nextDateTime
                , messageSchedule
                , rewrittenRequest
        );
    }

    //TODO: add boolean return flag to remove file on fail, make it thread-safe
    @Override
    public void save(@Nonnull BufferedOutputStream outputStream) {
        var messages = new ArrayList<>(scheduledMessages);
        try (ObjectOutputStream objectStream = new ObjectOutputStream(outputStream)) {
            objectStream.writeObject(messages);
        } catch (IOException ex) {
            LOG.error("Failed to persist the state", ex);
        }
    }

    @Override
    public void load(@Nonnull BufferedInputStream inputStream) {
        List<ScheduledMessage> messages;
        try (ObjectInputStream objectStream = new ObjectInputStream(inputStream)) {
            messages = (List<ScheduledMessage>) objectStream.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            LOG.error("Failed to load the state", ex);
            return;
        }
        //TODO: handle outdated messages (reschedule recursives)
        scheduledMessages.addAll(messages);
    }

    @Nonnull
    @Override
    public String getPersistenceKey() {
        return "Subscriptions";
    }

    private static class InvalidUserInputException extends Exception {
        private InvalidUserInputException(@Nonnull String message) {
            super(message);
        }

        private InvalidUserInputException(@Nonnull String message, @Nonnull Exception ex) {
            super(message, ex);
        }
    }

}

