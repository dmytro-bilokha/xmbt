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

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.SQLException;
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
    private final WebGatewayDao webGatewayDao;
    @Nonnull
    private final SecureRandom secureRandom;

    WebGatewayBot(
            @Nonnull ConcurrentHashMap<String, RequestMessage> pathKeyToImRequestMap
            , @Nonnull MessageBus imMessageBus
            , @Nonnull HttpServer webServer
            , @Nonnull ConfigService configService
            , @Nonnull WebGatewayDao webGatewayDao
    ) throws NoSuchAlgorithmException {
        this.pathKeyToImRequestMap = pathKeyToImRequestMap;
        this.imMessageBus = imMessageBus;
        this.webServer = webServer;
        this.configService = configService;
        this.webGatewayDao = webGatewayDao;
        this.secureRandom = SecureRandom.getInstanceStrong();
    }

    @Override
    public void run() {
        webServer.start();
        try {
            while (!Thread.currentThread().isInterrupted()) {
                var incomingMessage = imMessageBus.getBlocking();
                processMessage(incomingMessage);
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            LOG.info("Got interruption exception, exiting", ex);
        } catch (RuntimeException ex) {
            LOG.error("Got unexpected runtime exception, exiting", ex);
        } catch (SQLException ex) {
            LOG.error("Got unexpected DB exception, exiting", ex);
        } finally {
            webServer.stop(1);
        }
    }

    private void processMessage(@Nonnull RequestMessage incomingMessage) throws InterruptedException, SQLException {
        LOG.debug("Got from queue incoming {}", incomingMessage);
        if (incomingMessage.getRequest() != Request.RESPOND) {
            imMessageBus.sendBlocking(new ResponseMessage(
                    incomingMessage
                    , Response.OK
                    , "Ok"));
            return;
        }
        var userAddress = incomingMessage.getTextMessage().getAddress();
        var messageText = incomingMessage.getTextMessage().getText().strip();
        var webGateway = webGatewayDao.findWebGateway(userAddress);
        if ("delete".equals(messageText)) {
            deleteWebGateway(webGateway, incomingMessage);
            return;
        }
        createOrUpdateGateway(webGateway, incomingMessage);
    }

    private void deleteWebGateway(
            @CheckForNull WebGateway webGateway,
            @Nonnull RequestMessage requestMessage) throws InterruptedException, SQLException {
        if (webGateway == null) {
            imMessageBus.sendBlocking(new ResponseMessage(
                    requestMessage
                    , Response.OK
                    , "Your gateway doesn't exist, nothing to delete"));
            return;
        }
        webGatewayDao.deleteWebGateway(webGateway);
        pathKeyToImRequestMap.remove(pathIdToKey(webGateway.pathId()));
        imMessageBus.sendBlocking(new ResponseMessage(
                requestMessage
                , Response.OK
                , "Your gateway has been deleted"));
    }

    private String pathIdToKey(@Nonnull Long pathId) {
        return Long.toHexString(pathId);
    }

    private void createOrUpdateGateway(
            @CheckForNull WebGateway existingWebGateway,
            @Nonnull RequestMessage requestMessage) throws InterruptedException, SQLException {
        var webGateway = existingWebGateway;
        if (webGateway == null) {
            webGateway = new WebGateway(requestMessage.getTextMessage().getAddress(), secureRandom.nextLong());
            webGatewayDao.insertWebGateway(webGateway);
        }
        var pathKey = pathIdToKey(webGateway.pathId());
        pathKeyToImRequestMap.put(pathKey, requestMessage);
        imMessageBus.sendBlocking(new ResponseMessage(
                requestMessage
                , Response.OK
                , "Your gateway link is "
                + configService.getProperty(WebGatewayLinkPrefix.class).getStringValue() + pathKey
        ));
    }

}
