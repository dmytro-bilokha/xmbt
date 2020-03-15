package com.dmytrobilokha.xmbt.xmpp;

import com.dmytrobilokha.xmbt.config.InvalidConfigException;
import com.dmytrobilokha.xmbt.config.property.AbstractIntProperty;

import javax.annotation.Nonnull;
import java.util.Map;

public class XmppReconnectPauseSeedProperty extends AbstractIntProperty {

    public XmppReconnectPauseSeedProperty(@Nonnull Map<String, String> allProperties) throws InvalidConfigException {
        super("xmpp.reconnect.pause.seed", allProperties, 10);
    }

}
