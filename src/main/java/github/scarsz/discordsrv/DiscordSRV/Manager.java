package github.scarsz.discordsrv.DiscordSRV;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import github.scarsz.discordsrv.DiscordSRV.api.DiscordSRVListener;
import github.scarsz.discordsrv.DiscordSRV.api.events.GameChatMessagePreProcessEvent;
import github.scarsz.discordsrv.DiscordSRV.objects.Config;
import github.scarsz.discordsrv.DiscordSRV.objects.PlatformType;
import github.scarsz.discordsrv.DiscordSRV.platforms.Platform;
import github.scarsz.discordsrv.DiscordSRV.threads.ChannelTopicUpdater;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.utils.SimpleLog;

import javax.security.auth.login.LoginException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Made by Scarsz
 *
 * @in /dev/hell
 * @at 11/7/2016
 */
public class Manager {

    public static Manager instance;
    public Platform platform;
    public PlatformType platformType;
    public Manager(Platform platform) {
        Manager.instance = this;
        this.platform = platform;

        switch (platform.getClass().getSimpleName().replace("Platform", "").toLowerCase()) {
            // in order of least to most hated
            case "bukkit": this.platformType = PlatformType.BUKKIT; break;
            case "bungeecord": this.platformType = PlatformType.BUNGEECORD; break;
            case "sponge": this.platformType = PlatformType.SPONGE; break;
            default: platform.severe("Could not determine platform. Tell Scarsz to fix this case he's a retard."); this.platformType = PlatformType.BUKKIT;
        }
    }

    public static DecimalFormat decimalFormat = new DecimalFormat("#.#");

    public Config config = new Config();
    public Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public JDA jda = null;
    public long startTime = System.currentTimeMillis();

    public TextChannel chatChannel; //TODO
    public TextChannel consoleChannel; //TODO
    private List<DiscordSRVListener> listeners = new ArrayList<>();
    public ChannelTopicUpdater channelTopicUpdater = new ChannelTopicUpdater();

    public void initialize() {
        // send the config File to the Config
        config.configFile = platform.getPluginConfigFile();

        // shutdown JDA if it was already running (plugin reload? 🤦)
        if (jda != null) jda.shutdown(false);


        SimpleLog.LEVEL = SimpleLog.Level.WARNING;
        SimpleLog.addListener(new SimpleLog.LogListener() {
            @Override
            public void onLog(SimpleLog simpleLog, SimpleLog.Level level, Object o) {
                switch (level) {
                    case INFO:
                        platform.info("[JDA] " + o);
                        break;
                    case WARNING:
                        platform.warning("[JDA] " + o);
                        break;
                    case FATAL:
                        platform.severe("[JDA] " + o);
                        break;
                    default:
                        platform.info("[JDA " + level.name().toUpperCase() + "] " + o);
                        break;
                }
            }
            @Override
            public void onError(SimpleLog simpleLog, Throwable throwable) {}
        });

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

        // start channel topic updater if not already
        if (channelTopicUpdater.getState() == Thread.State.NEW) channelTopicUpdater.start();
        else {
            if (channelTopicUpdater != null) channelTopicUpdater.interrupt();
            channelTopicUpdater = new ChannelTopicUpdater();
            channelTopicUpdater.start();
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
