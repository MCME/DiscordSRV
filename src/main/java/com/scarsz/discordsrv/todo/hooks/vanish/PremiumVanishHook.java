package com.scarsz.discordsrv.todo.hooks.vanish;

import org.bukkit.entity.Player;

public class PremiumVanishHook {

    public static boolean isVanished(Player player) {
        return SuperVanishHook.isVanished(player);
    }

}
