package com.dmytrobilokha.xmbt.command.subscribe;

import com.dmytrobilokha.xmbt.api.RequestMessage;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class ScheduledMessage implements Comparable<ScheduledMessage>, Serializable {

    @Nonnull
    private final Schedule schedule;
    @Nonnull
    private final LocalDateTime dateTime;
    @Nonnull
    private final RequestMessage requestMessage;

    ScheduledMessage(
            @Nonnull LocalDateTime dateTime
            , @Nonnull Schedule schedule
            , @Nonnull RequestMessage requestMessage
    ) {
        this.dateTime = dateTime;
        this.schedule = schedule;
        this.requestMessage = requestMessage;
    }

    @CheckForNull
    ScheduledMessage getNext() {
        LocalDateTime nextDateTime = schedule.getNext();
        if (nextDateTime == null) {
            return null;
        }
        return new ScheduledMessage(nextDateTime, schedule, requestMessage);
    }

    @Nonnull
    RequestMessage getRequestMessage() {
        return requestMessage;
    }

    @Nonnull
    String getDisplayString() {
        return schedule.getDisplayString()
                + " " + requestMessage.getReceiver()
                + requestMessage.getTextMessage().getText();
    }

    @Override
    public int compareTo(ScheduledMessage o) {
        int cmp = dateTime.compareTo(o.dateTime);
        if (cmp != 0) {
            return cmp;
        }
        cmp = requestMessage.getTextMessage().getAddress().compareTo(o.requestMessage.getTextMessage().getAddress());
        if (cmp != 0) {
            return cmp;
        }
        return requestMessage.getTextMessage().getText().compareTo(o.requestMessage.getTextMessage().getText());
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
                && requestMessage.getTextMessage().equals(that.requestMessage.getTextMessage());
    }

    @Override
    public int hashCode() {
        return Objects.hash(dateTime);
    }

}
