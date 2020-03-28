import com.dmytrobilokha.xmbt.api.bot.BotFactory;
import com.dmytrobilokha.xmbt.bot.echo.EchoBotFactory;

module xmbt.bot.echo {
    provides BotFactory with EchoBotFactory;
    requires xmbt.main;
    requires jsr305;
}
