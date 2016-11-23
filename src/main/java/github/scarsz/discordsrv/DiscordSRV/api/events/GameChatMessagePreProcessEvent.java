package github.scarsz.discordsrv.DiscordSRV.api.events;

/**
 * Made by Scarsz
 *
 * @in /dev/hell
 * @at 11/7/2016
 */
public class GameChatMessagePreProcessEvent extends GameChatMessageEvent {

    public GameChatMessagePreProcessEvent(String playerName, String message, String channel) {
        super(playerName, message, channel);
    }

}
