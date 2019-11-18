package com.dmytrobilokha.xmbt.xmpp;

import com.dmytrobilokha.xmbt.config.InvalidConfigException;
import com.dmytrobilokha.xmbt.config.property.ConfigProperty;

import javax.annotation.Nonnull;
import java.util.Map;

public class XmppUsernameProperty extends ConfigProperty {

    public XmppUsernameProperty(@Nonnull Map<String, String> allProperties) throws InvalidConfigException {
        super("xmpp.username", allProperties);
    }

}
