package com.dmytrobilokha.xmbt.manager;

public class ConnectionException extends Exception {

    public ConnectionException(String message, Exception ex) {
        super(message, ex);
    }

}
