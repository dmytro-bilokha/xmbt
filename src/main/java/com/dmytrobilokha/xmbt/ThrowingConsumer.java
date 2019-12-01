package com.dmytrobilokha.xmbt;

@FunctionalInterface
public interface ThrowingConsumer<T, R extends Exception> {

    void accept(T t) throws R;

}
