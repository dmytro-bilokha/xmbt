package com.dmytrobilokha.xmbt.bot.sysinfo;

import com.dmytrobilokha.xmbt.api.messaging.MessageBus;
import com.dmytrobilokha.xmbt.api.messaging.RequestMessage;
import com.dmytrobilokha.xmbt.api.messaging.Response;
import com.dmytrobilokha.xmbt.api.messaging.ResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Properties;

class SysinfoBot implements Runnable {

    private static final String NEW_LINE = System.lineSeparator();
    private static final long BYTES_IN_MEGABYTE = 1024 * 1024;
    private static final Logger LOG = LoggerFactory.getLogger(SysinfoBot.class);
    private static final LocalDateTime INIT_DATE_TIME = LocalDateTime.now();

    @Nonnull
    private final MessageBus messageQueueClient;
    @Nonnull
    private final String buildTimestamp;

    SysinfoBot(@Nonnull MessageBus messageQueueClient) {
        this.messageQueueClient = messageQueueClient;
        this.buildTimestamp = getBuildTimestamp();
    }

    private String getBuildTimestamp() {
        try (var propertiesStream = this.getClass().getModule().getResourceAsStream("sysinfo.properties")) {
            if (propertiesStream == null) {
                return "UNKNOWN (no resource)";
            }
            Properties properties = new Properties();
            properties.load(propertiesStream);
            return properties.getProperty("buildTimestamp", "UNKNOWN (no property)");
        } catch (IOException ex) {
            LOG.error("Failed to load module resource with properties", ex);
            return "UNKNOWN (failed to read resource)";
        }
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                RequestMessage incomingMessage = messageQueueClient.getBlocking();
                LOG.debug("Got from queue incoming {}", incomingMessage);
                messageQueueClient.sendBlocking(new ResponseMessage(
                        incomingMessage, Response.OK, buildSysInfoText()));
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            LOG.info("Got interruption exception, exiting", ex);
        }
    }

    @Nonnull
    private String buildSysInfoText() {
        Runtime runtime = Runtime.getRuntime();
        StringBuilder textBuilder = new StringBuilder()
                .append("Module name and version: ").append(this.getClass().getModule()
                        .getDescriptor().toNameAndVersion())
                .append(NEW_LINE)
                .append("Build on: ").append(buildTimestamp)
                .append(NEW_LINE)
                .append("Initialized on: ").append(INIT_DATE_TIME)
                .append(NEW_LINE)
                .append("Uptime: ").append(getUptimeString())
                .append(NEW_LINE)
                .append("Runtime version: ").append(Runtime.version())
                .append(NEW_LINE)
                .append("Available processors/cores: ").append(runtime.availableProcessors())
                .append(NEW_LINE)
                .append("Free JVM memory, Mb: ").append(runtime.freeMemory() / BYTES_IN_MEGABYTE)
                .append(NEW_LINE)
                .append("Max JVM memory, Mb: ").append(runtime.maxMemory() / BYTES_IN_MEGABYTE)
                .append(NEW_LINE)
                .append("Total JVM memory, Mb: ").append(runtime.totalMemory() / BYTES_IN_MEGABYTE)
                .append(NEW_LINE);
        return textBuilder.toString();
    }

    @Nonnull
    private String getUptimeString() {
        Duration uptime = Duration.between(INIT_DATE_TIME, LocalDateTime.now());
        return uptime.toDaysPart() + " days " + uptime.toHoursPart() + " hours " + uptime.toMinutesPart() + " minutes";
    }

}
