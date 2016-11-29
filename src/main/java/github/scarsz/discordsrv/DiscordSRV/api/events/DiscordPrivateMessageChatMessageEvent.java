package github.scarsz.discordsrv.DiscordSRV.api.events;

import github.scarsz.discordsrv.DiscordSRV.api.Event;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;

/**
 * Made by Scarsz
 *
 * @in /dev/hell
 * @at 11/7/2016
 */
public class DiscordPrivateMessageChatMessageEvent extends Event {

    private final String message;
    private final PrivateMessageReceivedEvent rawEvent;
    private final String senderId;

    public DiscordPrivateMessageChatMessageEvent(PrivateMessageReceivedEvent rawEvent) {
        this.message = rawEvent.getMessage().getRawContent();
        this.rawEvent = rawEvent;
        this.senderId = rawEvent.getAuthor().getId();
    }

    public String getMessage() {
        return message;
    }

    public PrivateMessageReceivedEvent getRawEvent() {
        return rawEvent;
    }

    public String getSenderId() {
        return senderId;
    }

}
