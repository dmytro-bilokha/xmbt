package com.dmytrobilokha.xmbt.boot;

public class InitializationException extends Exception {

    public InitializationException(String message) {
        super(message);
    }

    public InitializationException(String message, Exception ex) {
        super(message, ex);
    }

}

