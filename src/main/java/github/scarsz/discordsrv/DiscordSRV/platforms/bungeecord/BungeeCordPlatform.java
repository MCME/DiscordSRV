package github.scarsz.discordsrv.DiscordSRV.platforms.bungeecord;

import github.scarsz.discordsrv.DiscordSRV.Manager;
import github.scarsz.discordsrv.DiscordSRV.platforms.Platform;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * Made by Scarsz
 *
 * @in /dev/hell
 * @at 11/7/2016
 */
public abstract class BungeeCordPlatform extends Plugin implements Platform {

    public Manager manager = new Manager(this);
    public static BungeeCordPlatform instance = null;

    @Override
    public void onEnable() {
        instance = this;
        manager.initialize();
    }

    @Override
    public void onDisable() {

    }

}
