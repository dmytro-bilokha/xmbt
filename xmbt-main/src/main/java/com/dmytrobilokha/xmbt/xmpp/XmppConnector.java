package com.dmytrobilokha.xmbt.xmpp;

import com.dmytrobilokha.xmbt.api.messaging.TextMessage;
import com.dmytrobilokha.xmbt.config.ConfigServiceImpl;
import com.dmytrobilokha.xmbt.manager.BotRegistry;
import com.dmytrobilokha.xmbt.manager.ConnectionException;
import com.dmytrobilokha.xmbt.manager.InvalidAddressException;
import com.dmytrobilokha.xmbt.manager.InvalidConnectionStateException;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;

public class XmppConnector {

    private static final Logger LOG = LoggerFactory.getLogger(XmppConnector.class);

    @Nonnull
    private final ConfigServiceImpl configService;
    @Nonnull
    private final BotRegistry botRegistry;
    @CheckForNull
    private AbstractXMPPConnection connection;

    public XmppConnector(@Nonnull ConfigServiceImpl configService, @Nonnull BotRegistry botRegistry) {
        this.configService = configService;
        this.botRegistry = botRegistry;
    }

    public void connect() throws ConnectionException, InterruptedException {
        if (connection != null) {
            throw new ConnectionException("Unable to connect, connection has been established already");
        }
        String username = configService.getProperty(XmppUsernameProperty.class).getStringValue();
        String password = configService.getProperty(XmppPasswordProperty.class).getStringValue();
        String server = configService.getProperty(XmppServerProperty.class).getStringValue();
        try {
            connection = new XMPPTCPConnection(username, password, server);
        } catch (XmppStringprepException ex) {
            throw new ConnectionException("Failed to create XMPP connection to server '"
                + server + "' with username '" + username + "'", ex);
        }
        try {
            connection.connect().login();
            Roster.getInstanceFor(connection).setSubscriptionMode(Roster.SubscriptionMode.accept_all);
            DeliveryReceiptManager.getInstanceFor(connection).autoAddDeliveryReceiptRequests();
            var presence = new Presence(Presence.Type.available);
            presence.setStatus("Up and running");
            presence.setPriority(24);
            presence.setMode(Presence.Mode.available);
            connection.sendStanza(presence);
        } catch (IOException ex) {
            throw new ConnectionException(
                    "I/O exception during trying to connect to XMPP server '" + server + "'", ex);
        } catch (SmackException | XMPPException ex) {
            throw new ConnectionException(
                    "Unable to connect to XMPP server '" + server + "' with username '" + username + "'", ex);
        }
        connection.addAsyncStanzaListener(new AsyncStanzaListener(botRegistry), StanzaTypeFilter.MESSAGE);
    }

    public void sendMessage(@Nonnull TextMessage message)
            throws InvalidAddressException, InvalidConnectionStateException {
        if (connection == null) {
            throw new InvalidConnectionStateException("Unable to send a message " + message
                + ", because XMPP connection hasn't been established");
        }
        LOG.debug("Got message to send {}", message);
        Message xmppMessage;
        try {
            xmppMessage = new Message(JidCreate.from(message.getAddress()), message.getText());
            xmppMessage.setType(Message.Type.chat);
        } catch (XmppStringprepException ex) {
            throw new InvalidAddressException("Unable to create XMPP address from '" + message.getAddress() + "'", ex);
        }
        connection.sendAsync(xmppMessage, responseStanza -> true);
    }

    public void disconnect() {
        if (connection != null && connection.isConnected()) {
            LOG.info("Disconnecting XMPP...");
            connection.disconnect();
        }
        connection = null; //NOPMD
    }

    public void ensureConnected() throws ConnectionException, InterruptedException {
        if (connection == null) {
            connect();
        } else if (!connection.isConnected()) {
            int pause = configService.getProperty(XmppReconnectPauseSeedProperty.class).getValue();
            int maxTries = configService.getProperty(XmppReconnectTrialsProperty.class).getValue();
            int totalWaitTime = 0;
            int trial;
            for (trial = 1; trial <= maxTries && !connection.isConnected(); trial++) {
                Thread.sleep(pause * 1000);
                totalWaitTime += pause;
                pause *= 1.5; //Increase pause exponentially
                LOG.info("Trying to reconnect to the XMPP server. Try number {}", trial);
                try {
                    connection.connect().login();
                } catch (XMPPException | SmackException | IOException ex) {
                    LOG.warn("Reconnection try {} failed", trial, ex);
                }
            }
            if (connection.isConnected()) {
                LOG.info("Reconnection try {} seems to be successful. Total wait time is {} seconds"
                        , trial, totalWaitTime);
                return;
            }
            throw new ConnectionException("Failed to reconnect to the XMPP server after " + trial
                    + " tries and total waiting time of " + totalWaitTime + " seconds");
        }
    }

}
