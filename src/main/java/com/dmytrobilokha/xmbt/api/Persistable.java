package com.dmytrobilokha.xmbt.api;

import javax.annotation.Nonnull;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;

public interface Persistable {

    void save(@Nonnull BufferedOutputStream outputStream);

    void load(@Nonnull BufferedInputStream inputStream);

    @Nonnull
    String getPersistenceKey();

}
