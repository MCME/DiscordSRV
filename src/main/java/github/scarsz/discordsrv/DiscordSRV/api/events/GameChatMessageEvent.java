package github.scarsz.discordsrv.DiscordSRV.api.events;

import github.scarsz.discordsrv.DiscordSRV.api.Event;

/**
 * Made by Scarsz
 *
 * @in /dev/hell
 * @at 11/7/2016
 */
public abstract class GameChatMessageEvent extends Event {

    private final String playerName;
    private final String message;
    private final String channel;

    public GameChatMessageEvent(String playerName, String message, String channel) {
        this.playerName = playerName;
        this.message = message;
        this.channel = channel;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getMessage() {
        return message;
    }

    public String getChannel() {
        return channel;
    }

}
