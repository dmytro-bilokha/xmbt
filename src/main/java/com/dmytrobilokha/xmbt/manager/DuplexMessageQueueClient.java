package com.dmytrobilokha.xmbt.manager;

import com.dmytrobilokha.xmbt.xmpp.TextMessage;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.concurrent.BlockingQueue;

public class DuplexMessageQueueClient {

    @Nonnull
    private final BlockingQueue<TextMessage> incomingMessageQueue;
    @Nonnull
    private final BlockingQueue<TextMessage> outgoingMessageQueue;

    public DuplexMessageQueueClient(@Nonnull BlockingQueue<TextMessage> incomingMessageQueue
                             , @Nonnull  BlockingQueue<TextMessage> outgoingMessageQueue) {
        this.incomingMessageQueue = incomingMessageQueue;
        this.outgoingMessageQueue = outgoingMessageQueue;
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
        return outgoingMessageQueue.offer(message);
    }

    public void sendBlocking(@Nonnull TextMessage message) throws InterruptedException {
        outgoingMessageQueue.put(message);
    }

}
