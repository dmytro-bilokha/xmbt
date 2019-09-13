package com.dmytrobilokha.xmbt.fs;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class FsService {

    public void consumeFile(@Nonnull Path file, @Nonnull FsConsumer<Reader> consumer) throws IOException {
        try (Reader reader = Files.newBufferedReader(file)) {
            consumer.accept(reader);
        }
    }

    public void writeFile(@Nonnull Path file, @Nonnull FsConsumer<Writer> consumer) throws IOException {
        try (Writer writer = Files.newBufferedWriter(file
                , StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            consumer.accept(writer);
        }
    }
}
