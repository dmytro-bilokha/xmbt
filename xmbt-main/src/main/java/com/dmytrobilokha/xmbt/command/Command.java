package com.dmytrobilokha.xmbt.command;

import com.dmytrobilokha.xmbt.api.messaging.RequestMessage;
import com.dmytrobilokha.xmbt.api.messaging.ResponseMessage;

import javax.annotation.Nonnull;

public interface Command {

    @Nonnull
    String getName();

    void acceptRequest(@Nonnull RequestMessage requestMessage) throws InterruptedException;

    void acceptResponse(@Nonnull ResponseMessage responseMessage) throws InterruptedException;

    default void tick() throws InterruptedException {
        //By default, do nothing in the main process loop
    }

}
