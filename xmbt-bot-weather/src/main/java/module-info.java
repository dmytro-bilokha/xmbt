import com.dmytrobilokha.xmbt.api.bot.BotFactory;
import com.dmytrobilokha.xmbt.bot.weather.WeatherBotFactory;

module xmbt.bot.weather {
    exports com.dmytrobilokha.xmbt.bot.weather.config to xmbt.main;
    exports com.dmytrobilokha.xmbt.bot.weather.dto to org.eclipse.yasson;
    provides BotFactory with WeatherBotFactory;
    requires xmbt.main;
    requires jsr305;
    requires java.json.bind;
    requires java.net.http;
}
