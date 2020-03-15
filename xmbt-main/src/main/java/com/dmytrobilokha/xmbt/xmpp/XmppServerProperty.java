package com.dmytrobilokha.xmbt.xmpp;

import com.dmytrobilokha.xmbt.config.InvalidConfigException;
import com.dmytrobilokha.xmbt.config.property.ConfigProperty;

import javax.annotation.Nonnull;
import java.util.Map;

public class XmppServerProperty extends ConfigProperty {

    public XmppServerProperty(@Nonnull Map<String, String> allProperties) throws InvalidConfigException {
        super("xmpp.server", allProperties);
    }

}
