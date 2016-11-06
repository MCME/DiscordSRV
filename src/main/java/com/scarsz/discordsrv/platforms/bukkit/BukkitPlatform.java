package com.scarsz.discordsrv.platforms.bukkit;

import com.google.common.io.Files;
import com.google.gson.internal.LinkedTreeMap;
import com.scarsz.discordsrv.Manager;
import com.scarsz.discordsrv.events.PlatformChatEvent;
import com.scarsz.discordsrv.events.results.PlatformChatProcessResult;
import com.scarsz.discordsrv.objects.IPlatform;
import com.scarsz.discordsrv.todo.hooks.chat.*;
import com.scarsz.discordsrv.todo.listeners.AchievementListener;
import com.scarsz.discordsrv.todo.listeners.ChatListener;
import com.scarsz.discordsrv.todo.listeners.PlayerDeathListener;
import com.scarsz.discordsrv.todo.listeners.PlayerJoinLeaveListener;
import com.scarsz.discordsrv.todo.objects.AccountLinkManager;
import com.scarsz.discordsrv.todo.objects.CancellationDetector;
import com.scarsz.discordsrv.todo.objects.ConsoleAppender;
import com.scarsz.discordsrv.todo.objects.Lag;
import com.scarsz.discordsrv.todo.threads.ChannelTopicUpdater;
import com.scarsz.discordsrv.todo.threads.ConsoleMessageQueueWorker;
import com.scarsz.discordsrv.todo.threads.ServerLogWatcher;
import com.scarsz.discordsrv.util.HttpUtil;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.events.Event;
import net.dv8tion.jda.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

@SuppressWarnings({"Duplicates", "unchecked"})
public class BukkitPlatform extends JavaPlugin implements IPlatform, Listener {

    private Manager manager = new Manager(this);
    public static Plugin plugin;

    private CancellationDetector<AsyncPlayerChatEvent> cancellationDetector = new CancellationDetector<>(AsyncPlayerChatEvent.class);
    private boolean canUsePingNotificationSounds = false;

    //<editor-fold desc="JavaPlugin overrides">
    public void onEnable() {
        plugin = this;

        Bukkit.getPluginManager().registerEvents(this, this);
        loadConfigToManager();

        //<editor-fold desc="load config, create if doesn't exist, update config if old">
        if (!new File(getDataFolder(), "colors.json").exists()) saveResource("colors.json", false);
        if (!new File(getDataFolder(), "channels.json").exists()) saveResource("channels.json", false);
        if (!new File(getDataFolder(), "config.yml").exists()) {
            saveResource("config.yml", false);
            reloadConfig();
        }
        if (getConfig().getDouble("ConfigVersion") < Double.parseDouble(getDescription().getVersion()) || !getConfig().isSet("ConfigVersion"))
            try {
                //Files.move(new File(getDataFolder(), "config.yml"), new File(getDataFolder(), "config.yml-build." + getConfig().getDouble("ConfigVersion") + ".old"));
                getLogger().info("Your DiscordSRV config file was outdated; attempting migration...");

                File config = new File(getDataFolder(), "config.yml");
                File oldConfig = new File(getDataFolder(), "config.yml-build." + getConfig().getDouble("ConfigVersion") + ".old");
                Files.move(config, oldConfig);
                if (!new File(getDataFolder(), "config.yml").exists()) saveResource("config.yml", false);

                Scanner s1 = new Scanner(oldConfig);
                ArrayList<String> oldConfigLines = new ArrayList<>();
                while (s1.hasNextLine()) oldConfigLines.add(s1.nextLine());
                s1.close();

                Scanner s2 = new Scanner(config);
                ArrayList<String> newConfigLines = new ArrayList<>();
                while (s2.hasNextLine()) newConfigLines.add(s2.nextLine());
                s2.close();

                Map<String, String> oldConfigMap = new HashMap<>();
                for (String line : oldConfigLines) {
                    if (line.startsWith("#") || line.startsWith("-") || line.isEmpty()) continue;
                    List<String> lineSplit = new ArrayList<>();
                    Collections.addAll(lineSplit, line.split(": +|:"));
                    if (lineSplit.size() < 2) continue;
                    String key = lineSplit.get(0);
                    lineSplit.remove(0);
                    String value = String.join(": ", lineSplit);
                    oldConfigMap.put(key, value);
                }

                Map<String, String> newConfigMap = new HashMap<>();
                for (String line : newConfigLines) {
                    if (line.startsWith("#") || line.startsWith("-") || line.isEmpty()) continue;
                    List<String> lineSplit = new ArrayList<>();
                    Collections.addAll(lineSplit, line.split(": +|:"));
                    if (lineSplit.size() >= 2) newConfigMap.put(lineSplit.get(0), lineSplit.get(1));
                }

                for (String key : oldConfigMap.keySet()) {
                    if (newConfigMap.containsKey(key) && !key.startsWith("ConfigVersion")) {
                        String oldKey = oldConfigMap.get(key);
                        if (key.toLowerCase().equals("bottoken")) oldKey = "OMITTED";
                        getLogger().info("Migrating config option " + key + " with value " + oldKey + " to new config");
                        newConfigMap.put(key, oldConfigMap.get(key));
                    }
                }

                for (String line : newConfigLines) {
                    if (line.startsWith("#") || line.startsWith("ConfigVersion")) continue;
                    String key = line.split(":")[0];
                    if (oldConfigMap.containsKey(key))
                        newConfigLines.set(newConfigLines.indexOf(line), key + ": " + oldConfigMap.get(key));
                }

                BufferedWriter writer = new BufferedWriter(new FileWriter(config));
                for (String line : newConfigLines) writer.write(line + System.lineSeparator());
                writer.flush();
                writer.close();

                getLogger().info("Migration complete. Note: migration does not apply to config options that are multiple-line lists.");
                reloadConfig();
            } catch (IOException ignored) { }
        if (!new File(getDataFolder(), "config.yml").exists()) saveResource("config.yml", false);
        reloadConfig();
        //</editor-fold>

        //<editor-fold desc="update check">
        if (!getConfig().getBoolean("UpdateCheckDisabled")) {
            try {
                double latest = Double.parseDouble(HttpUtil.requestHttp("https://raw.githubusercontent.com/Scarsz/DiscordSRV/master/latestbuild"));
                if (latest > Double.parseDouble(getDescription().getVersion())) {
                    getLogger().warning(System.lineSeparator() + System.lineSeparator() + "The current build of DiscordSRV is outdated! Get build " + latest + " at http://dev.bukkit.org/bukkit-plugins/discordsrv/" + System.lineSeparator() + System.lineSeparator());
                    manager.updateIsAvailable = true;
                }

                double minimum = Double.parseDouble(HttpUtil.requestHttp("https://raw.githubusercontent.com/Scarsz/DiscordSRV/master/minimumbuild"));
                if (minimum > Double.parseDouble(getDescription().getVersion())) {
                    getLogger().warning(System.lineSeparator() + System.lineSeparator() + "The current build of DiscordSRV does not meet the minimum! DiscordSRV will not start. Get build " + latest + " at http://dev.bukkit.org/server-mods/discordsrv/" + System.lineSeparator() + System.lineSeparator());
                    Bukkit.getPluginManager().disablePlugin(this);
                    return;
                }
            } catch (IOException e) {
                System.out.println("An error occurred while checking version");
                e.printStackTrace();
            }

            if (!manager.updateIsAvailable) getLogger().info("DiscordSRV is up-to-date. For change logs see the latest file at http://dev.bukkit.org/bukkit-plugins/discordsrv/");
        }
        //</editor-fold>

        //<editor-fold desc="build JDA">
        manager.buildJDA(manager.configGetString("BotToken"));
        //</editor-fold>

        //<editor-fold desc="achievement events">
        getServer().getPluginManager().registerEvents(new AchievementListener(), this);
        //</editor-fold>
        //<editor-fold desc="chat events">
        if (getPlugin("herochat") != null && getConfig().getBoolean("HeroChatHook")) {
            getLogger().info("Enabling Herochat hook");
            getServer().getPluginManager().registerEvents(new HerochatHook(), this);
        } else if (getPlugin("legendchat") != null && getConfig().getBoolean("LegendChatHook")) {
            getLogger().info("Enabling LegendChat hook");
            getServer().getPluginManager().registerEvents(new LegendChatHook(), this);
        } else if (getPlugin("lunachat") != null && getConfig().getBoolean("LunaChatHook")) {
            getLogger().info("Enabling LunaChatHook hook");
            getServer().getPluginManager().registerEvents(new LunaChatHook(), this);
        } else if (getPlugin("towny") != null && getPlugin("townychat") != null && getConfig().getBoolean("TownyChatHook")) {
            getLogger().info("Enabling TownyChatHook hook");
            getServer().getPluginManager().registerEvents(new TownyChatHook(), this);
        } else if (getPlugin("venturechat") != null && getConfig().getBoolean("VentureChatHook")) {
            getLogger().info("Enabling VentureChatHook hook");
            getServer().getPluginManager().registerEvents(new VentureChatHook(), this);
        } else {
            getLogger().info("No chat plugin hooks enabled");
            getServer().getPluginManager().registerEvents(new ChatListener(), this);
        }
        //</editor-fold>
        //<editor-fold desc="death events">
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(), this);
        //</editor-fold>
        //<editor-fold desc="join/leave events">
        getServer().getPluginManager().registerEvents(new PlayerJoinLeaveListener(), this);
        //</editor-fold>

        //<editor-fold desc="TPS poller">
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Lag(), 100L, 1L);
        //</editor-fold>
        //<editor-fold desc="channel topic updater">
        if (manager.channelTopicUpdater == null) {
            manager.channelTopicUpdater = new ChannelTopicUpdater();
            manager.channelTopicUpdater.start();
        }
        //</editor-fold>
        //<editor-fold desc="console streaming to channel">
        if (manager.consoleChannel != null) {
            // kill server log watcher if it's already started
            if (manager.serverLogWatcher != null && !manager.serverLogWatcher.isInterrupted()) {
                manager.serverLogWatcher.interrupt();
                manager.serverLogWatcher = null;
            }

            if (!getConfig().getBoolean("LegacyConsoleChannelEngine")) {
                // attach appender to queue console messages
                Logger rootLogger = (Logger) LogManager.getRootLogger();
                rootLogger.addAppender(new ConsoleAppender());

                // start console message queue worker thread
                if (manager.consoleMessageQueueWorker == null) {
                    manager.consoleMessageQueueWorker = new ConsoleMessageQueueWorker();
                    manager.consoleMessageQueueWorker.start();
                }
            } else {
                // start server log watcher thread
                manager.serverLogWatcher = new ServerLogWatcher();
                manager.serverLogWatcher.start();
            }
        }
        //</editor-fold>

        //<editor-fold desc="enable metrics">
        if (!getConfig().getBoolean("MetricsDisabled"))
            try {
                Metrics metrics = new Metrics(this);
                metrics.start();
            } catch (IOException e) {
                getLogger().warning("Unable to start metrics. Oh well.");
            }
        //</editor-fold>

        //<editor-fold desc="check channel permissions">
        // main chat channel
        if (getConfig().getBoolean("Discordmanager.mainChatChannelDiscordToMinecraft") || getConfig().getBoolean("Discordmanager.mainChatChannelMinecraftToDiscord")) {
            if (manager.mainChatChannel == null) getLogger().warning("Channel " + manager.mainChatChannel + " was not accessible");
            if (manager.mainChatChannel != null && !manager.mainChatChannel.checkPermission(manager.jda.getSelfInfo(), Permission.MESSAGE_WRITE)) getLogger().warning("The bot does not have access to send messages in " + manager.mainChatChannel);
            if (manager.mainChatChannel != null && !manager.mainChatChannel.checkPermission(manager.jda.getSelfInfo(), Permission.MESSAGE_READ)) getLogger().warning("The bot does not have access to read messages in " + manager.mainChatChannel);
        }
        // console channel
        if (manager.consoleChannel != null) {
            if (manager.consoleChannel == null) getLogger().warning("Channel " + manager.consoleChannel + " was not accessible");
            if (manager.consoleChannel != null && !manager.consoleChannel.checkPermission(manager.jda.getSelfInfo(), Permission.MESSAGE_WRITE)) getLogger().warning("The bot does not have access to send messages in " + manager.consoleChannel);
            if (manager.consoleChannel != null && !manager.consoleChannel.checkPermission(manager.jda.getSelfInfo(), Permission.MESSAGE_READ)) getLogger().warning("The bot does not have access to read messages in " + manager.consoleChannel);
        }
        //</editor-fold>

        //TODO: to manager
        // load unsubscribed users
        if (new File(getDataFolder(), "unsubscribed.txt").exists())
            try {
                Collections.addAll(manager.unsubscribedPlayers, Files.toString(new File(getDataFolder(), "unsubscribed.txt"), Charset.defaultCharset()).split("\n"));
            } catch (IOException e) {
                e.printStackTrace();
            }

        //TODO: to manager
        // load user-defined colors
        if (!new File(getDataFolder(), "colors.json").exists()) saveResource("colors.json", false);
        try {
            LinkedTreeMap<String, String> colorsJson = manager.gson.fromJson(Files.toString(new File(getDataFolder(), "colors.json"), Charset.defaultCharset()), LinkedTreeMap.class);
            for (String key : colorsJson.keySet()) {
                String definition = colorsJson.get(key);
                key = key.toLowerCase();
                manager.colors.put(key, definition);
            }
            getLogger().info("Colors: " + manager.colors);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // check if server can do pings
        double thisVersion = Double.valueOf(Bukkit.getBukkitVersion().split("\\.", 2)[1].split("-")[0]);
        canUsePingNotificationSounds = thisVersion >= 9.0;
        if (!canUsePingNotificationSounds) getLogger().warning("Server version is <1.9, mention sounds are disabled");

        // enable reporting plugins that have canceled chat events
        if (manager.configGetBoolean("ReportCanceledChatEvents")) {
            getLogger().info("Chat event cancellation detector has been enabled");
            cancellationDetector.addListener((plugin, event) -> getLogger().info(event.getClass().getName() + " cancelled by " + plugin));
        }

        //TODO: new api
        // super listener for all discord events
        manager.jda.addEventListener(new ListenerAdapter() {
            public void onEvent(Event event) {
                // don't notify of message receiving events, that's handled in the normal message listener
                if (event.getClass().getName().contains("MessageReceived")) return;

                //notifyListeners(event);
            }
        });

        //TODO: to manager
        // account link manager
        manager.accountLinkManager = new AccountLinkManager();

        //TODO: to manager
        // canned responses
        try {
            manager.responses.clear();
            String key = "DiscordCannedResponses";

            FileReader fr = new FileReader(new File(getDataFolder(), "config.yml"));
            BufferedReader br = new BufferedReader(fr);
            boolean done = false;
            while (!done) {
                String line = br.readLine();
                if (line == null) done = true;
                if (line != null && line.startsWith(key)) {
                    ((Map<String, Object>) new Yaml().load(line.substring(key.length() + 1))).forEach((s, o) -> {
                        if (getConfig().getBoolean("DiscordCannedResponsesTriggersAreCaseInsensitive")) s = s.toLowerCase();
                        manager.responses.put(s, (String) o);
                    });
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onDisable() {
        manager.shutdown();
    }
    //</editor-fold>

    //<editor-fold desc="IPlatform implements">
    public boolean disablePlatform() {
        Bukkit.getPluginManager().disablePlugin(this);
        return true;
    }
    public void loadConfigToManager() {
        reloadConfig();
        getConfig().getValues(true).forEach((k, v) -> manager.config.put(k, v));
    }
    public void info(String message) {
        getLogger().info(message);
    }
    public void warning(String message) {
        getLogger().warning(message);
    }
    public void severe(String message) {
        getLogger().severe(message);
    }
    //</editor-fold>

    //<editor-fold desc="Chat events">
    /**
     * Given a chat event and priority, process the chat event for delivery to Discord
     * @param priority {@link EventPriority} Priority of the message
     * @param event {@link AsyncPlayerChatEvent} Chat event to process
     */
    private void sendChatEvent(String priority, AsyncPlayerChatEvent event) {
        PlatformChatEvent chatEvent = new PlatformChatEvent();
        chatEvent.channel = null;
        chatEvent.customName = event.getPlayer().getCustomName();
        chatEvent.displayName = event.getPlayer().getDisplayName();
        chatEvent.eventName = event.getEventName();
        chatEvent.playerName = event.getPlayer().getName();
        chatEvent.priority = priority;
        chatEvent.world = event.getPlayer().getWorld().getName();

        PlatformChatProcessResult result = manager.processChatEvent(chatEvent);
    }
    @EventHandler(priority = EventPriority.LOWEST) public void onPlayerChatLowest(AsyncPlayerChatEvent event) { sendChatEvent("LOWEST", event); }
    @EventHandler(priority = EventPriority.LOW) public void onPlayerChatLow(AsyncPlayerChatEvent event) { sendChatEvent("LOW", event); }
    @EventHandler(priority = EventPriority.NORMAL) public void onPlayerChatNormal(AsyncPlayerChatEvent event) { sendChatEvent("NORMAL", event); }
    @EventHandler(priority = EventPriority.HIGH) public void onPlayerChatHigh(AsyncPlayerChatEvent event) { sendChatEvent("HIGH", event); }
    @EventHandler(priority = EventPriority.HIGHEST) public void onPlayerChatHighest(AsyncPlayerChatEvent event) { sendChatEvent("HIGHEST", event); }
    @EventHandler(priority = EventPriority.MONITOR) public void onPlayerChatMonitor(AsyncPlayerChatEvent event) { sendChatEvent("MONITOR", event); }
    //</editor-fold>

    //<editor-fold desc="Utilities">
    private static Plugin getPlugin(String pluginName) {
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins())
            if (plugin.getName().toLowerCase().trim().equals(pluginName.toLowerCase().trim()))
                return plugin;
        return null;
    }
    //</editor-fold>

}
