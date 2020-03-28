import com.dmytrobilokha.xmbt.api.bot.BotFactory;

module xmbt.main {
    uses BotFactory;
    exports com.dmytrobilokha.xmbt.api.messaging;
    exports com.dmytrobilokha.xmbt.api.bot;
    exports com.dmytrobilokha.xmbt.api.service;
    requires java.sql;
    requires slf4j.api;
    requires java.json.bind;
    requires java.net.http;
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