package com.dmytrobilokha.xmbt.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Cleaner extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(Cleaner.class);

    @Nonnull
    private final Set<Path> filesToBeDeleted = new CopyOnWriteArraySet<>();
    @Nonnull
    private final Set<Thread> threadsToBeInterrupted = new CopyOnWriteArraySet<>();

    Cleaner() {
        this.setName("cleaner-hook");
    }

    public void registerThread(@Nonnull Thread thread) {
        threadsToBeInterrupted.add(thread);
        LOG.debug("Registered {} to be interrupted on exit", thread);
    }

    public void registerFile(@Nonnull Path filePath) {
        filesToBeDeleted.add(filePath);
        LOG.debug("Registered {} to be deleted on exit", filePath);
    }

    @Override
    public void run() {
        threadsToBeInterrupted.forEach(Thread::interrupt);
        LOG.debug("Interrupt signal has been sent to {}", threadsToBeInterrupted);
        filesToBeDeleted.forEach(this::deletePidFile);
    }

    private void deletePidFile(Path filePath) {
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            LOG.error("Failed to delete file '{}'", filePath, ex);
            return;
        }
        LOG.debug("File '{}' has been deleted", filePath);
    }

}
