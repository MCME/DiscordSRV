package com.scarsz.discordsrv.platforms.bungeecord;

import com.scarsz.discordsrv.Manager;
import com.scarsz.discordsrv.objects.IPlatform;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BungeeCordPlatform extends Plugin implements IPlatform, Listener {

    private Manager manager = new Manager(this);

    //<editor-fold desc="JavaPlugin overrides">
    public void onEnable() {
        getProxy().getPluginManager().registerListener(this, this);
        loadConfigToManager();
        manager.buildJDA(manager.configGetString("BotToken"));
    }
    public void onDisable() {
        manager.shutdown();
    }
    //</editor-fold>

    //<editor-fold desc="IPlatform implements">
    public boolean disablePlatform() {
        return false;
    }
    public void loadConfigToManager() {
//        reloadConfig(); getConfig().getValues(true).forEach((k, v) -> manager.config.put(k, v));
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try {
                OutputStream destinationConfigFileOutputStream = new FileOutputStream(configFile);
                IOUtils.copy(getResourceAsStream("config.yml"), destinationConfigFileOutputStream);
                destinationConfigFileOutputStream.close();
            } catch (IOException e) {
                System.err.println("Failed writing the default config file");
                e.printStackTrace();
            }
        }
    }
    //</editor-fold>

}
