package com.dmytrobilokha.xmbt.api;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.concurrent.BlockingQueue;

public class BotConnector {

    @Nonnull
    private final BlockingQueue<RequestMessage> incomingMessageQueue;
    @Nonnull
    private final BlockingQueue<ResponseMessage> outgoingMessageQueue;

    public BotConnector(@Nonnull BlockingQueue<RequestMessage> incomingMessageQueue
                        , @Nonnull  BlockingQueue<ResponseMessage> outgoingMessageQueue
    ) {
        this.incomingMessageQueue = incomingMessageQueue;
        this.outgoingMessageQueue = outgoingMessageQueue;
    }

    @CheckForNull
    public RequestMessage get() {
        return incomingMessageQueue.poll();
    }

    @Nonnull
    public RequestMessage getBlocking() throws InterruptedException {
        return incomingMessageQueue.take();
    }

    public boolean send(@Nonnull ResponseMessage message) {
        return outgoingMessageQueue.offer(message);
    }

    public void sendBlocking(@Nonnull ResponseMessage message) throws InterruptedException {
        outgoingMessageQueue.put(message);
    }

}
