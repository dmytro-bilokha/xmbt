package com.dmytrobilokha.xmbt.manager;

import com.dmytrobilokha.xmbt.dictionary.FuzzyDictionary;
import com.dmytrobilokha.xmbt.xmpp.TextMessage;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.time.DayOfWeek;
import java.time.LocalDate;
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
import java.util.Objects;
import java.util.Scanner;
import java.util.TreeSet;
import java.util.stream.Collectors;

//TODO: refactor, implement validation of message by bots without actual executing/answering to it
public class MessageTimer {

    private static final String USAGE = "Usage: " + System.lineSeparator()
            + "schedule HH:mm [space_separated_days_of_week] "
            + BotManager.BOT_NAME_PREFIX + "botname [message_to_bot...]";
    private static final TextMessage EMPTY_MESSAGE = new TextMessage("", "");

    @Nonnull
    private final ScheduledMessage earliestMessage;
    @Nonnull
    private final NavigableSet<ScheduledMessage> scheduledMessages;
    @Nonnull
    private final FuzzyDictionary<DayOfWeek> dayOfWeekDictionary;

    public MessageTimer() {
        this.earliestMessage = createEmptyJitMessage();
        this.scheduledMessages = new TreeSet<>();
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
    TextMessage scheduleMessage(@Nonnull TextMessage commandMessage) {
        var commandMessageScanner = new Scanner(commandMessage.getText());
        if (!commandMessageScanner.hasNext() || !"schedule".equals(commandMessageScanner.next())) {
            return commandMessage.withNewText("Unrecognizable schedule command");
        }
        if (!commandMessageScanner.hasNext()) {
            return commandMessage.withNewText("Missing mandatory schedule time parameter. " + USAGE);
        }
        var scheduleTimeString = commandMessageScanner.next();
        LocalTime scheduleTime;
        try {
            scheduleTime = LocalTime.parse(scheduleTimeString, DateTimeFormatter.ISO_LOCAL_TIME);
        } catch (DateTimeParseException ex) {
            return commandMessage.withNewText("Unable to parse time from string '" + scheduleTimeString + '\'');
        }
        if (!commandMessageScanner.hasNext()) {
            return commandMessage.withNewText("Missing mandatory botname time parameter. " + USAGE);
        }
        var daysOfWeek = EnumSet.noneOf(DayOfWeek.class);
        String botname = "";
        while (commandMessageScanner.hasNext()) {
            var nextToken = commandMessageScanner.next();
            if (nextToken.isEmpty()) {
                continue;
            }
            if (nextToken.charAt(0) == BotManager.BOT_NAME_PREFIX) {
                botname = nextToken;
                break;
            }
            List<DayOfWeek> matchingDays = dayOfWeekDictionary.get(nextToken);
            if (matchingDays.isEmpty()) {
                return commandMessage.withNewText("Failed to convert '" + nextToken + "' to day of week. " + USAGE);
            }
            if (matchingDays.size() > 1) {
                return commandMessage.withNewText("Found following days of week coresponding to '" + nextToken + "': "
                    + matchingDays.stream()
                        .map(day -> day.getDisplayName(TextStyle.FULL, Locale.ENGLISH))
                        .collect(Collectors.joining(" "))
                    + ". Try to specify more characters");
            }
            daysOfWeek.add(matchingDays.get(0));
        }
        if (botname.isEmpty()) {
            return commandMessage.withNewText("Missing mandatory botname time parameter. " + USAGE);
        }
        Schedule messageSchedule = new Schedule(scheduleTime, daysOfWeek);
        LocalDateTime nextDateTime = messageSchedule.getNext();
        if (nextDateTime == null) {
            return commandMessage.withNewText("Cannot schedule in past. " + USAGE);
        }
        String scheduleMessageText = commandMessageScanner.hasNext()
                ? commandMessageScanner.useDelimiter("\\A").next() : "";
        scheduledMessages.add(new ScheduledMessage(
                nextDateTime
                , messageSchedule
                , commandMessage.withNewText(botname + scheduleMessageText)));
        return commandMessage.withNewText("Your message has been scheduled");
    }

    @Nonnull
    List<TextMessage> tick(long milliseconds) throws InterruptedException {
        Thread.sleep(milliseconds);
        var jitMessage = new ScheduledMessage(
                LocalDateTime.now()
                , new Schedule(LocalTime.MIDNIGHT, EnumSet.noneOf(DayOfWeek.class))
                , EMPTY_MESSAGE
        );
        var timedMessages = new HashSet<>(scheduledMessages.subSet(earliestMessage, jitMessage));
        var result = timedMessages.stream().map(sm -> sm.message).collect(Collectors.toList());
        scheduledMessages.removeAll(timedMessages);
        return result;
    }

    private static class ScheduledMessage implements Comparable<ScheduledMessage> {
        @Nonnull
        private final Schedule schedule;
        @Nonnull
        private final LocalDateTime dateTime;
        @Nonnull
        private final TextMessage message;

        private ScheduledMessage(
                @Nonnull LocalDateTime dateTime
                , @Nonnull Schedule schedule
                , @Nonnull TextMessage message
        ) {
            this.dateTime = dateTime;
            this.schedule = schedule;
            this.message = message;
        }

        @CheckForNull
        private ScheduledMessage getNext() {
            LocalDateTime nextDateTime = schedule.getNext();
            if (nextDateTime == null) {
                return null;
            }
            return new ScheduledMessage(nextDateTime, schedule, message);
        }

        @Override
        public int compareTo(ScheduledMessage o) {
            int cmp = dateTime.compareTo(o.dateTime);
            if (cmp != 0) {
                return cmp;
            }
            cmp = message.getAddress().compareTo(o.message.getAddress());
            if (cmp != 0) {
                return cmp;
            }
            return message.getText().compareTo(o.message.getText());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ScheduledMessage that = (ScheduledMessage) o;
            return dateTime.equals(that.dateTime)
                    && message.equals(that.message);
        }

        @Override
        public int hashCode() {
            return Objects.hash(dateTime);
        }
    }

    private static class Schedule {
        @Nonnull
        private final LocalTime time;
        @Nonnull
        private final EnumSet<DayOfWeek> dayOfWeeks;

        Schedule(@Nonnull LocalTime time, @Nonnull EnumSet<DayOfWeek> dayOfWeeks) {
            this.time = time;
            this.dayOfWeeks = dayOfWeeks;
        }

        //TODO: change approach to handle daylight saving time shifts, server clock adjustments
        @CheckForNull
        LocalDateTime getNext() {
            LocalDateTime now = LocalDateTime.now();
            LocalDate today = now.toLocalDate();
            LocalDateTime todaySchedule = LocalDateTime.of(today, time);
            if (dayOfWeeks.isEmpty()) {
                if (todaySchedule.isBefore(now)) {
                    return null;
                }
                return todaySchedule;
            }
            if (dayOfWeeks.contains(today.getDayOfWeek()) && now.isBefore(todaySchedule)) {
                return todaySchedule;
            }
            for (int i = 1; i < 6; i++) {
                LocalDate incomingDay = today.plusDays(i);
                if (dayOfWeeks.contains(incomingDay.getDayOfWeek())) {
                    return LocalDateTime.of(incomingDay, time);
                }
            }
            return null;
        }

    }

}
