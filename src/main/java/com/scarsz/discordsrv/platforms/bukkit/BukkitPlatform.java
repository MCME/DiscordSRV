package com.scarsz.discordsrv.platforms.bukkit;

import com.scarsz.discordsrv.Manager;
import com.scarsz.discordsrv.events.PlatformChatEvent;
import com.scarsz.discordsrv.events.results.PlatformChatProcessResult;
import com.scarsz.discordsrv.objects.IPlatform;
import com.scarsz.discordsrv.todo.objects.Lag;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitPlatform extends JavaPlugin implements IPlatform, Listener {

    private Manager manager = new Manager(this);

    //<editor-fold desc="JavaPlugin overrides">
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        loadConfigToManager();
        manager.buildJDA(manager.configGetString("BotToken"));

        // start TPS poller
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Lag(), 100L, 1L);
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
        reloadConfig(); getConfig().getValues(true).forEach((k, v) -> manager.config.put(k, v));
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

}
