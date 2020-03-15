package com.dmytrobilokha.xmbt.fs;

import com.dmytrobilokha.xmbt.ThrowingConsumer;

import javax.annotation.Nonnull;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class FsService {

    public void readFile(
            @Nonnull Path file, @Nonnull ThrowingConsumer<Reader, IOException> consumer) throws IOException {
        try (Reader reader = Files.newBufferedReader(file)) {
            consumer.accept(reader);
        }
    }

    public void writeFile(
            @Nonnull Path file, @Nonnull ThrowingConsumer<Writer, IOException> consumer) throws IOException {
        try (Writer writer = Files.newBufferedWriter(file
                , StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            consumer.accept(writer);
        }
    }

    public void inputFromFile(
            @Nonnull Path file
            , @Nonnull ThrowingConsumer<BufferedInputStream, IOException> consumer
    ) throws IOException {
        try (
                InputStream inputStream = Files.newInputStream(file);
                BufferedInputStream bufferedIs = new BufferedInputStream(inputStream)
        ) {
            consumer.accept(bufferedIs);
        }
    }

    public void outputToFile(
            @Nonnull Path file
            , @Nonnull ThrowingConsumer<BufferedOutputStream, IOException> consumer
    ) throws IOException {
        try (
                OutputStream outputStream = Files.newOutputStream(file);
                BufferedOutputStream bufferedOs = new BufferedOutputStream(outputStream)
        ) {
            consumer.accept(bufferedOs);
        }
    }

    public boolean isRegularFile(@Nonnull Path file) {
        return Files.isRegularFile(file);
    }

}
