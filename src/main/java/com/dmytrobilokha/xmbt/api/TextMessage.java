package com.dmytrobilokha.xmbt.api;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Objects;

public class TextMessage implements Serializable {

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
        return address.equals(o.address);
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
