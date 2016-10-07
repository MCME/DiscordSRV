package com.scarsz.discordsrv.todo.util;

import com.scarsz.discordsrv.Legacy;
import com.scarsz.discordsrv.todo.hooks.vanish.EssentialsHook;
import com.scarsz.discordsrv.todo.hooks.vanish.PremiumVanishHook;
import org.bukkit.Bukkit;
import com.scarsz.discordsrv.todo.hooks.vanish.SuperVanishHook;
import com.scarsz.discordsrv.todo.hooks.vanish.VanishNoPacketHook;
import org.bukkit.entity.Player;

@SuppressWarnings({"ConstantConditions", "WeakerAccess"})
public class VanishedPlayerCheck {

    public static boolean checkPlayerIsVanished(Player player) {
        boolean isVanished = false;

        if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) isVanished = EssentialsHook.isVanished(player) || isVanished;
        if (Bukkit.getPluginManager().isPluginEnabled("PremiumVanish")) isVanished = PremiumVanishHook.isVanished(player) || isVanished;
        if (Bukkit.getPluginManager().isPluginEnabled("SuperVanish")) isVanished = SuperVanishHook.isVanished(player) || isVanished;
        if (Bukkit.getPluginManager().isPluginEnabled("VanishNoPacket")) isVanished = VanishNoPacketHook.isVanished(player) || isVanished;

        if (Legacy.plugin.getConfig().getBoolean("PlayerVanishLookupReporting")) Legacy.plugin.getLogger().info("Looking up vanish status for " + player + ": " + isVanished);
        return isVanished;
    }

}