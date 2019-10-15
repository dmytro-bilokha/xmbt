package com.dmytrobilokha.xmbt.manager;

import com.dmytrobilokha.xmbt.xmpp.TextMessage;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.concurrent.BlockingQueue;

public class BotConnector {

    @Nonnull
    private final BlockingQueue<TextMessage> incomingMessageQueue;
    @Nonnull
    private final BlockingQueue<TextMessage> outgoingMessageQueue;
    @Nonnull
    private final String messagePrefix;

    public BotConnector(@Nonnull BlockingQueue<TextMessage> incomingMessageQueue
                        , @Nonnull  BlockingQueue<TextMessage> outgoingMessageQueue
                        , @Nonnull String messagePrefix
    ) {
        this.incomingMessageQueue = incomingMessageQueue;
        this.outgoingMessageQueue = outgoingMessageQueue;
        this.messagePrefix = messagePrefix;
    }

    @CheckForNull
    public TextMessage get() {
        return incomingMessageQueue.poll();
    }

    @Nonnull
    public TextMessage getBlocking() throws InterruptedException {
        return incomingMessageQueue.take();
    }

    public boolean send(@Nonnull TextMessage message) {
        return outgoingMessageQueue.offer(appendTransformation(message));
    }

    public void sendBlocking(@Nonnull TextMessage message) throws InterruptedException {
        outgoingMessageQueue.put(appendTransformation(message));
    }

    @Nonnull
    private TextMessage appendTransformation(@Nonnull TextMessage message) {
        return message.withNewText(messagePrefix + message.getText());
    }

}
