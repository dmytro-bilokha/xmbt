package com.dmytrobilokha.xmbt.api.messaging;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public interface MessageBus {

    @CheckForNull
    RequestMessage get();

    @Nonnull
    RequestMessage getBlocking() throws InterruptedException;

    boolean send(@Nonnull ResponseMessage message);

    void sendBlocking(@Nonnull ResponseMessage message) throws InterruptedException;

}
