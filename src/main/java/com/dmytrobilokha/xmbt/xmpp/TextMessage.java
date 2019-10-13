package com.dmytrobilokha.xmbt.xmpp;

public class TextMessage {

    private final String address;
    private final String text;

    public TextMessage(String address, String text) {
        this.address = address;
        this.text = text;
    }

    public String getAddress() {
        return address;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return "TextMessage{"
                + "address='" + address + '\''
                + ", text='" + text + '\''
                + '}';
    }
}
