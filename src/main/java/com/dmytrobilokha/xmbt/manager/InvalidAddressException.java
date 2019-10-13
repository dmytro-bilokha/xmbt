package com.dmytrobilokha.xmbt.manager;

public class InvalidAddressException extends Exception {

    public InvalidAddressException(String message, Exception ex) {
        super(message, ex);
    }

}
