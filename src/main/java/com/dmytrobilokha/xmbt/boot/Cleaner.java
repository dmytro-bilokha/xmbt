package com.dmytrobilokha.xmbt.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class Cleaner extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(Cleaner.class);

    @Nonnull
    private final Path pidFilePath;
    @Nonnull
    private final Loader loader;

    Cleaner(@Nonnull Loader loader, @Nonnull Path pidFilePath) {
        this.loader = loader;
        this.pidFilePath = pidFilePath;
        this.setName("cleaner-hook");
    }

    @Override
    public void run() {
        deletePidFile();
        loader.shutdown();
    }

    private void deletePidFile() {
        try {
            Files.deleteIfExists(pidFilePath);
        } catch (IOException ex) {
            LOG.error("Failed to delete PID file '" + pidFilePath + "'", ex);
            return;
        }
        LOG.debug("PID file '" + pidFilePath + "' has been deleted");
    }

}
