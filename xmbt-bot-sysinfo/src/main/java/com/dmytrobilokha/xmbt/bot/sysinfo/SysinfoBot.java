package com.dmytrobilokha.xmbt.bot.sysinfo;

import com.dmytrobilokha.xmbt.api.BotConnector;
import com.dmytrobilokha.xmbt.api.RequestMessage;
import com.dmytrobilokha.xmbt.api.Response;
import com.dmytrobilokha.xmbt.api.ResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

class SysinfoBot implements Runnable {

    private static final String NEW_LINE = System.lineSeparator();
    private static final Logger LOG = LoggerFactory.getLogger(SysinfoBot.class);

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
        Package thisPackage = this.getClass().getPackage();
        StringBuilder textBuilder = new StringBuilder()
                .append("Implementation Title: ").append(thisPackage.getImplementationTitle())
                .append(NEW_LINE)
                .append("Implementation Vendor: ").append(thisPackage.getImplementationVendor())
                .append(NEW_LINE)
                .append("Implementation Version: ").append(thisPackage.getImplementationVersion())
                .append(NEW_LINE)
                .append("Specification Title: ").append(thisPackage.getSpecificationTitle())
                .append(NEW_LINE)
                .append("Specification Vendor: ").append(thisPackage.getSpecificationVendor())
                .append(NEW_LINE)
                .append("Specification Version: ").append(thisPackage.getSpecificationVersion());
        return textBuilder.toString();
    }

}
