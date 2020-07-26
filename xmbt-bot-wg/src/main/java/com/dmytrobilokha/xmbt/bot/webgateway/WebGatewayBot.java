package com.dmytrobilokha.xmbt.bot.webgateway;

import com.dmytrobilokha.xmbt.api.messaging.MessageBus;
import com.dmytrobilokha.xmbt.api.messaging.Request;
import com.dmytrobilokha.xmbt.api.messaging.RequestMessage;
import com.dmytrobilokha.xmbt.api.messaging.Response;
import com.dmytrobilokha.xmbt.api.messaging.ResponseMessage;
import com.dmytrobilokha.xmbt.api.service.config.ConfigService;
import com.dmytrobilokha.xmbt.bot.webgateway.config.WebGatewayLinkPrefix;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class WebGatewayBot implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(WebGatewayBot.class);

    @Nonnull
    private final MessageBus imMessageBus;
    @Nonnull
    private final Map<String, RequestMessage> pathKeyToImRequestMap;
    @Nonnull
    private final HttpServer webServer;
    @Nonnull
    private final ConfigService configService;
    @Nonnull
    private final SecureRandom secureRandom;
    @Nonnull
    private final Map<String, String> userToPathKeyMap;

    WebGatewayBot(
            @Nonnull ConcurrentHashMap<String, RequestMessage> pathKeyToImRequestMap
            , @Nonnull MessageBus imMessageBus
            , @Nonnull HttpServer webServer
            , @Nonnull ConfigService configService
    ) throws NoSuchAlgorithmException {
        this.pathKeyToImRequestMap = pathKeyToImRequestMap;
        this.imMessageBus = imMessageBus;
        this.webServer = webServer;
        this.configService = configService;
        this.secureRandom = SecureRandom.getInstanceStrong();
        this.userToPathKeyMap = new HashMap<>();
    }

    @Override
    public void run() {
        webServer.start();
        try {
            while (!Thread.currentThread().isInterrupted()) {
                RequestMessage incomingMessage = imMessageBus.getBlocking();
                LOG.debug("Got from queue incoming {}", incomingMessage);
                var messageText = incomingMessage.getTextMessage().getText();
                var pathKey = userToPathKeyMap.get(incomingMessage.getTextMessage().getAddress());
                if (messageText.isBlank() || pathKey == null) {
                    pathKey = Long.toHexString(secureRandom.nextLong());
                }
                if (incomingMessage.getRequest() == Request.RESPOND) {
                    pathKeyToImRequestMap.put(pathKey, incomingMessage);
                    userToPathKeyMap.put(incomingMessage.getTextMessage().getAddress(), pathKey);
                }
                imMessageBus.sendBlocking(new ResponseMessage(
                        incomingMessage
                        , Response.OK
                        , "Your gateway link is "
                         + configService.getProperty(WebGatewayLinkPrefix.class).getStringValue() + pathKey
                ));
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            LOG.info("Got interruption exception, exiting", ex);
        } catch (RuntimeException ex) {
            LOG.error("Got unexpected runtime exception, exiting", ex);
        } finally {
            webServer.stop(1);
        }
    }

}
