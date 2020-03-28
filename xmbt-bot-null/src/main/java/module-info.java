import com.dmytrobilokha.xmbt.api.bot.BotFactory;
import com.dmytrobilokha.xmbt.bot.nullbot.NullBotFactory;

module xmbt.bot.nullbot {
    provides BotFactory with NullBotFactory;
    requires xmbt.main;
    requires jsr305;
    requires slf4j.api;
}
