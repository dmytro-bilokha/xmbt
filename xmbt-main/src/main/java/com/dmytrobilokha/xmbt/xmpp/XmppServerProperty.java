package com.dmytrobilokha.xmbt.xmpp;

import com.dmytrobilokha.xmbt.api.service.config.ConfigProperty;
import com.dmytrobilokha.xmbt.api.service.config.InvalidConfigException;

import javax.annotation.Nonnull;
import java.util.Map;

public class XmppServerProperty extends ConfigProperty {

    public XmppServerProperty(@Nonnull Map<String, String> allProperties) throws InvalidConfigException {
        super("xmpp.server", allProperties);
    }

}
