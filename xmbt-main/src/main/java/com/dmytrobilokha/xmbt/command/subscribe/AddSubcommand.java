package com.dmytrobilokha.xmbt.command.subscribe;

import com.dmytrobilokha.xmbt.api.messaging.Request;
import com.dmytrobilokha.xmbt.api.messaging.RequestMessage;
import com.dmytrobilokha.xmbt.api.messaging.Response;
import com.dmytrobilokha.xmbt.api.messaging.ResponseMessage;
import com.dmytrobilokha.xmbt.api.messaging.TextMessage;
import com.dmytrobilokha.xmbt.command.InvalidUserInputException;
import com.dmytrobilokha.xmbt.command.Subcommand;
import com.dmytrobilokha.xmbt.dictionary.FuzzyDictionary;
import com.dmytrobilokha.xmbt.manager.BotManager;
import com.dmytrobilokha.xmbt.manager.BotRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.dmytrobilokha.xmbt.command.subscribe.SubscribeCommand.COMMAND_NAME;

class AddSubcommand implements Subcommand {

    private static final Logger LOG = LoggerFactory.getLogger(AddSubcommand.class);
    private static final String COMMAND_SUBNAME = "add";
    private static final String USAGE = "Usage: " + System.lineSeparator()
            + COMMAND_NAME  + ' ' + COMMAND_SUBNAME + " HH:mm [space_separated_days_of_week] "
            + BotManager.BOT_NAME_PREFIX + "botname [message_to_bot...]";
    private static final int ONE = 1;
    private static final Pattern READ_LEAST_OF_SCANNER_PATTERN = Pattern.compile("\\A");

    @Nonnull
    private final BotRegistry botRegistry;
    @Nonnull
    private final ScheduledMessageDao messageDao;
    @Nonnull
    private final Map<Long, ScheduledMessage> validationWaitingMessagesById;
    @Nonnull
    private final FuzzyDictionary<DayOfWeek> dayOfWeekDictionary;

    AddSubcommand(@Nonnull BotRegistry botRegistry, @Nonnull ScheduledMessageDao messageDao) {
        this.botRegistry = botRegistry;
        this.messageDao = messageDao;
        this.validationWaitingMessagesById = new HashMap<>();
        this.dayOfWeekDictionary = FuzzyDictionary.withLatinLetters();
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            dayOfWeekDictionary.put(dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH), dayOfWeek);
        }
    }

    @Nonnull
    @Override
    public String getName() {
        return COMMAND_SUBNAME;
    }

    @Override
    public void execute(
            @Nonnull Scanner commandMessageScanner
            , @Nonnull RequestMessage requestMessage
    ) throws InterruptedException {
        try {
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
                    , COMMAND_NAME
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

    @Override
    public void acceptResponse(@Nonnull ResponseMessage responseMessage) throws InterruptedException {
        var toBeValidatedScheduledMessage = validationWaitingMessagesById.get(responseMessage.getId());
        if (toBeValidatedScheduledMessage == null) {
            LOG.error("Got response non matching any request {}", responseMessage);
            return;
        }
        if (responseMessage.getResponse() == Response.OK) {
            try {
                messageDao.insert(toBeValidatedScheduledMessage);
            } catch (SQLException ex) {
                LOG.error("Failed to persist schedule request {}", toBeValidatedScheduledMessage, ex);
                botRegistry.enqueueResponseMessage(new ResponseMessage(
                        toBeValidatedScheduledMessage.getRequestMessage()
                        , Response.INTERNAL_ERROR
                        , "Failed to subscribe you because of a DB issue"
                ));
                return;
            }
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

}
