import com.dmytrobilokha.xmbt.api.bot.BotFactory;

module xmbt.main {
    uses BotFactory;
    exports com.dmytrobilokha.xmbt.api.messaging;
    exports com.dmytrobilokha.xmbt.api.bot;
    exports com.dmytrobilokha.xmbt.api.service;
    exports com.dmytrobilokha.xmbt.api.service.dictionary;
    exports com.dmytrobilokha.xmbt.api.service.config;
    exports com.dmytrobilokha.xmbt.api.service.persistence;
    requires java.sql;
    requires slf4j.api;
    requires logback.core;
    requires logback.classic;
    requires com.zaxxer.hikari;
    requires org.flywaydb.core;
    requires smack.core;
    requires smack.tcp;
    requires smack.im;
    requires smack.extensions;
    requires jxmpp.jid;
    requires jxmpp.core;
    requires jsr305;
}