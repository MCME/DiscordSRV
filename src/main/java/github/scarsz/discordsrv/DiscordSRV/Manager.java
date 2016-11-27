package github.scarsz.discordsrv.DiscordSRV;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import github.scarsz.discordsrv.DiscordSRV.api.DiscordSRVListener;
import github.scarsz.discordsrv.DiscordSRV.api.Event;
import github.scarsz.discordsrv.DiscordSRV.api.events.GameChatMessagePostProcessEvent;
import github.scarsz.discordsrv.DiscordSRV.api.events.GameChatMessagePreProcessEvent;
import github.scarsz.discordsrv.DiscordSRV.objects.AccountLinkManager;
import github.scarsz.discordsrv.DiscordSRV.objects.Config;
import github.scarsz.discordsrv.DiscordSRV.objects.PlatformType;
import github.scarsz.discordsrv.DiscordSRV.platforms.Platform;
import github.scarsz.discordsrv.DiscordSRV.threads.ChannelTopicUpdater;
import github.scarsz.discordsrv.DiscordSRV.util.DiscordUtil;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.utils.SimpleLog;

import javax.security.auth.login.LoginException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.*;

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

    public static final DecimalFormat decimalFormat = new DecimalFormat("#.#");
    public Map<String, TextChannel> channels = new HashMap<>();
    public Config config = new Config();
    public Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public List<String> hookedPlugins = new ArrayList<>();
    public JDA jda = null;
    public long startTime = System.currentTimeMillis();
    public AccountLinkManager accountLinkManager;
    public Map<String, UUID> linkingCodes = new HashMap<>();

    public TextChannel chatChannel; //TODO
    public TextChannel consoleChannel; //TODO
    public List<DiscordSRVListener> listeners = new ArrayList<>();
    public ChannelTopicUpdater channelTopicUpdater = new ChannelTopicUpdater();

    public void initialize() {
        // send the config File to the Config
        config.configFile = platform.getPluginConfigFile();

        //TODO update check
        //TODO CKC thank yous
        //TODO random phrases

        // shutdown JDA if it was already running (plugin reload? 🤦)
        if (jda != null) jda.shutdown(false);

        // set JDA message logging
        SimpleLog.LEVEL = SimpleLog.Level.OFF;
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
                }
            }
            @Override
            public void onError(SimpleLog simpleLog, Throwable throwable) {}
        });

        // build JDA
        try {
            jda = new JDABuilder(AccountType.BOT)
                    .setToken(config.getString("BotToken")) // set bot token
                    .addListener(new DiscordListener()) // register Discord listener
                    .setAutoReconnect(true) // automatically reconnect to Discord if shit happens
                    .setAudioEnabled(false) // we don't use audio and this not being disabled causes major codec problems on some systems
                    .setBulkDeleteSplittingEnabled(false) // has to be off for JDA not to bitch
                    .setGame(Game.of(config.getString("DiscordGameStatus"))) // set game status
                    .setStatus(OnlineStatus.ONLINE) // set bot as online
                    .buildBlocking(); // build JDA
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

        // print the things the bot can see
        for (Guild guild : jda.getGuilds()) {
            platform.info("Found guild " + guild);
            for (Channel channel : guild.getTextChannels()) {
                platform.info("- " + channel);
            }
        }

        //TODO relocate channels.json to proper location
//        if (!new File(getDataFolder(), "channels.json").exists()) saveResource("channels.json", false);
//        try {
//            for (ChannelInfo<String, String> channel : (List<ChannelInfo<String, String>>) gson.fromJson(FileUtils.readFileToString(new File(getDataFolder(), "channels.json"), Charset.defaultCharset()), new TypeToken<List<ChannelInfo<String, String>>>(){}.getType())) {
//                if (channel == null || channel.channelName() == null || channel.channelId() == null) {
//                    // malformed channels.json
//                    platform.warning("JSON parsing error for " + channel + " \"" + channel.channelName() + "\" \"" + channel.channelId() + "\"");
//                    continue;
//                }
//
//                TextChannel requestedChannel = jda.getTextChannelById(channel.channelId());
//                if (requestedChannel == null) continue;
//                channels.put(channel.channelName(), requestedChannel);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        // check & get location info
        chatChannel = getTextChannelFromChannelName(config.getString("DiscordMainChatChannel"));
        consoleChannel = jda.getTextChannelById(config.getString("DiscordConsoleChannelId"));

        if (chatChannel == null) platform.warning("Specified chat channel from channels.json could not be found (is it's name set to \"" + config.getString("DiscordMainChatChannel") + "\"?)");
        if (consoleChannel == null) platform.warning("Specified console channel from config could not be found");
        if (chatChannel == null && consoleChannel == null) {
            platform.severe("Chat and console channels are both unavailable, plugin will not work");
            return;
        }

        // send startup message if enabled
        if (config.getBoolean("DiscordChatChannelServerStartupMessageEnabled")) DiscordUtil.sendMessage(chatChannel, config.getString("DiscordChatChannelServerStartupMessage"));


    }

    public void shutdown() {
        platform.info("Manager shutting down...");
        jda.getPresence().setStatus(OnlineStatus.INVISIBLE);
        jda.shutdown(false);

        channelTopicUpdater.interrupt();

        accountLinkManager.save();
        config.save();
        platform.info(" done");
    }

    private void processChatEvent(GameChatMessagePreProcessEvent gameChatMessagePreProcessEvent) {
        // broadcast gameChatMessagePreProcessEvent
        listeners.forEach(discordSRVListener -> discordSRVListener.gameChatMessagePreProcess(gameChatMessagePreProcessEvent));

        //TODO actual message processing for game -> Discord

        // broadcast GameChatMessagePostProcessEvent
        GameChatMessagePostProcessEvent gameChatMessagePostProcessEvent = new GameChatMessagePostProcessEvent(gameChatMessagePreProcessEvent.getPlayerName(), gameChatMessagePreProcessEvent.getMessage());
        listeners.forEach(discordSRVListener -> discordSRVListener.gameChatMessagePostProcess(gameChatMessagePostProcessEvent));
    }

    public void processEvent(Event event) {
        for (DiscordSRVListener listener : listeners) {
            try {
                Method eventMethod = listener.getClass().getMethod(event.getClass().getSimpleName().substring(0, 1).toLowerCase() + event.getClass().getSimpleName().substring(1).replace("Event", ""));
                eventMethod.invoke(listener, event);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    public void addListener(DiscordSRVListener listener) {
        listeners.add(listener);
        platform.info("Listener \"" + listener + "\" registered");
    }
    public void removeListener(DiscordSRVListener listener) {
        listeners.remove(listener);
        platform.info("Listener \"" + listener + "\" unregistered");
    }

    //TODO rename
    public TextChannel getTextChannelFromChannelName(String channelName) {
        return channels.get(channelName);
    }

    public boolean chatChannelIsLinked(String channelName) {
        return channels.containsKey(channelName);
    }
}
