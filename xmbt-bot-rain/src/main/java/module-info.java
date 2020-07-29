import com.dmytrobilokha.xmbt.api.bot.BotFactory;
import com.dmytrobilokha.xmbt.bot.rain.RainBotFactory;

module xmbt.bot.rain {
    provides BotFactory with RainBotFactory;
    requires xmbt.main;
    requires jsr305;
    requires java.net.http;
}
