package com.dmytrobilokha.xmbt.bot.sysinfo;

import com.dmytrobilokha.xmbt.api.BotConnector;
import com.dmytrobilokha.xmbt.api.RequestMessage;
import com.dmytrobilokha.xmbt.api.Response;
import com.dmytrobilokha.xmbt.api.ResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.time.LocalDateTime;

class SysinfoBot implements Runnable {

    private static final String NEW_LINE = System.lineSeparator();
    private static final long BYTES_IN_MEGABYTE = 1024 * 1024;
    private static final Logger LOG = LoggerFactory.getLogger(SysinfoBot.class);
    private static final LocalDateTime INIT_DATE_TIME = LocalDateTime.now();

    @Nonnull
    private final BotConnector messageQueueClient;

    SysinfoBot(@Nonnull BotConnector messageQueueClient) {
        this.messageQueueClient = messageQueueClient;
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
