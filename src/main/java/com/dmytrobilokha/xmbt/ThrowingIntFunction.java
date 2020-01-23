package com.dmytrobilokha.xmbt;

@FunctionalInterface
public interface ThrowingIntFunction<T, E extends Exception> {

    int apply(T t) throws E;

}
