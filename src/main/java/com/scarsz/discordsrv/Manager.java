package com.scarsz.discordsrv;

import com.scarsz.discordsrv.events.PlatformChatEvent;
import com.scarsz.discordsrv.objects.ChatProcessResult;
import com.scarsz.discordsrv.objects.IPlatform;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.JDABuilder;
import net.dv8tion.jda.utils.SimpleLog;

import javax.security.auth.login.LoginException;
import java.util.HashMap;

public class Manager {

    public JDA jda = null;
    public HashMap<String, Object> config = new HashMap<>();

    private final IPlatform platform;

    public Manager(IPlatform platform) {
        this.platform = platform;
    }
    public Manager(IPlatform platform, String botToken) {
        this(platform);

        buildJDA(botToken);
    }

    public void buildJDA(String botToken) {
        // kill previously started JDA if available
        if (jda != null) try { jda.shutdown(false); } catch (Exception e) { e.printStackTrace(); }

        // set log level
        SimpleLog.LEVEL = SimpleLog.Level.WARNING;
        SimpleLog.addListener(new SimpleLog.LogListener() {
            @Override
            public void onLog(SimpleLog simpleLog, SimpleLog.Level level, Object o) {
                if (level == SimpleLog.Level.INFO) System.out.println("[JDA] " + o);
            }
            @Override
            public void onError(SimpleLog simpleLog, Throwable throwable) {}
        });

        // build JDA
        try {
            this.jda = new JDABuilder()
                    .setBotToken(botToken)
                    .setAutoReconnect(true)
                    .setAudioEnabled(false)
                    .setBulkDeleteSplittingEnabled(false)
                    .buildBlocking();
        } catch (LoginException | InterruptedException e) {
            e.printStackTrace();
        }

        // shut down platform if JDA wasn't built
        platform.disablePlugin();
    }

    /**
     * Process the given chat event for delivery to Discord
     * @return result of processing the given PlatformChatEvent
     * @see PlatformChatEvent
     * @see ChatProcessResult
     */
    public ChatProcessResult processChatEvent(PlatformChatEvent event) {
        return new ChatProcessResult();
    }

    public int configGetInt(String key) {
        return (int) config.get(key);
    }
    public String configGetString(String key) {
        return (String) config.get(key);
    }

}
