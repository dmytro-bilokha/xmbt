package com.dmytrobilokha.xmbt.manager;

import com.dmytrobilokha.xmbt.xmpp.TextMessage;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class MessageTimer {

    @Nonnull
    private static final TextMessage emptyMessage = new TextMessage("", "");

    @Nonnull
    private ScheduledMessage earliestMessage;
    @Nonnull
    private NavigableSet<ScheduledMessage> scheduledMessages;

    public MessageTimer() {
        this.earliestMessage = new ScheduledMessage(LocalDateTime.now(), emptyMessage);
        this.scheduledMessages = new TreeSet<>();
    }

    void scheduleMessage(@Nonnull Duration duration, @Nonnull TextMessage message) {
        scheduledMessages.add(new ScheduledMessage(LocalDateTime.now().plus(duration), message));
    }

    @Nonnull
    List<TextMessage> tick(long milliseconds) throws InterruptedException {
        Thread.sleep(milliseconds);
        var jitMessage = new ScheduledMessage(LocalDateTime.now(), emptyMessage);
        var timedMessages = new HashSet<>(scheduledMessages.subSet(earliestMessage, jitMessage));
        var result = timedMessages.stream().map(sm -> sm.message).collect(Collectors.toList());
        scheduledMessages.removeAll(timedMessages);
        return result;
    }

    private static class ScheduledMessage implements Comparable<ScheduledMessage> {
        @Nonnull
        private final LocalDateTime dateTime;
        @Nonnull
        private final TextMessage message;

        private ScheduledMessage(@Nonnull LocalDateTime dateTime, @Nonnull TextMessage message) {
            this.dateTime = dateTime;
            this.message = message;
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

}
