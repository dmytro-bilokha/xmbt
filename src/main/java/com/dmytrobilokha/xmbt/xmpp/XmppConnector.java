package com.dmytrobilokha.xmbt.xmpp;

import com.dmytrobilokha.xmbt.config.ConfigPropertyProducer;
import com.dmytrobilokha.xmbt.config.ConfigService;
import com.dmytrobilokha.xmbt.manager.ConnectionException;
import com.dmytrobilokha.xmbt.manager.InvalidAddressException;
import com.dmytrobilokha.xmbt.manager.InvalidConnectionStateException;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class XmppConnector {

    private static final Logger LOG = LoggerFactory.getLogger(XmppConnector.class);

    @Nonnull
    private final ConfigService configService;
    @CheckForNull
    private AbstractXMPPConnection connection;
    @CheckForNull
    private BlockingQueue<TextMessage> incomingMessagesQueue;

    public XmppConnector(@Nonnull ConfigService configService) {
        this.configService = configService;
    }

    @Nonnull
    public static List<ConfigPropertyProducer> getPropertyProducers() {
        return List.of(
                XmppUsernameProperty::new
                , XmppPasswordProperty::new
                , XmppServerProperty::new
        );
    }

    public void setIncomingMessagesQueue(@Nonnull BlockingQueue<TextMessage> incomingMessagesQueue) {
        this.incomingMessagesQueue = incomingMessagesQueue;
    }

    public void connect() throws ConnectionException, InterruptedException {
        //TODO: make this nicer
        if (incomingMessagesQueue == null) {
            throw new IllegalStateException("No incoming messages queue found!");
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
        } catch (IOException ex) {
            throw new ConnectionException(
                    "I/O exception during trying to connect to XMPP server '" + server + "'", ex);
        } catch (SmackException | XMPPException ex) {
            throw new ConnectionException(
                    "Unable to connect to XMPP server '" + server + "' with username '" + username + "'", ex);
        }
        //TODO: make it separate class, not inner
        connection.addAsyncStanzaListener(new StanzaListener() {
            //TODO: add auto accept for roaster invites
            @Override
            public void processStanza(Stanza stanza) throws InterruptedException {
                if (!(stanza instanceof Message)) {
                    return;
                }
                //TODO: make this nicer
                if (incomingMessagesQueue == null) {
                    throw new IllegalStateException("No incoming messages queue found!");
                }
                Message xmppMessage = (Message) stanza;
                LOG.debug("Got stanza in listener {}", stanza);
                if (xmppMessage.getBody() == null) {
                    LOG.debug("Ignoring stanza message {}, because its body is null", xmppMessage);
                    return;
                }
                TextMessage messageFromUser = new TextMessage(xmppMessage.getFrom().toString(), xmppMessage.getBody());
                LOG.debug("Putting to queue {}", messageFromUser);
                incomingMessagesQueue.put(messageFromUser);
            }
        }, StanzaTypeFilter.MESSAGE);

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
        } catch (XmppStringprepException ex) {
            throw new InvalidAddressException("Unable to create XMPP address from '" + message.getAddress() + "'", ex);
        }
        connection.sendAsync(xmppMessage, responseStanza -> true);
    }

    public void disconnect() {
        if (connection != null) {
            connection.disconnect();
        }
    }

}
