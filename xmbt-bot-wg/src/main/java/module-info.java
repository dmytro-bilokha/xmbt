import com.dmytrobilokha.xmbt.api.bot.BotFactory;
import com.dmytrobilokha.xmbt.bot.webgateway.WebGatewayBotFactory;

module xmbt.bot.webgateway {
    exports com.dmytrobilokha.xmbt.bot.webgateway.config to xmbt.main;
    provides BotFactory with WebGatewayBotFactory;
    requires xmbt.main;
    requires jsr305;
    requires jdk.httpserver;
    requires org.apache.commons.text;
}
