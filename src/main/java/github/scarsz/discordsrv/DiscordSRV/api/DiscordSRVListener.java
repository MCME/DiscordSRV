package github.scarsz.discordsrv.DiscordSRV.api;

import github.scarsz.discordsrv.DiscordSRV.api.events.*;

/**
 * Made by Scarsz
 *
 * @in /dev/hell
 * @at 11/7/2016
 */
@SuppressWarnings("unused")
public abstract class DiscordSRVListener {

    /**
     * Called directly before processing a received chat message from Discord
     * @param event
     */
    public void discordChatMessagePreProcess(DiscordChatMessagePreProcessEvent event) {}
    /**
     * Called directly after processing a received chat message from Discord
     * @param event
     */
    public void discordChatMessagePostProcess(DiscordChatMessagePostProcessEvent event) {}

    /**
     * Called directly before processing a received chat message from the game
     * @param event
     */
    public void gameChatMessagePreProcess(GameChatMessagePreProcessEvent event) {}
    /**
     * Called directly after processing a received chat message from the game
     * @param event
     */
    public void gameChatMessagePostProcess(GameChatMessagePostProcessEvent event) {}

    /**
     * Called directly after a death event is processed to Discord
     * @param event
     */
    public void gamePlayerDeath(GamePlayerDeathEvent event) {}
    public void gamePlayerJoin(GamePlayerJoinEvent event) {}
    public void gamePlayerQuit(GamePlayerQuitEvent event) {}

}
