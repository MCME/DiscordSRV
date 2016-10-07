package com.scarsz.discordsrv.todo.api;

import com.scarsz.discordsrv.Legacy;

@SuppressWarnings("unused")
public class DiscordSRVAPI {

    public static void addListener(DiscordSRVListenerInterface listener) {
        Legacy.listeners.add(listener);
    }
    public static void removeListener(DiscordSRVListenerInterface listener) {
        Legacy.listeners.remove(listener);
    }

}
