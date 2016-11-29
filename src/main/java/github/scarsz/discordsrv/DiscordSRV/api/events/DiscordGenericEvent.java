package github.scarsz.discordsrv.DiscordSRV.api.events;

import github.scarsz.discordsrv.DiscordSRV.api.Event;

/**
 * Made by Scarsz
 *
 * @in /dev/hell
 * @on 11/28/2016
 * @at 5:48 PM
 */
public class DiscordGenericEvent extends Event {

    private final net.dv8tion.jda.core.events.Event rawEvent;

    public DiscordGenericEvent(net.dv8tion.jda.core.events.Event rawEvent) {
        this.rawEvent = rawEvent;
    }

    public net.dv8tion.jda.core.events.Event getRawEvent() {
        return rawEvent;
    }

}
