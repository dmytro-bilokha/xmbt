import com.dmytrobilokha.xmbt.api.bot.BotFactory;
import com.dmytrobilokha.xmbt.bot.ns.NsBotFactory;

module xmbt.bot.ns {
    exports com.dmytrobilokha.xmbt.bot.ns.config to xmbt.main;
    exports com.dmytrobilokha.xmbt.bot.ns.dto to org.eclipse.yasson;
    provides BotFactory with NsBotFactory;
    requires xmbt.main;
    requires jsr305;
    requires java.json.bind;
    requires java.net.http;
    requires httpclient5;
    requires httpcore5;
}
