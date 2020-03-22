module xmbt.main {
    uses com.dmytrobilokha.xmbt.api.BotFactory;
    exports com.dmytrobilokha.xmbt.api;
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