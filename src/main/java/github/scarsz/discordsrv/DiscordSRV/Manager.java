package github.scarsz.discordsrv.DiscordSRV;

import github.scarsz.discordsrv.DiscordSRV.api.DiscordSRVListener;
import github.scarsz.discordsrv.DiscordSRV.api.events.GameChatMessagePreProcessEvent;
import github.scarsz.discordsrv.DiscordSRV.objects.Config;
import github.scarsz.discordsrv.DiscordSRV.objects.PlatformType;
import github.scarsz.discordsrv.DiscordSRV.platforms.Platform;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.List;

/**
 * Made by Scarsz
 *
 * @in /dev/hell
 * @at 11/7/2016
 */
public class Manager {

    public static Platform platform;
    public static PlatformType platformType;
    public Manager(Platform platform) {
        Manager.platform = platform;

        switch (platform.getClass().getSimpleName().replace("Platform", "").toLowerCase()) {
            case "bukkit": Manager.platformType = PlatformType.BUKKIT; break;
            case "bungeecord": Manager.platformType = PlatformType.BUNGEECORD; break;
            case "sponge": Manager.platformType = PlatformType.SPONGE; break;
            default: platform.severe("Could not determine platform. Tell Scarsz to fix this case he's a retard."); Manager.platformType = PlatformType.BUKKIT;
        }
    }

    public Config config = new Config();
    public JDA jda = null;

    private List<DiscordSRVListener> listeners = new ArrayList<>();

    public void initialize() {
        // send the config File to the Config
        config.configFile = platform.getPluginConfigFile();

        // shutdown JDA if it was already running (plugin reload)
        if (jda != null) jda.shutdown(false);

        // build JDA
        try {
            jda = new JDABuilder(AccountType.BOT)
                    .setToken(config.getString("BotToken"))
                    .addListener(new DiscordListener())
                    .setAutoReconnect(true)
                    .setAudioEnabled(false)
                    .setBulkDeleteSplittingEnabled(false)
                    .buildBlocking();
        } catch (LoginException | InterruptedException | RateLimitedException e) {
            e.printStackTrace();
        }
    }

    public void processChatEvent(GameChatMessagePreProcessEvent gameChatMessagePreProcessEvent) {
        listeners.forEach(discordSRVListener -> discordSRVListener.gameChatMessagePreProcess(gameChatMessagePreProcessEvent));
    }

    public void addListener(DiscordSRVListener listener) {
        listeners.add(listener);
    }
    public void removeListener(DiscordSRVListener listener) {
        listeners.remove(listener);
    }

}
