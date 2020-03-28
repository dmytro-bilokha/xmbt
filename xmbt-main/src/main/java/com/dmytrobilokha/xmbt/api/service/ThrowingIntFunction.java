package com.dmytrobilokha.xmbt.api.service;

@FunctionalInterface
public interface ThrowingIntFunction<T, E extends Exception> {

    int apply(T t) throws E;

}
