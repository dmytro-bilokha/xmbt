package com.dmytrobilokha.xmbt.xmpp;

import com.dmytrobilokha.xmbt.config.InvalidConfigException;
import com.dmytrobilokha.xmbt.config.property.ConfigProperty;

import javax.annotation.Nonnull;
import java.util.Properties;

class XmppPasswordProperty extends ConfigProperty {

    protected XmppPasswordProperty(@Nonnull Properties allProperties) throws InvalidConfigException {
        super("xmpp.password", allProperties);
    }

}
