package com.dmytrobilokha.xmbt.command.subscribe;

import com.dmytrobilokha.xmbt.api.TextMessage;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.util.Objects;

public class ScheduledMessage implements Comparable<ScheduledMessage> {

    @Nonnull
    private final Schedule schedule;
    @Nonnull
    private final LocalDateTime dateTime;
    @Nonnull
    private final TextMessage message;

    ScheduledMessage(
            @Nonnull LocalDateTime dateTime
            , @Nonnull Schedule schedule
            , @Nonnull TextMessage message
    ) {
        this.dateTime = dateTime;
        this.schedule = schedule;
        this.message = message;
    }

    @CheckForNull
    ScheduledMessage getNext() {
        LocalDateTime nextDateTime = schedule.getNext();
        if (nextDateTime == null) {
            return null;
        }
        return new ScheduledMessage(nextDateTime, schedule, message);
    }

    @Nonnull
    TextMessage getMessage() {
        return message;
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
