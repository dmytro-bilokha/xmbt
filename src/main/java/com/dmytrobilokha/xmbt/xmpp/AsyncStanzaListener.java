package com.dmytrobilokha.xmbt.xmpp;

import com.dmytrobilokha.xmbt.api.TextMessage;
import com.dmytrobilokha.xmbt.manager.BotRegistry;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

class AsyncStanzaListener implements StanzaListener {

    private static final Logger LOG = LoggerFactory.getLogger(AsyncStanzaListener.class);

    @Nonnull
    private final BotRegistry botRegistry;

    AsyncStanzaListener(@Nonnull BotRegistry botRegistry) {
        this.botRegistry = botRegistry;
    }

    @Override
    public void processStanza(Stanza stanza) throws InterruptedException {
        if (!(stanza instanceof Message)) {
            return;
        }
        Message xmppMessage = (Message) stanza;
        LOG.debug("Got stanza in listener {}", stanza);
        if (xmppMessage.getBody() == null) {
            LOG.debug("Ignoring stanza message {}, because its body is null", xmppMessage);
            return;
        }
        TextMessage messageFromUser = new TextMessage(extractSenderAddress(xmppMessage), xmppMessage.getBody());
        LOG.debug("Putting to queue {}", messageFromUser);
        botRegistry.enqueueMessageFromUser(messageFromUser);
    }

    @Nonnull
    private String extractSenderAddress(@Nonnull Message xmppMessage) {
        String fullSenderAddress = xmppMessage.getFrom().toString();
        int separatorIndex = fullSenderAddress.indexOf('/');
        if (separatorIndex < 1) {
            return fullSenderAddress;
        }
        return fullSenderAddress.substring(0, separatorIndex);
    }

}
