package github.scarsz.discordsrv.DiscordSRV.api.events;

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

/**
 * Made by Scarsz
 *
 * @in /dev/hell
 * @at 11/7/2016
 */
public class DiscordChatMessagePostProcessEvent extends DiscordChatMessageEvent {

    public DiscordChatMessagePostProcessEvent(String discordChannelId, String gameDestinationChannel, String message, GuildMessageReceivedEvent rawEvent, String senderId) {
        super(discordChannelId, gameDestinationChannel, message, rawEvent, senderId);
    }

}
