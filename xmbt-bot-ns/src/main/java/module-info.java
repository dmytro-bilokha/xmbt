import com.dmytrobilokha.xmbt.api.bot.BotFactory;
import com.dmytrobilokha.xmbt.bot.ns.NsBotFactory;

module xmbt.bot.ns {
    exports com.dmytrobilokha.xmbt.bot.ns.config to xmbt.main;
    exports com.dmytrobilokha.xmbt.bot.ns.dto;
    provides BotFactory with NsBotFactory;
    requires xmbt.main;
    requires jsr305;
    requires java.net.http;
    requires jakarta.json.bind;
    requires org.apache.httpcomponents.client5.httpclient5;
    requires org.apache.httpcomponents.core5.httpcore5;
}
