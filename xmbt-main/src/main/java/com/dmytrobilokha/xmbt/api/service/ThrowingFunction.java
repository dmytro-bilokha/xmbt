package com.dmytrobilokha.xmbt.api.service;

@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Exception> {

    R apply(T t) throws E;

}
