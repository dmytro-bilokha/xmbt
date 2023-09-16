import com.dmytrobilokha.xmbt.api.bot.BotFactory;

module xmbt.main {
    uses BotFactory;
    exports com.dmytrobilokha.xmbt.api.messaging;
    exports com.dmytrobilokha.xmbt.api.bot;
    exports com.dmytrobilokha.xmbt.api.service;
    exports com.dmytrobilokha.xmbt.api.service.dictionary;
    exports com.dmytrobilokha.xmbt.api.service.config;
    exports com.dmytrobilokha.xmbt.api.service.persistence;
    requires transitive java.sql;
    requires transitive org.slf4j;
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;
    requires com.zaxxer.hikari;
    requires smack.core;
    requires smack.tcp;
    requires smack.im;
    requires smack.extensions;
    requires jxmpp.jid;
    requires jxmpp.core;
    requires jsr305;
}