package com.dmytrobilokha.xmbt.command;

import com.dmytrobilokha.xmbt.xmpp.TextMessage;

import javax.annotation.Nonnull;

public interface Command {

    @Nonnull
    String getName();

    void execute(@Nonnull TextMessage commandMessage) throws InterruptedException;

    default void tick() throws InterruptedException {
        //By default, do nothing in the main process loop
    }

}
