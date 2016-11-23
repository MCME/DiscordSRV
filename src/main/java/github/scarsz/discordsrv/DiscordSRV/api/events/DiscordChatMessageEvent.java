package github.scarsz.discordsrv.DiscordSRV.api.events;

import github.scarsz.discordsrv.DiscordSRV.api.Event;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

/**
 * Made by Scarsz
 *
 * @in /dev/hell
 * @at 11/7/2016
 */
public abstract class DiscordChatMessageEvent extends Event {

    private final String discordChannelId;
    private final String gameDestinationChannel;
    private final String message;
    private final GuildMessageReceivedEvent rawEvent;
    private final String senderId;

    public DiscordChatMessageEvent(String discordChannelId, String gameDestinationChannel, String message, GuildMessageReceivedEvent rawEvent, String senderId) {
        this.discordChannelId = discordChannelId;
        this.gameDestinationChannel = gameDestinationChannel;
        this.message = message;
        this.rawEvent = rawEvent;
        this.senderId = senderId;
    }

    public String getDiscordChannelId() {
        return discordChannelId;
    }

    public String getGameDestinationChannel() {
        return gameDestinationChannel;
    }

    public String getMessage() {
        return message;
    }

    public GuildMessageReceivedEvent getRawEvent() {
        return rawEvent;
    }

    public String getSenderId() {
        return senderId;
    }

}
