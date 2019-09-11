package com.dmytrobilokha.xmbt.fs;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

public class FsService {

    public void consumeFile(Path file, FsConsumer<Reader> consumer) throws IOException {
        try (Reader reader = Files.newBufferedReader(file)) {
            consumer.accept(reader);
        }
    }
}
