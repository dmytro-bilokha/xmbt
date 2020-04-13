package com.dmytrobilokha.xmbt.bot.webgateway;

import com.dmytrobilokha.xmbt.api.bot.BotFactory;
import com.dmytrobilokha.xmbt.api.messaging.MessageBus;
import com.dmytrobilokha.xmbt.api.messaging.RequestMessage;
import com.dmytrobilokha.xmbt.api.service.ServiceContainer;
import com.dmytrobilokha.xmbt.bot.webgateway.config.WebGatewayBindAddress;
import com.dmytrobilokha.xmbt.bot.webgateway.config.WebGatewayBindPort;
import com.sun.net.httpserver.HttpServer;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;

public class WebGatewayBotFactory implements BotFactory {

    private static final int HTTP_QUEUE_SIZE = 100;

    @Override
    @Nonnull
    public String getBotName() {
        return "wg";
    }

    @Override
    @Nonnull
    public WebGatewayBot produce(@Nonnull MessageBus connector, @Nonnull ServiceContainer serviceContainer) {
        var configService = serviceContainer.getConfigService();
        HttpServer webServer;
        try {
            webServer = HttpServer.create(
                    new InetSocketAddress(
                            configService.getProperty(WebGatewayBindAddress.class).getStringValue()
                            , configService.getProperty(WebGatewayBindPort.class).getValue()
                    )
                    , HTTP_QUEUE_SIZE
            );
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to create a http web-server", ex);
        }
        var pathKeyMap = new ConcurrentHashMap<String, RequestMessage>();
        webServer.createContext("/wg", new WebRequestHandler(
                pathKeyMap
                , connector
                , readResource("FormPageTemplate.html")
                , readResource("ErrorPageTemplate.html")
        ));
        try {
            return new WebGatewayBot(pathKeyMap, connector, webServer, configService);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Failed to initialize bot, because there are no suitable random"
                    + " number generating algorithms", ex);
        }
    }

    @Nonnull
    private String readResource(@Nonnull String templateName) {
        try (var resourceStream = this.getClass().getModule().getResourceAsStream(templateName)) {
            return new String(resourceStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read template '" + templateName + "'", ex);
        }
    }

}
