package com.dmytrobilokha.xmbt.api.messaging;

import javax.annotation.Nonnull;

public class ResponseMessage {

    private final long id;
    @Nonnull
    private final String sender;
    @Nonnull
    private final String receiver;
    @Nonnull
    private final Response response;
    @Nonnull
    private final TextMessage textMessage;

    public ResponseMessage(
            @Nonnull RequestMessage originalMessage
            , @Nonnull Response response
            , @Nonnull String responseText) {
        this.id = originalMessage.getId();
        this.sender = originalMessage.getReceiver();
        this.receiver = originalMessage.getSender();
        this.response = response;
        this.textMessage = new TextMessage(originalMessage.getTextMessage().getAddress(), responseText);
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
    public Response getResponse() {
        return response;
    }

    @Nonnull
    public TextMessage getTextMessage() {
        return textMessage;
    }

    @Override
    public String toString() {
        return "ResponseMessage{"
                + "id=" + id
                + ", sender='" + sender + '\''
                + ", receiver='" + receiver + '\''
                + ", response=" + response
                + ", textMessage=" + textMessage
                + '}';
    }
}
