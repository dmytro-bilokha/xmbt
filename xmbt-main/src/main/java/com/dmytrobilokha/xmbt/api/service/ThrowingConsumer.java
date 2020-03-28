package com.dmytrobilokha.xmbt.api.service;

@FunctionalInterface
public interface ThrowingConsumer<T, R extends Exception> {

    void accept(T t) throws R;

}
