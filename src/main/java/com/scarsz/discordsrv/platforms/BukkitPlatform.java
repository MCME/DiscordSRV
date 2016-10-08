package com.scarsz.discordsrv.platforms;

import com.scarsz.discordsrv.Manager;
import com.scarsz.discordsrv.events.PlatformChatEvent;
import com.scarsz.discordsrv.objects.ChatProcessResult;
import com.scarsz.discordsrv.objects.IPlatform;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitPlatform extends JavaPlugin implements IPlatform, Listener {

    private Manager manager = new Manager(this);
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    public void disablePlugin() {
        org.bukkit.Bukkit.getPluginManager().disablePlugin(this);
    }
    public void loadConfig() {
        getConfig().getValues(true).forEach((k, v) -> manager.config.put(k, v));
    }

    private void sendChatEvent(String priority, AsyncPlayerChatEvent event) {
        PlatformChatEvent chatEvent = new PlatformChatEvent();
        chatEvent.customName = event.getPlayer().getCustomName();
        chatEvent.displayName = event.getPlayer().getDisplayName();
        chatEvent.eventName = event.getEventName();
        chatEvent.playerName = event.getPlayer().getName();
        chatEvent.priority = priority;
        chatEvent.world = event.getPlayer().getWorld().getName();

        ChatProcessResult result = manager.processChatEvent(chatEvent);
    }
    @EventHandler(priority = EventPriority.LOWEST) public void onPlayerChatLowest(AsyncPlayerChatEvent event) { sendChatEvent("LOWEST", event); }
    @EventHandler(priority = EventPriority.LOW) public void onPlayerChatLow(AsyncPlayerChatEvent event) { sendChatEvent("LOW", event); }
    @EventHandler(priority = EventPriority.NORMAL) public void onPlayerChatNormal(AsyncPlayerChatEvent event) { sendChatEvent("NORMAL", event); }
    @EventHandler(priority = EventPriority.HIGH) public void onPlayerChatHigh(AsyncPlayerChatEvent event) { sendChatEvent("HIGH", event); }
    @EventHandler(priority = EventPriority.HIGHEST) public void onPlayerChatHighest(AsyncPlayerChatEvent event) { sendChatEvent("HIGHEST", event); }
    @EventHandler(priority = EventPriority.MONITOR) public void onPlayerChatMonitor(AsyncPlayerChatEvent event) { sendChatEvent("MONITOR", event); }

}
