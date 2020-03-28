import com.dmytrobilokha.xmbt.api.bot.BotFactory;
import com.dmytrobilokha.xmbt.bot.sysinfo.SysinfoBotFactory;

module xmbt.bot.sysinfo {
    provides BotFactory with SysinfoBotFactory;
    requires xmbt.main;
    requires jsr305;
    requires slf4j.api;
}