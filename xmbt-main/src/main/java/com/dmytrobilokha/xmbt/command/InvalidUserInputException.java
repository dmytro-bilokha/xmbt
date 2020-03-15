package com.dmytrobilokha.xmbt.command;

import javax.annotation.Nonnull;

public class InvalidUserInputException extends Exception {

    public InvalidUserInputException(@Nonnull String message) {
        super(message);
    }

    public InvalidUserInputException(@Nonnull String message, @Nonnull Exception ex) {
        super(message, ex);
    }

}
