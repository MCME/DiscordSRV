package com.scarsz.discordsrv.todo.listeners;

import com.scarsz.discordsrv.Legacy;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {

    @EventHandler
    public void PlayerDeathEvent(PlayerDeathEvent event) {
        // return if death messages are disabled
        if (!Legacy.plugin.getConfig().getBoolean("MinecraftPlayerDeathMessageEnabled")) return;
        
        Legacy.sendMessage(Legacy.chatChannel, ChatColor.stripColor(event.getDeathMessage()));
    }

}