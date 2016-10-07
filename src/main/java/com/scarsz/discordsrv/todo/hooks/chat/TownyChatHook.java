package com.scarsz.discordsrv.todo.hooks.chat;

import com.palmergames.bukkit.TownyChat.Chat;
import com.palmergames.bukkit.TownyChat.events.AsyncChatHookEvent;
import com.scarsz.discordsrv.Legacy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TownyChatHook implements Listener {

    public TownyChatHook(){
        Legacy.hookedPlugins.add("townychat");

        Chat instance = (Chat) Bukkit.getPluginManager().getPlugin("TownyChat");
        if (instance == null) { Legacy.plugin.getLogger().info("Not automatically enabling channel hooking"); return; }
        List<String> linkedChannels = new LinkedList<>();
        Legacy.channels.forEach((name, channel) -> {
            if (instance.getChannelsHandler() != null && instance.getChannelsHandler().isChannel(name)) {
                instance.getChannelsHandler().getChannel(name).setHooked(true);
                linkedChannels.add(name);
            }
        });
        Legacy.plugin.getLogger().info("Automatically enabled hooking for " + linkedChannels.size() + " channels: " + String.join(", ", linkedChannels));
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onMessage(AsyncChatHookEvent event) {
    	// make sure chat channel is registered
    	if (!Legacy.chatChannelIsLinked(event.getChannel().getName())) return;
        
        // make sure chat channel is linked to discord channel
        if (Legacy.getTextChannelFromChannelName(event.getChannel().getName()) == null) return;

        // make sure message isn't blank
        if (event.getMessage().replace(" ", "").isEmpty()) return;
        
        Legacy.processChatEvent(event.isCancelled(), event.getPlayer(), event.getMessage(), event.getChannel().getName());
    }
    
    public static void broadcastMessageToChannel(String channel, String message, String rawMessage) {
    	// get instance of TownyChat-plugin
    	Chat instance = (Chat) Bukkit.getPluginManager().getPlugin("TownyChat");
    	
    	// return if TownyChat is disabled
    	if (instance == null) return;
    	
    	// return if channel is not available
    	if (!instance.getChannelsHandler().isChannel(channel)) return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (instance.getChannelsHandler().getChannel(channel).isPresent(player.getName())) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', Legacy.plugin.getConfig().getString("ChatChannelHookMessageFormat")
                        .replace("%channelcolor%", instance.getChannelsHandler().getChannel(channel).getMessageColour())
                        .replace("%channelname%", instance.getChannelsHandler().getChannel(channel).getName())
                        .replace("%channelnickname%", instance.getChannelsHandler().getChannel(channel).getChannelTag())
                        .replace("%message%", message)));
                Legacy.notifyPlayersOfMentions(Collections.singletonList(player), rawMessage);
            }
        }
    }
}