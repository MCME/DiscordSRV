package github.scarsz.discordsrv.DiscordSRV.api.events;

/**
 * Made by Scarsz
 *
 * @in /dev/hell
 * @at 11/7/2016
 */
public class GameChatMessagePostProcessEvent extends GameChatMessageEvent {

    public GameChatMessagePostProcessEvent(String playerName, String message) {
        super(playerName, message);
    }

}
