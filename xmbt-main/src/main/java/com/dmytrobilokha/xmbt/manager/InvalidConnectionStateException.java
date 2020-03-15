package com.dmytrobilokha.xmbt.manager;

public class InvalidConnectionStateException extends Exception {

    public InvalidConnectionStateException(String message) {
        super(message);
    }

    public InvalidConnectionStateException(String message, Exception ex) {
        super(message, ex);
    }

}
