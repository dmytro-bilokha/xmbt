package com.dmytrobilokha.xmbt.xmpp;

import com.dmytrobilokha.xmbt.config.InvalidConfigException;
import com.dmytrobilokha.xmbt.config.property.ConfigProperty;

import javax.annotation.Nonnull;
import java.util.Properties;

class XmppUsernameProperty extends ConfigProperty {

    protected XmppUsernameProperty(@Nonnull Properties allProperties) throws InvalidConfigException {
        super("xmpp.username", allProperties);
    }

}
