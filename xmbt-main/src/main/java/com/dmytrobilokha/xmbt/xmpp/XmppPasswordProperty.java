package com.dmytrobilokha.xmbt.xmpp;

import com.dmytrobilokha.xmbt.api.service.config.ConfigProperty;
import com.dmytrobilokha.xmbt.api.service.config.InvalidConfigException;

import javax.annotation.Nonnull;
import java.util.Map;

public class XmppPasswordProperty extends ConfigProperty {

    public XmppPasswordProperty(@Nonnull Map<String, String> allProperties) throws InvalidConfigException {
        super("xmpp.password", allProperties);
    }

}
