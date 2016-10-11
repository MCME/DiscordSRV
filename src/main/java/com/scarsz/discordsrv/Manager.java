package com.scarsz.discordsrv;

import com.scarsz.discordsrv.events.PlatformChatEvent;
import com.scarsz.discordsrv.events.results.PlatformChatProcessResult;
import com.scarsz.discordsrv.objects.IPlatform;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.JDABuilder;
import net.dv8tion.jda.entities.Channel;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.utils.SimpleLog;

import javax.security.auth.login.LoginException;
import java.util.HashMap;

public class Manager {

    public static final double version = 13.0;

    public HashMap<String, Object> config = new HashMap<>();
    public JDA jda = null;
    public final IPlatform platform;

    public Manager(IPlatform platform) {
        System.out.println("Initializing DiscordSRV v13");

        this.platform = platform;
        System.out.println("Platform: " + platform.getClass().toString().replace("Platform", ""));
    }
    public Manager(IPlatform platform, String botToken) {
        this(platform);

        buildJDA(botToken);
    }

    public void buildJDA(String botToken) {
        System.out.println("Initializing JDA");

        // kill previously started JDA if available
        if (jda != null) try { System.out.println("Murdering previous JDA instance"); jda.shutdown(false); } catch (Exception e) { e.printStackTrace(); }

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
        if (jda == null) {
            System.err.println("DiscordSRV failed to build JDA. Check the \"caused by\" lines above.");
            platform.disablePlatform();
            return;
        }

        // game status
        if (!configGetString("DiscordGameStatus").isEmpty()) {
            jda.getAccountManager().setGame(configGetString("DiscordGameStatus"));
        }

        // print the text channels that the bot can see
        for (Guild server : jda.getGuilds()) {
            System.out.println("Found guild " + server);
            for (Channel channel : server.getTextChannels()) System.out.println("- " + channel);
        }
    }

    /**
     * Shut down the DiscordSRV manager safely
     */
    public void shutdown() {
        // disconnect from Discord
        if (jda != null) jda.shutdown(false);
    }

    /**
     * Process the given chat event for delivery to Discord
     * @return result of processing the given PlatformChatEvent
     * @see PlatformChatEvent
     * @see PlatformChatProcessResult
     */
    public PlatformChatProcessResult processChatEvent(PlatformChatEvent event) {
        return new PlatformChatProcessResult();
    }

    //<editor-fold desc="Config getters">
    public int configGetInt(String key) {
        return (int) config.get(key);
    }
    public String configGetString(String key) {
        return (String) config.get(key);
    }
    //</editor-fold>

}
