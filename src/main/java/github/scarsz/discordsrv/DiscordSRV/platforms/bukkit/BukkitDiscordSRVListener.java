package github.scarsz.discordsrv.DiscordSRV.platforms.bukkit;

import github.scarsz.discordsrv.DiscordSRV.api.DiscordSRVListener;
import github.scarsz.discordsrv.DiscordSRV.api.events.DiscordChatMessagePostProcessEvent;
import org.bukkit.Bukkit;

/**
 * Made by Scarsz
 *
 * @in /dev/hell
 * @at 11/7/2016
 */
public class BukkitDiscordSRVListener extends DiscordSRVListener {

    @Override
    public void discordChatMessagePostProcess(DiscordChatMessagePostProcessEvent event) {
        if (event.getGameDestinationChannel() == null) {
            Bukkit.broadcastMessage(event.getMessage());
        }
    }

}
