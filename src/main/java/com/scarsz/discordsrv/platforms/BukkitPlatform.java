package com.scarsz.discordsrv.platforms;

import com.scarsz.discordsrv.Manager;
import com.scarsz.discordsrv.objects.IPlatform;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
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

}
