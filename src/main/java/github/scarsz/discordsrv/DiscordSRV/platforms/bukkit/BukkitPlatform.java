package github.scarsz.discordsrv.DiscordSRV.platforms.bukkit;

import github.scarsz.discordsrv.DiscordSRV.Manager;
import github.scarsz.discordsrv.DiscordSRV.api.DiscordSRVListener;
import github.scarsz.discordsrv.DiscordSRV.api.events.GameChatMessagePreProcessEvent;
import github.scarsz.discordsrv.DiscordSRV.api.events.GamePlayerDeathPreProcessEvent;
import github.scarsz.discordsrv.DiscordSRV.platforms.Platform;
import github.scarsz.discordsrv.DiscordSRV.platforms.bukkit.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Made by Scarsz
 *
 * @in /dev/hell
 * @at 11/7/2016
 */
public class BukkitPlatform extends JavaPlugin implements Platform, Listener {

    public Manager manager = new Manager(this);
    public static BukkitPlatform instance = null;

    /*
         /$$$$$$             /$$                          /$$$$$$                                    /$$      /$$             /$$     /$$                       /$$
        |_  $$_/            | $$                         /$$__  $$                                  | $$$    /$$$            | $$    | $$                      | $$
          | $$   /$$$$$$$  /$$$$$$    /$$$$$$   /$$$$$$ | $$  \__//$$$$$$   /$$$$$$$  /$$$$$$       | $$$$  /$$$$  /$$$$$$  /$$$$$$  | $$$$$$$   /$$$$$$   /$$$$$$$  /$$$$$$$
          | $$  | $$__  $$|_  $$_/   /$$__  $$ /$$__  $$| $$$$   |____  $$ /$$_____/ /$$__  $$      | $$ $$/$$ $$ /$$__  $$|_  $$_/  | $$__  $$ /$$__  $$ /$$__  $$ /$$_____/
          | $$  | $$  \ $$  | $$    | $$$$$$$$| $$  \__/| $$_/    /$$$$$$$| $$      | $$$$$$$$      | $$  $$$| $$| $$$$$$$$  | $$    | $$  \ $$| $$  \ $$| $$  | $$|  $$$$$$
          | $$  | $$  | $$  | $$ /$$| $$_____/| $$      | $$     /$$__  $$| $$      | $$_____/      | $$\  $ | $$| $$_____/  | $$ /$$| $$  | $$| $$  | $$| $$  | $$ \____  $$
         /$$$$$$| $$  | $$  |  $$$$/|  $$$$$$$| $$      | $$    |  $$$$$$$|  $$$$$$$|  $$$$$$$      | $$ \/  | $$|  $$$$$$$  |  $$$$/| $$  | $$|  $$$$$$/|  $$$$$$$ /$$$$$$$/
        |______/|__/  |__/   \___/   \_______/|__/      |__/     \_______/ \_______/ \_______/      |__/     |__/ \_______/   \___/  |__/  |__/ \______/  \_______/|_______/
     */

    public File getPluginConfigFile() {
        return new File(getDataFolder(), "config.yml");
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

    public int queryMaxPlayers() {
        return Bukkit.getMaxPlayers();
    }
    public String queryMotd() {
        return Bukkit.getMotd();
    }
    public List<String> queryOnlinePlayers() {
        List<String> onlinePlayers = Bukkit.getOnlinePlayers().stream().map((Function<Player, String>) HumanEntity::getName).collect(Collectors.toList());
        //TODO check if player is vanished
        return onlinePlayers;
    }
    public String queryServerVersion() {
        return Bukkit.getBukkitVersion();
    }
    public String queryTps() {
        return Lag.getTPSString();
    }
    public int queryTotalPlayers() {
        return Bukkit.getWorlds().size() != 0 ? new File(Bukkit.getWorlds().get(0).getWorldFolder().getAbsolutePath(), "/playerdata").listFiles(f -> f.getName().endsWith(".dat")).length : 0;
    }

    /*
         /$$$$$$$  /$$             /$$      /$$$$$$                                        /$$      /$$             /$$     /$$                       /$$
        | $$__  $$| $$            | $$     /$$__  $$                                      | $$$    /$$$            | $$    | $$                      | $$
        | $$  \ $$| $$  /$$$$$$  /$$$$$$  | $$  \__//$$$$$$   /$$$$$$  /$$$$$$/$$$$       | $$$$  /$$$$  /$$$$$$  /$$$$$$  | $$$$$$$   /$$$$$$   /$$$$$$$  /$$$$$$$
        | $$$$$$$/| $$ |____  $$|_  $$_/  | $$$$   /$$__  $$ /$$__  $$| $$_  $$_  $$      | $$ $$/$$ $$ /$$__  $$|_  $$_/  | $$__  $$ /$$__  $$ /$$__  $$ /$$_____/
        | $$____/ | $$  /$$$$$$$  | $$    | $$_/  | $$  \ $$| $$  \__/| $$ \ $$ \ $$      | $$  $$$| $$| $$$$$$$$  | $$    | $$  \ $$| $$  \ $$| $$  | $$|  $$$$$$
        | $$      | $$ /$$__  $$  | $$ /$$| $$    | $$  | $$| $$      | $$ | $$ | $$      | $$\  $ | $$| $$_____/  | $$ /$$| $$  | $$| $$  | $$| $$  | $$ \____  $$
        | $$      | $$|  $$$$$$$  |  $$$$/| $$    |  $$$$$$/| $$      | $$ | $$ | $$      | $$ \/  | $$|  $$$$$$$  |  $$$$/| $$  | $$|  $$$$$$/|  $$$$$$$ /$$$$$$$/
        |__/      |__/ \_______/   \___/  |__/     \______/ |__/      |__/ |__/ |__/      |__/     |__/ \_______/   \___/  |__/  |__/ \______/  \_______/|_______/
     */

    @Override
    public void onEnable() {
        instance = this;
        manager.initialize();
        manager.addListener(new BukkitDiscordSRVListener());

        // start TPS monitor
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Lag(), 100L, 1L);

        // in-game chat events
        if (checkIfPluginEnabled("herochat") && getConfig().getBoolean("HeroChatHook")) {
            getLogger().info("Enabling Herochat hook");
            getServer().getPluginManager().registerEvents(new HerochatHook(), this);
        } else if (checkIfPluginEnabled("legendchat") && getConfig().getBoolean("LegendChatHook")) {
            getLogger().info("Enabling LegendChat hook");
            getServer().getPluginManager().registerEvents(new LegendChatHook(), this);
        } else if (checkIfPluginEnabled("LunaChat") && getConfig().getBoolean("LunaChatHook")) {
            getLogger().info("Enabling LunaChatHook hook");
            getServer().getPluginManager().registerEvents(new LunaChatHook(), this);
        } else if (checkIfPluginEnabled("Towny") && checkIfPluginEnabled("TownyChat") && getConfig().getBoolean("TownyChatHook")) {
            getLogger().info("Enabling TownyChatHook hook");
            getServer().getPluginManager().registerEvents(new TownyChatHook(), this);
        } else if (checkIfPluginEnabled("venturechat") && getConfig().getBoolean("VentureChatHook")) {
            getLogger().info("Enabling VentureChatHook hook");
            getServer().getPluginManager().registerEvents(new VentureChatHook(), this);
        } else {
            getLogger().info("No compatible chat plugins found that have their hooks enabled");
            getServer().getPluginManager().registerEvents(new ChatListener(), this);
        }
    }

    @Override
    public void onDisable() {

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        GameChatMessagePreProcessEvent gameChatMessagePreProcessEvent = new GameChatMessagePreProcessEvent(event.getPlayer().getName(), event.getMessage(), null);
        //manager.processChatEvent(gameChatMessagePreProcessEvent);
        manager.processEvent(gameChatMessagePreProcessEvent);
    }
    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        for (DiscordSRVListener listener : manager.listeners) {
            listener.gamePlayerDeathPreProcess(new GamePlayerDeathPreProcessEvent(event.getEntity().getName(), event.getEntity().getLastDamageCause().getDamage(), event.getDeathMessage(), event.getEntity().getWorld().getName()));
        }
    }

    private boolean checkIfPluginEnabled(String pluginName) {
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins())
            if (plugin.getName().equalsIgnoreCase(pluginName)) return true;
        return false;
    }

}
