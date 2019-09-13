package com.dmytrobilokha.xmbt.boot;

import com.dmytrobilokha.xmbt.config.ConfigPropertyProducer;
import com.dmytrobilokha.xmbt.config.ConfigService;
import com.dmytrobilokha.xmbt.config.property.ConfigFilePathProperty;
import com.dmytrobilokha.xmbt.config.property.LogFilePathProperty;
import com.dmytrobilokha.xmbt.config.property.LogLevelProperty;
import com.dmytrobilokha.xmbt.config.property.NsApiKeyProperty;
import com.dmytrobilokha.xmbt.config.property.PidFilePathProperty;
import com.dmytrobilokha.xmbt.fs.FsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public final class Loader {

    private static final Logger LOG = LoggerFactory.getLogger(Loader.class);

    private static final List<ConfigPropertyProducer> SYSTEM_PROPERTY_PRODUCERS = List.of(
            ConfigFilePathProperty::new
            , LogFilePathProperty::new
            , LogLevelProperty::new
            , PidFilePathProperty::new
    );

    private static final List<ConfigPropertyProducer> CONFIGFILE_PROPERTY_PRODUCERS = List.of(
            NsApiKeyProperty::new
    );

    @Nonnull
    private final FsService fsService;
    @Nonnull
    private final ConfigService configService;
    @Nonnull
    private final Thread mainThread;

    private volatile boolean shutdownRequested;

    private Loader() {
        mainThread = Thread.currentThread();
        fsService = new FsService();
        configService = new ConfigService(
                fsService, SYSTEM_PROPERTY_PRODUCERS, CONFIGFILE_PROPERTY_PRODUCERS);
    }

    public static void main(@Nonnull String[] cliArgs) {
        new Loader().start();
    }

    private void start() {
        init();
        run();
    }

    private void init() {
        System.out.print("Initializing...");
        try {
            configService.init();
            new LoggerInitializer(configService).init();
            Path pidFilePath = configService.getProperty(PidFilePathProperty.class).getValue();
            writePidToFile(pidFilePath);
            addShutdownHook(pidFilePath);
            System.out.println("OK");
            detachFromTerminal();
        } catch (Exception ex) {
            System.out.println("FAIL! Check log for details");
            LOG.error("Failed to initialize the service", ex);
            System.exit(1);
        }
        LOG.info("Api key={}", configService.getProperty(NsApiKeyProperty.class).getStringValue());
    }

    private void run() {
        try {
            while (!shutdownRequested) {
                Thread.sleep(5000);
            }
            LOG.info("Service shutdown requested, exiting...");
        } catch (InterruptedException ex) {
            LOG.error("The service execution has been interrupted");
        }
    }

    private void writePidToFile(@Nonnull Path pidFilePath) throws InitializationException {
        try {
            fsService.writeFile(pidFilePath, writer -> writer.write(Long.toString(ProcessHandle.current().pid())));
        } catch (IOException ex) {
            throw new InitializationException("Failed to save PID to file '" + pidFilePath + "'", ex);
        }
    }

    void shutdown() {
        shutdownRequested = true;
        try {
            mainThread.join();
        } catch (InterruptedException ex) {
            LOG.warn("Interrupted during waiting for the main thread to shutdown", ex);
        }
    }

    private void addShutdownHook(@Nonnull Path pidFilePath) {
        Runtime.getRuntime()
                .addShutdownHook(new Cleaner(this, pidFilePath));
    }

    private void detachFromTerminal() throws IOException {
        System.in.close();
        System.out.close();
        System.err.close();
    }

}
