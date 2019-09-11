package com.dmytrobilokha.xmbt.fs;

import java.io.IOException;

@FunctionalInterface
public interface FsConsumer<T> {

    void accept(T t) throws IOException;

}
