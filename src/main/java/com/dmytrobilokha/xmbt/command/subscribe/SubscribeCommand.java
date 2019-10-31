package com.dmytrobilokha.xmbt.command.subscribe;

import com.dmytrobilokha.xmbt.api.RequestMessage;
import com.dmytrobilokha.xmbt.api.Response;
import com.dmytrobilokha.xmbt.api.ResponseMessage;
import com.dmytrobilokha.xmbt.api.TextMessage;
import com.dmytrobilokha.xmbt.command.Command;
import com.dmytrobilokha.xmbt.dictionary.FuzzyDictionary;
import com.dmytrobilokha.xmbt.manager.BotManager;
import com.dmytrobilokha.xmbt.manager.BotRegistry;

import javax.annotation.Nonnull;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.NavigableSet;
import java.util.Scanner;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SubscribeCommand implements Command {

    private static final String COMMAND_NAME = "subscribe";
    private static final String USAGE = "Usage: " + System.lineSeparator()
            + COMMAND_NAME + " HH:mm [space_separated_days_of_week] "
            + BotManager.BOT_NAME_PREFIX + "botname [message_to_bot...]";
    private static final TextMessage EMPTY_MESSAGE = new TextMessage("", "");
    private static final int ONE = 1;
    private static final Pattern READ_LEAST_OF_SCANNER_PATTERN = Pattern.compile("\\A");

    @Nonnull
    private final BotRegistry botRegistry;
    @Nonnull
    private final ScheduledMessage earliestMessage;
    @Nonnull
    private final NavigableSet<ScheduledMessage> scheduledMessages;
    @Nonnull
    private final FuzzyDictionary<DayOfWeek> dayOfWeekDictionary;

    public SubscribeCommand(BotRegistry botRegistry) {
        this.botRegistry = botRegistry;
        this.scheduledMessages = new TreeSet<>();
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
            scheduleMessage(requestMessage.getTextMessage().getAddress()
                    , botName, scheduleTime, daysOfWeek, commandMessageScanner);
            botRegistry.enqueueResponseMessage(
                    new ResponseMessage(requestMessage, Response.OK, "You have been subscribed"));
        } catch (InvalidUserInputException ex) {
            botRegistry.enqueueResponseMessage(
                    new ResponseMessage(requestMessage, Response.INVALID_COMMAND, ex.getMessage()));
        }
    }

    @Override
    public void acceptResponse(@Nonnull ResponseMessage responseMessage) throws InterruptedException {
        //TODO: implement validation response handling here
    }

    @Override
    public void tick() throws InterruptedException {
        var jitMessage = createEmptyJitMessage();
        var timedMessages = new HashSet<>(scheduledMessages.subSet(earliestMessage, jitMessage));
        for (ScheduledMessage timedMessage : timedMessages) {
            botRegistry.enqueueMessageFromUser(timedMessage.getMessage());
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

    private void scheduleMessage(
            @Nonnull String userAddress
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
        scheduledMessages.add(new ScheduledMessage(
                nextDateTime
                , messageSchedule
                , new TextMessage(userAddress, botName + scheduleMessageText)));
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

