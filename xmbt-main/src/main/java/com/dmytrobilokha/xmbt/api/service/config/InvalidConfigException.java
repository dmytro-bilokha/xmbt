package com.dmytrobilokha.xmbt.api.service.config;

public class InvalidConfigException extends Exception {

    public InvalidConfigException(String message) {
        super(message);
    }

    public InvalidConfigException(String message, Exception ex) {
        super(message, ex);
    }

}
