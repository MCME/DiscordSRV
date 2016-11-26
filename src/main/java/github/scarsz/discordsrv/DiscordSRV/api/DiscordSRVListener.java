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

    private final String name;
    public DiscordSRVListener(String name) {
        this.name = name;
    }
    /**
     * Get the name of the listener
     * @return the name of the listener
     */
    public String getName() {
        return name;
    }

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
     * Called directly before a death event is processed to Discord
     * @param event
     */
    public void gamePlayerDeathPreProcess(GamePlayerDeathPreProcessEvent event) {}
    /**
     * Called directly after a death event is processed to Discord
     * @param event
     */
    public void gamePlayerDeathPostProcess(GamePlayerDeathPostProcessEvent event) {}

    /**
     * Called directly before a join event is processed to Discord
     * @param event
     */
    public void gamePlayerJoinPreProcess(GamePlayerJoinPreProcessEvent event) {}
    /**
     * Called directly before a join event is processed to Discord
     * @param event
     */
    public void gamePlayerJoinPostProcess(GamePlayerJoinPostProcessEvent event) {}

    /**
     * Called directly before a quit event is processed to Discord
     * @param event
     */
    public void gamePlayerQuitPreProcessEvent(GamePlayerQuitPreProcessEvent event) {}
    /**
     * Called directly after a quit event is processed to Discord
     * @param event
     */
    public void gamePlayerQuitPostProcessEvent(GamePlayerQuitPostProcessEvent event) {}

}
