package com.scarsz.discordsrv.todo.api;

import com.scarsz.discordsrv.todo.api.events.ProcessChatEvent;
import net.dv8tion.jda.events.Event;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

@SuppressWarnings("unused")
public class DiscordSRVListener implements DiscordSRVListenerInterface {

    public void onDiscordMessageReceived(MessageReceivedEvent event) {}
    public void onRawDiscordEventReceived(Event event) {}
    public void onProcessChat(ProcessChatEvent event) {}

}
