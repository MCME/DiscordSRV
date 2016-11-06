package com.scarsz.discordsrv;

import com.google.gson.Gson;
import com.scarsz.discordsrv.events.PlatformChatEvent;
import com.scarsz.discordsrv.events.results.PlatformChatProcessResult;
import com.scarsz.discordsrv.objects.IPlatform;
import com.scarsz.discordsrv.todo.objects.AccountLinkManager;
import com.scarsz.discordsrv.todo.threads.ChannelTopicUpdater;
import com.scarsz.discordsrv.todo.threads.ConsoleMessageQueueWorker;
import com.scarsz.discordsrv.todo.threads.ServerLogWatcher;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.JDABuilder;
import net.dv8tion.jda.entities.Channel;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.utils.SimpleLog;

import javax.security.auth.login.LoginException;
import java.util.*;

public class Manager {

    public static final double version = 13.0;

    public HashMap<String, Object> config = new HashMap<>();
    public boolean configGetBoolean(String key) { return (boolean) config.get(key); }
    public int configGetInt(String key) {
        return (int) config.get(key);
    }
    public String configGetString(String key) {
        return (String) config.get(key);
    }

    public final Gson gson = new Gson();
    public JDA jda = null;
    public final IPlatform platform;
    public final long startTime = System.nanoTime();
    public boolean updateIsAvailable = false;

    // channels
    public final HashMap<String, TextChannel> channels = new HashMap<>();
    public TextChannel mainChatChannel;
    public TextChannel consoleChannel;

    // plugin hooks
    private final List<String> hookedPlugins = new ArrayList<>();

    // account linking
    public AccountLinkManager accountLinkManager;
    private final HashMap<String, UUID> linkingCodes = new HashMap<>();

    // threads
    public ChannelTopicUpdater channelTopicUpdater = null;
    public ConsoleMessageQueueWorker consoleMessageQueueWorker = null;
    public ServerLogWatcher serverLogWatcher = null;

    // iterable misc variables
    public final Map<String, String> colors = new HashMap<>();
    public final List<String> consoleMessageQueue = new LinkedList<>();
    public final Map<String, String> responses = new HashMap<>();
    public final List<String> unsubscribedPlayers = new ArrayList<>();

    public Manager(IPlatform platform) {
        this.platform = platform;
        platform.info("Platform: " + platform.getClass().toString().replace("Platform", ""));

        initialize();
    }

    /**
     * Initialize the manager excluding JDA
     */
    public void initialize() {
        System.out.println("Initializing DiscordSRV v13");
    }
    /**
     * Shut down the DiscordSRV manager safely
     */
    public void shutdown() {
        // disconnect from Discord
        if (jda != null) jda.shutdown(false);
    }

    public void buildJDA(String botToken) {
        System.out.println("Initializing JDA");

        // murder previously started JDA
        if (jda != null) try { platform.info("Murdering previous JDA instance"); jda.shutdown(false); } catch (Exception e) { e.printStackTrace(); }
        jda = null;

        // set log level
        SimpleLog.LEVEL = SimpleLog.Level.WARNING;
        SimpleLog.addListener(new SimpleLog.LogListener() {
            @Override
            public void onLog(SimpleLog simpleLog, SimpleLog.Level level, Object o) {
                if (level == SimpleLog.Level.INFO)
                    platform.info("[JDA] " + o);
                else if (level == SimpleLog.Level.WARNING)
                    platform.warning("[JDA] " + o);
                else if (level == SimpleLog.Level.FATAL)
                    platform.severe("[JDA] " + o);
            }
            @Override
            public void onError(SimpleLog simpleLog, Throwable throwable) {}
        });

        // build JDA
        try {
            jda = new JDABuilder()
                    .setBotToken(botToken)
                    .setAutoReconnect(true)
                    .setAudioEnabled(false)
                    .setBulkDeleteSplittingEnabled(false)
                    .buildBlocking();
        } catch (LoginException | InterruptedException e) {
            e.printStackTrace();
        }

        // shut down platform if JDA wasn't built
        if (jda == null) {
            platform.severe("DiscordSRV failed to build JDA. Check the \"caused by\" lines above.");
            platform.disablePlatform();
            return;
        }

        // print the text channels that the bot can see
        for (Guild server : jda.getGuilds()) {
            platform.info("Found guild " + server);
            for (Channel channel : server.getTextChannels()) platform.info("- " + channel);
        }

        // check & get location info
        mainChatChannel = getTextChannelFromChannelName(configGetString("DiscordMainChatChannel"));
        consoleChannel = jda.getTextChannelById(configGetString("DiscordConsoleChannelId"));

        if (mainChatChannel == null) platform.warning("Specified main chat channel from channels.json could not be found (is it's name set to \"" + configGetString("DiscordMainChatChannel") + "\"?)");
        if (consoleChannel == null) platform.warning("Specified console channel from config could not be found");
        if (mainChatChannel == null && consoleChannel == null) {
            platform.severe("Chat and console channels are both unavailable, disabling");
            platform.disablePlatform();
            return;
        }

        // game status
        if (!configGetString("DiscordGameStatus").isEmpty()) {
            jda.getAccountManager().setGame(configGetString("DiscordGameStatus"));
        }

        // send startup message if enabled
        if (configGetBoolean("DiscordChatChannelServerStartupMessageEnabled"))
            sendMessage(mainChatChannel, configGetString("DiscordChatChannelServerStartupMessage"));
    }

    /**
     * Process the given chat event for delivery to Discord
     * @return result of processing the given PlatformChatEvent
     * @see PlatformChatEvent
     * @see PlatformChatProcessResult
     */
    public PlatformChatProcessResult processChatEvent(PlatformChatEvent event) {
        return new PlatformChatProcessResult();
    }

    //<editor-fold desc="Utilities">
    public void debug(String message) {
        platform.info("[DEBUG @ " + String.valueOf(Thread.currentThread().getStackTrace()[2]) + "] " + message);
    }
    private TextChannel getTextChannelFromChannelName(String inGameChannelName) {
        for (Map.Entry<String, TextChannel> linkedChannel : channels.entrySet())
            if (linkedChannel.getKey().equals(inGameChannelName)) return linkedChannel.getValue();
        return null;
    }
    public void sendMessage(TextChannel channel, String message) {
        if (jda == null) {
            debug("JDA null");
            return;
        }
        if (channel == null) {
            debug("Channel null");
        }
        //debug("yeah hi");
    }
    //</editor-fold>

}
