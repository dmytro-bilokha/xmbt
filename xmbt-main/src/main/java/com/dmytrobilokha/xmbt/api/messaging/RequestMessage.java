package com.dmytrobilokha.xmbt.api.messaging;

import javax.annotation.Nonnull;
import java.io.Serializable;

public class RequestMessage implements Serializable {

    private final long id;
    @Nonnull
    private final String sender;
    @Nonnull
    private final String receiver;
    @Nonnull
    private final Request request;
    @Nonnull
    private final TextMessage textMessage;

    public RequestMessage(
            long id
            , @Nonnull String sender
            , @Nonnull String receiver
            , @Nonnull Request request
            , @Nonnull TextMessage textMessage
    ) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.request = request;
        this.textMessage = textMessage;
    }

    public long getId() {
        return id;
    }

    @Nonnull
    public String getSender() {
        return sender;
    }

    @Nonnull
    public String getReceiver() {
        return receiver;
    }

    @Nonnull
    public Request getRequest() {
        return request;
    }

    @Nonnull
    public TextMessage getTextMessage() {
        return textMessage;
    }

    @Override
    public String toString() {
        return "RequestMessage{"
                + "id=" + id
                + ", sender='" + sender + '\''
                + ", receiver='" + receiver + '\''
                + ", command=" + request
                + ", textMessage=" + textMessage
                + '}';
    }
}
