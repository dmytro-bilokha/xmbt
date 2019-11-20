package com.dmytrobilokha.xmbt.api;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Objects;

public class TextMessage implements Serializable {

    private static final int JID_SEPARATOR = '/';

    @Nonnull
    private final String address;
    @Nonnull
    private final String text;

    public TextMessage(@Nonnull String address, @Nonnull String text) {
        this.address = address;
        this.text = text;
    }

    @Nonnull
    public TextMessage withNewText(@Nonnull String newText) {
        return new TextMessage(this.address, newText);
    }

    @Nonnull
    public String getAddress() {
        return address;
    }

    @Nonnull
    public String getText() {
        return text;
    }

    public boolean hasSameAddress(@Nonnull TextMessage o) {
        int minLength = Math.min(address.length(), o.address.length());
        for (int i = 0; i < minLength; i++) {
            int thisCodePoint = address.codePointAt(i);
            int thatCodePoint = o.address.codePointAt(i);
            if (thisCodePoint == thatCodePoint) {
                //XMPP address should be compared without resource part
                if (thisCodePoint == JID_SEPARATOR) {
                    return true;
                }
            } else {
                return false;
            }
        }
        return address.length() == o.address.length();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TextMessage message = (TextMessage) o;
        return address.equals(message.address)
                && text.equals(message.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address);
    }

    @Override
    public String toString() {
        return "TextMessage{"
                + "address='" + address + '\''
                + ", text='" + text + '\''
                + '}';
    }

}
