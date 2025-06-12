package com.dmytrobilokha.xmbt.bot.webgateway;

import com.dmytrobilokha.xmbt.api.messaging.MessageBus;
import com.dmytrobilokha.xmbt.api.messaging.RequestMessage;
import com.dmytrobilokha.xmbt.api.messaging.Response;
import com.dmytrobilokha.xmbt.api.messaging.ResponseMessage;
import com.dmytrobilokha.xmbt.api.messaging.TextMessage;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.commons.text.StringEscapeUtils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class WebRequestHandler implements HttpHandler {

    private static final int REQUEST_MAX_BYTES = 16384;

    @Nonnull
    private final MessageBus messageBus;
    @Nonnull
    private final Map<String, RequestMessage> pathKeyToImRequestMap;
    @Nonnull
    private final String formPageTemplate;
    @Nonnull
    private final String errorPageTemplate;

    WebRequestHandler(
            @Nonnull ConcurrentHashMap<String, RequestMessage> pathKeyToImRequestMap
            , @Nonnull MessageBus messageBus
            , @Nonnull String formPageTemplate
            , @Nonnull String errorPageTemplate
    ) {
        this.pathKeyToImRequestMap = pathKeyToImRequestMap;
        this.messageBus = messageBus;
        this.formPageTemplate = formPageTemplate;
        this.errorPageTemplate = errorPageTemplate;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        switch (exchange.getRequestMethod()) {
            case "GET" -> doGet(exchange);
            case "POST" -> doPost(exchange);
            default -> sendError(HttpURLConnection.HTTP_BAD_METHOD
                    , "Don't know how to handle method '" + exchange.getRequestMethod() + '\''
                    , exchange);
        }
    }

    private void doGet(@Nonnull HttpExchange exchange) throws IOException {
        String pathKey = extractPathKey(exchange);
        RequestMessage originalRequest = pathKeyToImRequestMap.get(pathKey);
        if (originalRequest == null) {
            sendError(HttpURLConnection.HTTP_NOT_FOUND, "The URL you requested doesn't exist.", exchange);
            return;
        }
        sendMessageForm(exchange, originalRequest.getTextMessage());
    }

    private void doPost(@Nonnull HttpExchange exchange) throws IOException {
        String key = extractPathKey(exchange);
        var request = pathKeyToImRequestMap.get(key);
        if (request == null) {
            sendError(HttpURLConnection.HTTP_NOT_FOUND, "Unknown key: '" + key + '\'', exchange);
            return;
        }
        String payload;
        try (InputStream requestBodyStream = exchange.getRequestBody()) {
            payload = new String(requestBodyStream.readNBytes(REQUEST_MAX_BYTES), StandardCharsets.UTF_8);
        }
        if (!payload.startsWith("content=")) {
            sendError(
                    HttpURLConnection.HTTP_BAD_REQUEST, "Couldn't extract message content from the request", exchange);
            return;
        }
        payload = payload.substring("content=".length());
        var messageText = URLDecoder.decode(payload, StandardCharsets.UTF_8);
        var messageSent = messageBus.send(new ResponseMessage(request, Response.OK, messageText));
        if (!messageSent) {
            sendError(HttpURLConnection.HTTP_INTERNAL_ERROR, "Failed to send a message, the queue is full", exchange);
            return;
        }
        sendSelfRedirect(exchange);
    }

    private void sendSelfRedirect(@Nonnull HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Location", exchange.getRequestURI().toString());
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_SEE_OTHER, 0);
        var out = exchange.getResponseBody();
        out.flush();
        out.close();
    }

    @Nonnull
    private String extractPathKey(@Nonnull HttpExchange exchange) {
        String fullPath = exchange.getRequestURI().getPath();
        if (fullPath.endsWith("/")) {
            fullPath = fullPath.substring(0, fullPath.length() - 1);
        }
        String[] pathParts = fullPath.split("/");
        if (pathParts.length == 0) {
            return "";
        }
        return pathParts[pathParts.length - 1];
    }

    private void sendError(
            int errorCode, @Nonnull String errorMessage, @Nonnull HttpExchange exchange) throws IOException {
        sendResponse(errorCode, MessageFormat.format(errorPageTemplate, errorMessage), exchange);
    }

    private void sendMessageForm(
            @Nonnull HttpExchange exchange, @Nonnull TextMessage originalMessage) throws IOException {
        var originalSender = originalMessage.getAddress();
        var escapedMessageText = StringEscapeUtils.escapeHtml4(originalMessage.getText());
        sendResponse(HttpURLConnection.HTTP_OK
                , MessageFormat.format(formPageTemplate, originalSender, escapedMessageText), exchange
        );
    }

    private void sendResponse(
            int responseCode, @Nonnull String response, @Nonnull HttpExchange exchange) throws IOException {
        var responseBytes = response.getBytes(StandardCharsets.UTF_8);
        var out = exchange.getResponseBody();
        var headers = exchange.getResponseHeaders();
        headers.add("Content-Type", "text/html");
        exchange.sendResponseHeaders(responseCode, responseBytes.length);
        out.write(responseBytes);
        out.flush();
        out.close();
    }

}
