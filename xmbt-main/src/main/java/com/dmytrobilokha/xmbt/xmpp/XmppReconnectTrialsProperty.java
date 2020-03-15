package com.dmytrobilokha.xmbt.xmpp;

import com.dmytrobilokha.xmbt.config.InvalidConfigException;
import com.dmytrobilokha.xmbt.config.property.AbstractIntProperty;

import javax.annotation.Nonnull;
import java.util.Map;

public class XmppReconnectTrialsProperty extends AbstractIntProperty {

    public XmppReconnectTrialsProperty(@Nonnull Map<String, String> allProperties) throws InvalidConfigException {
        super("xmpp.reconnect.trials", allProperties, 20);
    }

}
