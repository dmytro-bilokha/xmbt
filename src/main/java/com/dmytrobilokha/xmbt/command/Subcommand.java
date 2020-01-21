package com.dmytrobilokha.xmbt.command;

import com.dmytrobilokha.xmbt.api.RequestMessage;
import com.dmytrobilokha.xmbt.api.ResponseMessage;

import javax.annotation.Nonnull;
import java.util.Scanner;

public interface Subcommand {

    @Nonnull
    String getName();

    void execute(
            @Nonnull Scanner commandMessageScanner, @Nonnull RequestMessage requestMessage) throws InterruptedException;

    default void acceptResponse(@Nonnull ResponseMessage responseMessage) throws InterruptedException {
        //Rare subcommand needs to get messages from bots
    }

}
