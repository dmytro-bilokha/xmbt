package com.dmytrobilokha.xmbt.manager;

import com.dmytrobilokha.xmbt.api.messaging.MessageBus;
import com.dmytrobilokha.xmbt.api.messaging.RequestMessage;
import com.dmytrobilokha.xmbt.api.messaging.ResponseMessage;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.concurrent.BlockingQueue;

public class BotConnector implements MessageBus {

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

    @Override
    @CheckForNull
    public RequestMessage get() {
        return incomingMessageQueue.poll();
    }

    @Override
    @Nonnull
    public RequestMessage getBlocking() throws InterruptedException {
        return incomingMessageQueue.take();
    }

    @Override
    public boolean send(@Nonnull ResponseMessage message) {
        return outgoingMessageQueue.offer(message);
    }

    @Override
    public void sendBlocking(@Nonnull ResponseMessage message) throws InterruptedException {
        outgoingMessageQueue.put(message);
    }

}
