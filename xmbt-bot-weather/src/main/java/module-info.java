import com.dmytrobilokha.xmbt.api.bot.BotFactory;
import com.dmytrobilokha.xmbt.bot.weather.WeatherBotFactory;

module xmbt.bot.weather {
    exports com.dmytrobilokha.xmbt.bot.weather.config to xmbt.main;
    exports com.dmytrobilokha.xmbt.bot.weather.dto;
    provides BotFactory with WeatherBotFactory;
    requires xmbt.main;
    requires jsr305;
    requires jakarta.json.bind;
    requires java.net.http;
    requires org.apache.httpcomponents.client5.httpclient5;
    requires org.apache.httpcomponents.core5.httpcore5;
}
